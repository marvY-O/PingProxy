package Machine;
import java.io.IOException;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;

public class run {
    public static void main (String args[]) throws IOException{
    	
    	String clientIP = "127.0.0.1";
        
        try {
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            while( networkInterfaceEnumeration.hasMoreElements()){
                for ( InterfaceAddress interfaceAddress : networkInterfaceEnumeration.nextElement().getInterfaceAddresses()) {
                    if ( interfaceAddress.getAddress().isSiteLocalAddress()) {
                        String cur = interfaceAddress.getAddress().getHostAddress();
                        if (cur.substring(0, 3).equals("192")) {
                        	clientIP = cur;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        
    	Scanner sc = new Scanner(System.in);
    	System.out.printf("Enter IP Address of the server: ");
    	String ac_address = sc.next();
        //String ac_address = "192.168.1.4";
       
        System.out.printf("Enter port of the server: ");
    	int port = sc.nextInt();
        //int  port = 5000;
        
        Machine m = new Machine(ac_address, port, clientIP);
        m.initiate();
    }
}
