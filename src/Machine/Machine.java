package Machine;
import java.io.*;  
import  java.lang.*;
import java.util.*;
import java.net.*; 
import Message.*; 

public class Machine {
    String ac_address, clientIP;
    int ac_port;
    Queue<Packet> buffer;
    Queue<Packet> receiveBuffer;

    public Machine(String ac_address, int ac_port, String clientIP) throws IOException{
        this.ac_address = ac_address;
        this.clientIP = clientIP;
        this.ac_port = ac_port;
        this.buffer = new LinkedList<Packet>();
        this.receiveBuffer = new LinkedList<Packet>();
    }
    
	public void initiate() {
		Scanner sc = new Scanner(System.in);
        try{

            Socket s = new Socket(ac_address, ac_port);
            
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            System.out.printf("Connected to server %s:%d\n", ac_address, ac_port);
            
            Runnable replier = new Runnable() {
            	@Override
            	public void run() {
            		while (true) {
            			Packet p;
            			try {
            				p = (Packet) ois.readObject();
            				if (p.type == "PING") {
            					Packet reply = new Packet();
            					reply.clientIP = clientIP;
            					reply.destinationIP = p.clientIP;
            					reply.type="YES";
            					reply.timestamp = p.timestamp;
            					oos.writeObject(reply);
            				}
            				else {
            					synchronized (buffer) { 
            						buffer.add(p);
            					}
            				}
            			} catch(IOException e) {
                        	e.printStackTrace();
                        } catch(ClassNotFoundException e) {
                        	e.printStackTrace();
                        }
            		}
            	}
            };
            
            while (true) {
	            System.out.printf("Enter IP to ping: ");
	            String destIP = sc.next();
	            int noPackets = 5;
	            
	            Runnable send = new Runnable() {
	            	@Override
	            	public void run() {
	            		for (int i=0; i<noPackets; i++) {
	            			Packet p = new Packet();
	        	            p.clientIP = clientIP;
	        	            p.destinationIP = destIP;
	        	            p.type = "PING";
	        	            p.timestamp = System.currentTimeMillis();
	        	            oos.writeObject(p);
	            		}
	            	}
	            };

	            Runnable recieve = new Runnable () {
	            	@Override
	            	public void run() {
			            int cntPackets = 0;
			            while (true) {
			            	
			            	if (cntPackets == noPackets) break;
					        if (buffer.isEmpty()) continue;
					        else {
					        	Packet pRec = buffer.poll();
					        	int pSize = 64;
					        	if (pRec.type == "YES") {
					        		System.out.printf("Recvd %d bytes from %s time=%lms\n", pSize, pRec.clientIP, System.currentTimeMillis()-pRec.timestamp);
					        	}
					        	else if (pRec.type == "UNREACHABLE") {
					        		System.out.printf("%s is unreachable\n", pRec.clientIP);
					        	}
					        	cntPackets++;
					        }
			            }
	            	}
	            };
	            
	            Thread sender = new Thread(send);
	            Thread reciever = new Thread(recieve);
	            sender.start();
	            reciever.start();
		            
	       }
            
            
        }
        catch(IOException e) {
        	e.printStackTrace();
        }
	}
}
