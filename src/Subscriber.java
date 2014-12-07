import java.net.*;
import com.medavox.distribackup.peers.*;
import java.nio.file.*;
public class Subscriber extends Peer
{
    private static String defaultHost = "127.0.0.1";
    private static int defaultConnectPort = 1210;
    private static int defaultListeningPort = 1211;
    private static Path defaultRoot = Paths.get("/home/scc/distribackup/subscriber-root");
    //private String defaultRoot = "subscriber-root";
    public Subscriber(Path root, String host, int port)
    {
	super(root, 1211);
	
        connect(host, port);
    }
    
    public static void main (String args[])
    {//local listening port
	int port = 0;
	String host = "";
	if(args.length == 2)
	{
	    try
	    {
		port = Integer.parseInt(args[1]);
	    }
	    catch(NumberFormatException nfe)
	    {
		System.err.println("incorrect arguments. \""+args[1]+"\" is not a valid port number");
		usage();
	    }
	}
	else if(args.length == 1)
	{
	    port = defaultConnectPort;
	    host = args[0];
	}
	else if(args.length == 0)
	{
	    port = defaultConnectPort;
	    host = defaultHost;
	}
	Subscriber p = new Subscriber(defaultRoot, host, port);
    }
    
    public static void usage()
    {
	System.out.println("Usage: TODO");
	System.exit(1);
    }
    /**Subscriber user has edited files in error */
    public void fileChanged(Path file, String eventType)
    {
	System.err.println("WARNING: Don't edit these files!");
	System.err.println("They are merely copies, your changes will be overwritten!");
    }
    
    /* connect to the publisher
     * wait for update announcement, then
     * wait for incoming files
     * whenever one's finished, announce you have it and wait for requests
     * if someone else announces they have it while you're waiting, request it from them
     * */
}
