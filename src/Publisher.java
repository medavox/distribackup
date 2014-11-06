import com.medavox.connections.*;
import java.util.*;

public class Publisher
{
	private final int LISTEN_SOCKET_MAX_BACKLOG = 50;
	private final int LISTEN_PORT = 1210;
    private List<Peer> peers = new LinkedList<Peer>();
    
    public Publisher()
    {
        ServerSocket svr = new ServerSocket(LISTENING_PORT, LISTEN_SOCKET_MAX_BACKLOG);
		
    }
    
    public static void main (String args[])
    {
		Publisher p new Publisher();
	}
}

