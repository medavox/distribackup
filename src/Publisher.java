//import com.medavox.connections.*;
import com.medavox.distribackup.peers.*;
import com.medavox.distribackup.connections.*;
import com.medavox.distribackup.filesystem.FileInfo;
import com.medavox.distribackup.filesystem.FileUtils;
import com.medavox.distribackup.filesystem.UpdateAnnouncement;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class Publisher extends Peer
{
    private static int port = 1210;
    private static Path defaultRoot = Paths.get("/home/scc/distribackup/publisher-root");
    private long globalRevisionNumber;//the grand-daddy. This ones defines the network's value
    public Path root;
    //private List<Peer> peers = new LinkedList<Peer>();
    //private final Path root = Paths.get("root");
    
    public Publisher(Path root, int port)
    {/*keep a list of peers we've seen
		use some kind of heartbeat system to check they're still around
		on a file change:
		    send changed file to all subscribers*/
		super(root, port);
    }
    /*whenever a file has changed or been added,
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
			    System.out.println("Sending file "+file);
			    globalRevisionNumber++;//increment the actual Global Revision Number
		    	//update/create entry in the archive state
			    //make sure the path we pass is relative to archive root
			    /*Path relativePath = root.relativize(file);
		    	String name = relativePath.getFileName().toString();
		    	String path = relativePath.getParent().toString();
		    	
		    	File asFile = file.toFile();
		    	FileInfo newFile;
	    		newFile = new FileInfo(name, path);//if it's a directory
	    		
		    	if(!asFile.isDirectory())
		    	{//is a file, so use the Filewise constructor for FileInfo
					try
					{
						long fileSize = Files.size(file);
				
				    	long revNum = 1;//this is a new file, so it's the first version!
				    	byte[] checksum = FileUtils.checksum(file.toFile());
				    	newFile = new FileInfo(name, path, fileSize, revNum, checksum);
					} 
					catch (IOException e)
					{
						e.printStackTrace();
						System.exit(1);
					}
		    	}*/
			    
			    FileInfo newFile = pathToFileInfo(file, 1);
		    	//add created FileInfo object to globalArchiveState
		    	FileInfo[] update = {newFile};
		    	globalArchiveState.update(globalRevisionNumber, update);
		    	
		    	//announce change to all known peers
			    for(PeerInfo p : peers.values())
			    {
			    	ConnectionOperator co = p.getOpenConnection();
			    	UpdateAnnouncement ua = new UpdateAnnouncement(globalRevisionNumber, update);

			    	try
			    	{
			    		co.sendUpdateAnnouncement(ua);
			    	}
			    	catch(IOException ioe)
			    	{
			    		System.err.println("ERROR: Failed to send update to peer: "+p);
			    	}
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

