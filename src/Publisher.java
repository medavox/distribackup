//import com.medavox.connections.*;
import com.medavox.distribackup.peers.*;
import com.medavox.distribackup.connections.*;
import java.util.*;
import java.net.*;
import java.nio.file.*;

public class Publisher extends Peer
{
    private int port = 1210;
    private String defaultRoot = "/home/scc/distribackup/publisher-root";
    public Path root;
    private List<Peer> peers = new LinkedList<Peer>();
    //private final Path root = Paths.get("root");
    
    public Publisher(Path root, int port)
    {/*keep a list of peers we've seen
	use some kind of heartbeat system to check they're still around
	on a file change:
	    send changed file to all subscribers*/
	super(root, port);
    }
    /*whenever a file has changed or been added,
     * send that file to every subscriber
     * limit concurrent connections to something sensible*/
    public Publisher()
    {
	super(Paths.get(defaultRoot), port);
    }
    
    public Publisher(Path root)
    {
	super(root, port);
    }
    
    public void newSocket(Socket s)
    {
	//see if we've seen the connecting peer before
	//if not, create a new peer object (query for some info)
	//if we have, then add this socket to that peer's open sockets
	
	//bis = new BufferedInputStream(s.getInputStream());
	//bos = new BufferedOutputStream(s.getOutputStream());
	
	handshake(s);
	
	
	
	boolean isNewPeer = true;//STOPGAP: REMOVE
	if(isNewPeer)
	{
	    byte[] version
	}
	else
	{
	    
	}
    }
    
    private static void usage()
    {
	System.out.println("Usage: LOLOLOL");
	System.exit(1);
    }
    
    public static void main (String[] args)
    {
	//get the folder we need to watch 
	try
	{
	    Publisher p = new Publisher();
	    
	}
	catch(InvalidPathException ipe)
	{
	    usage();
	}
	
    }
}

