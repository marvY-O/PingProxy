package Authenticator;
import java.io.*;
import Message.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
	Socket s;
    public HashMap<InetAddress, Queue<Packet>> buffer;
    String serverIP;
      
    public ClientHandler(Socket sc, HashMap<InetAddress, Queue<Packet>> buffer, String serverIP) throws IOException{
        s = sc;
        this.buffer = buffer;
        this.serverIP = serverIP;
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

                	while (!Thread.currentThread().isInterrupted()) {
                			Packet p;
                			if (s.isClosed()) {
                				synchronized(buffer) {
                					if (buffer.containsKey(s.getInetAddress())) buffer.remove(s.getInetAddress());
                				}
                				try {
	                				s.close();
	                				oos.close();
	                				ois.close();
                				} catch(IOException e) {
                					e.printStackTrace();
                				}
                				Thread.currentThread().interrupt();
                				continue;
                			}
							try {
								p = (Packet) ois.readObject();
								InetAddress destAddr, clientAddr;
								clientAddr = InetAddress.getByName(p.clientIP);

								try {
									destAddr = InetAddress.getByName(p.destinationIP);
								} catch (UnknownHostException e) {
									String a = p.clientIP;
									p.clientIP = p.destinationIP;
									p.destinationIP = a;
									p.type = "NO";
									System.out.println("unknown host");
									synchronized(buffer) {
										buffer.get(clientAddr).add(p);
									}
									continue;
								}  
								
								if (p.destinationIP.equals(serverIP)) {
									String a = p.clientIP;
									p.clientIP = p.destinationIP;
									p.destinationIP = a;
									p.type = "YES";
									System.out.println("same as server");
									synchronized(buffer) {
										buffer.get(clientAddr).add(p);
									}
								}
								else {
									String a = p.clientIP;
									p.clientIP = p.destinationIP;
									p.destinationIP = a;
									synchronized (buffer) {
										if (buffer.containsKey(clientAddr)) {
											p.type = "YES";
											buffer.get(clientAddr).add(p);
											continue;
										}
									}
									p.type = "NO";
									synchronized (buffer) {
										buffer.get(clientAddr).add(p);
									}
									
								}
							} catch (StreamCorruptedException e) {
								return;
							} catch (EOFException e) {
								synchronized(buffer) {
                					if (buffer.containsKey(s.getInetAddress())) buffer.remove(s.getInetAddress());
                				}
								Thread.currentThread().interrupt();
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

                	while (!Thread.currentThread().isInterrupted()) {
                		if (s.isClosed()) {
            				if (buffer.containsKey(s.getInetAddress())) buffer.remove(s.getInetAddress());
            				try {
                				s.close();
                				oos.close();
                				ois.close();
            				} catch(IOException e) {
            					e.printStackTrace();
            				}
            				Thread.currentThread().interrupt();
            				continue;
            			}
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
