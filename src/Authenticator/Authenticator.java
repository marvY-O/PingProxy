package Authenticator;
import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.*;
import Message.Packet;

public class Authenticator {
    int port;
    HashMap<InetAddress, Queue<Packet>> buffer;
    String serverIP;
    
    public Authenticator(int port, String serverIP) {
        this.port = port;
        buffer = new HashMap<InetAddress, Queue<Packet>>();
        this.serverIP = serverIP;
    }

    public void start() throws IOException{
        ServerSocket ss = new ServerSocket(port);
        System.out.printf("Server started at %s:%d\n", serverIP, port);
        while (true){
            Socket s = null; 
            try {
                s = ss.accept();
                System.out.printf("A new client is connected : %s\n", (String) s.getInetAddress().getHostAddress());
                
                synchronized (buffer) {
                	buffer.put(s.getInetAddress(), new LinkedList<Packet>());
                }
                
                ClientHandler clientNew = new ClientHandler(s, buffer);
                
                Thread t = new Thread(clientNew);
                t.start();                  
                
            }
            catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }

}
    

