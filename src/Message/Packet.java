package Message;
import java.io.Serializable;

public class Packet implements Serializable{
	public String clientIP;
	public String destinationIP;
	public String type;
	public int pktID;
	public long timestamp;
}
