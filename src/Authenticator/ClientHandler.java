package Authenticator;
import java.io.*;
import Message.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
	Socket s;
    public HashMap<InetAddress, Queue<Packet>> buffer;
      
    public ClientHandler(Socket sc, HashMap<InetAddress, Queue<Packet>> buffer) throws IOException{
        s = sc;
        this.buffer = buffer;
    }
    
	@Override
	public void run() {
		ObjectOutputStream oos;
    	ObjectInputStream ois;

        try{
    		oos = new ObjectOutputStream(s.getOutputStream());
    		ois = new ObjectInputStream(s.getInputStream());
    		
    		Runnable receiver = new Runnable() {
                @Override
                public void run() {

                	while (true) {
                			Packet p;
							try {
								p = (Packet) ois.readObject();
								InetAddress destAddr = InetAddress.getByName(p.destinationIP);
								synchronized (buffer) {
									if (buffer.containsKey(destAddr)) {
										buffer.get(destAddr).add(p);
									}else {
										String a = p.clientIP;
										p.clientIP = p.destinationIP;
										p.destinationIP = a;
										p.type = "NO";
									}
								}
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
                	}
                	
                }
            };
            
            Runnable sender = new Runnable() {
                @Override
                public void run() {

                	while (true) {
                		if (buffer.get(s.getInetAddress()).size() == 0) continue;
                		Packet curPacket;
                		synchronized (buffer) {
	            			curPacket = buffer.get(s.getInetAddress()).peek();
	            			buffer.get(s.getInetAddress()).poll();
                		}
            			try {
            				oos.writeObject(curPacket);
            			} catch(IOException e) {
            				e.printStackTrace();
            			}
                		
                	}
                	
                }
            };
            
            
            Thread recv = new Thread(receiver);
            recv.start();
            
            Thread send = new Thread(sender);
            send.start();
        }catch(IOException e) {
        	e.printStackTrace();
        }
	}
}
