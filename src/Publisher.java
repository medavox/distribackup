//import com.medavox.connections.*;
import com.medavox.distribackup.peers.*;
import com.medavox.distribackup.connections.*;

import static java.nio.file.StandardWatchEventKinds.*;
import java.util.*;
import java.net.*;
import java.io.IOException;
import java.nio.file.*;

public class Publisher extends Peer
{
    private static int port = 1210;
    private static Path defaultRoot = Paths.get("/home/scc/distribackup/publisher-root");
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
    /*public Publisher()
    {
	super(defaultRoot, port);
    }
    
    public Publisher(Path root)
    {
	super(root, port);
    }*/
    
    public void fileChanged(Path file, String eventType)
    {
	switch(eventType)
	{
	    case "ENTRY_DELETE":
	    //don't send anything
	    break;
	    
	    case "ENTRY_CREATE":
	    case "ENTRY_MODIFY":
		try
		{
		    System.out.println("Sending file "+file);
		    sendFile(file, bos);
		}
		catch(IOException ioe)
		{
		    ioe.printStackTrace();
		    System.exit(1);
		}
	    break;
	}
    }
    
    private static void usage()
    {
	System.out.println("Usage: TODO");
	System.exit(1);
    }
    
    public static void main (String[] args)
    {
	//get the folder we need to watch 
	try
	{
	    Publisher p = new Publisher(defaultRoot, port);
	}
	catch(InvalidPathException ipe)
	{
	    usage();
	}
    }
}

