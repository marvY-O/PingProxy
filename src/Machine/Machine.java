package Machine;
import java.io.*;  
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
    
    public static int getSerializedSize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.flush();
        return baos.size();
    }
    
	public void initiate() {
		Scanner sc = new Scanner(System.in);
        try{

            Socket s = new Socket(ac_address, ac_port);
            
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            System.out.printf("Connected to server %s:%d\n", ac_address, ac_port);
            
            Runnable reply = new Runnable() {
            	@Override
            	public void run() {
            		while (!Thread.currentThread().isInterrupted()) {
            			Packet p;
            			if (s.isClosed()) {
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
            				if (p.type.equals("PING")) {
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
            			} catch(EOFException e) {
            				System.out.printf("\nServer Disconnected!! Retry again..\n");
            				Thread.currentThread().interrupt();
            			} catch(IOException e) {
                        	e.printStackTrace();
                        } catch(ClassNotFoundException e) {
                        	e.printStackTrace();
                        }
            		}
            	}
            };
            
            Thread replier = new Thread(reply);
            replier.start();
            
            while (true) {
	            System.out.printf("Enter IP to ping: ");
	            String destIP = sc.next();
	            int noPackets = 10;


	            Runnable send = new Runnable () {
	            	@Override
	            	public void run() {
	            		if (s.isClosed()) return;
	            		for (int i=0; i<noPackets; i++) {
	            			Packet p = new Packet();
	        	            p.clientIP = clientIP;
	        	            p.destinationIP = destIP;
	        	            p.type = "PING";
	        	            p.timestamp = System.currentTimeMillis();
	        	            try{
	        	            	oos.writeObject(p);
	        	            	Thread.sleep(50);
	        	            } catch(SocketException e) {
	        	            	System.out.printf("\nError reaching Server!!\n");
	        	            	return;
	            			}catch(IOException e) {
	        	            	e.printStackTrace();
	        	            } catch(InterruptedException e) {
	        	            	e.printStackTrace();
	        	            }
	        	            
	            		}
	    	            
	            	}
	            };
	            

	            Thread sender = new Thread(send);
	            sender.start();
	            long curTime = System.currentTimeMillis();
	            
	            int cntPackets = 0;
	            while (true) {
	            	
	            	if (cntPackets == noPackets) break;
	            	if (System.currentTimeMillis() - curTime > 5000) {
	            		System.out.printf("\nRequest Timed Out.\n");
	            		break;
	            	}
	            	synchronized(buffer) {
	            		if (buffer.isEmpty()) continue;
	            	}
	            	Packet pRec;
	            	synchronized(buffer) {
	            		pRec = buffer.poll();
	            	}
	            	
		        	int pSize = 64;
		        	if (pRec.type.equals("YES")) {
		        		System.out.printf("Recvd %d bytes from %s time=%dms\n", getSerializedSize(pRec), pRec.clientIP, System.currentTimeMillis()-pRec.timestamp);
		        	}
		        	else if (pRec.type.equals("NO")) {
		        		System.out.printf("%s is unreachable\n", pRec.clientIP);
		        	}
		        	else {
		        		System.out.printf("Unknown %s\n", pRec.type);
		        	}
		        	cntPackets++;
			        
	            }
		            
	       }
            
            
        }
        catch(IOException e) {
        	e.printStackTrace();
        }
	}
}
