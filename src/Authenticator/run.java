package Authenticator;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class run {
    public static void main(String args[]) throws IOException{
        int ac_port = 5000;
        String serverIP = "127.0.0.1";

        try {
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            while( networkInterfaceEnumeration.hasMoreElements()){
                for ( InterfaceAddress interfaceAddress : networkInterfaceEnumeration.nextElement().getInterfaceAddresses()) {
                    if ( interfaceAddress.getAddress().isSiteLocalAddress()) {
                        String cur = interfaceAddress.getAddress().getHostAddress();
                        if (cur.substring(0, 3).equals("192")) {
                        	serverIP = cur;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Authenticator ac = new Authenticator(ac_port, serverIP);
        ac.start();
        
    }
}
