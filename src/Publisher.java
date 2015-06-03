//import com.medavox.connections.*;
import com.medavox.distribackup.peers.*;
import com.medavox.distribackup.connections.*;
import com.medavox.distribackup.filesystem.ArchiveInfo;
import com.medavox.distribackup.filesystem.FileInfo;
import com.medavox.distribackup.filesystem.FileUtils;
import com.medavox.distribackup.filesystem.FileInfoBunch;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

/**The main entry point for peers pushing updates to others.*/
public class Publisher extends Peer
{
    private static int port = 1210;
    private static Path defaultRoot = Paths.get("/home/scc/distribackup/publisher-root");//TODO: make relative for demo
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
		publisherUUID = myUUID;
		globalArchiveState.setGRN(0);
		//TODO: perform initial filetree scan, and add found files to archive state
		//FileInfo[] foundFiles = FileUtils.scanFileTree(root);
		//globalArchiveState.update(foundFiles);
    }
    
    public void receiveUpdateAnnouncement(ReceivedMessage ua)//TODO: better handling of Publisher impostor
    {
        //WE are the publisher!
    	//so there's an impostor in our midst...
    	PeerInfo pi = peers.get(ua.getUUID());
    	System.err.println("WARNING: Peer \""+pi+
    			"\" is pretending to be the Publisher!");
    }
    
    public void handleFileRequest(ReceivedMessage fr)
    {
    	//we (nearly) always have the file and the right version
    	//(unless we are a new Publisher in an old network,
    	//or a returning-from-long-absence Publisher that needs bringing up to date)
	    	//construct a FileDataChunk for it
	    	//then send it to the relevant peer
    	
    	//find out which file is being requested
    	FileInfo fi = ((FileInfoBunch)fr.getCommunicable()).getFiles()[0];
    	System.out.println("Received File Request for: "+fi.getName());
        	
    	boolean hasFile = globalArchiveState.containsKey(fi.toString());
    	FileInfo extantFileInfo = globalArchiveState.getFileInfoWithPath(fi.toString());
    	boolean isRightVersion = (fi.getRevisionNumber() == extantFileInfo.getRevisionNumber());
    	
    	//call common code for both versions of this method
    	handleFileRequest(fr, hasFile, isRightVersion);
    }
    
    /**Callback for when a file is finished*/
    public void finishedDownloadingFile(FileInfo cfi)
    {
    	//we're the publisher
    }
    
    public void fileChanged(Path file, String eventType)
    {
		switch(eventType)
		{
		    case "ENTRY_DELETE":
		    	//TODO
		    break;
		    
		    case "ENTRY_CREATE"://a MODIFY is triggered straight after anyway
		    case "ENTRY_MODIFY":
			    globalRevisionNumber++;//increment the actual Global Revision Number
		    	//TODO: roll consecutive create and modifies into one
			    
			    FileInfo newFile = new FileInfo(file, 1);
		    	//add created FileInfo object to globalArchiveState
		    	FileInfo[] update = {newFile};
		    	globalArchiveState.update(globalRevisionNumber, update);
		    	
		    	//announce change to all known peers
			    for(PeerInfo p : peers.values())
			    {
			    	if(p.hasOpenConnection())
			    	{
				    	ConnectionOperator co = p.getOpenConnection();
				    	FileInfoBunch ua = new FileInfoBunch(globalRevisionNumber, update);
				    	try
				    	{
				    		co.sendUpdateAnnouncement(ua);
				    	}
				    	catch(IOException ioe)
				    	{
				    		System.err.println("ERROR: Failed to send update to peer: "+p);
				    	}
			    	}
			    	else
			    	{
			    		System.err.println("ERROR: no known open connections to this peer!");
			    		System.err.println("TODO create new ones from address pool");
			    	}
			    }
		    break;
		}
    }
    
    private static void usage()
    {
		System.out.println("Usage: java Publisher <archive root>");
		System.exit(1);
    }
    
    public static void main (String[] args)
    {
		//get the folder we need to watch 
		try
		{
			if(args.length == 1)
			{
				Path root = Paths.get(args[0]);
				Publisher p = new Publisher(root, port);
			}
			/*else if (args.length == 0)
			{
				Publisher p = new Publisher(defaultRoot, port);
			}*/
			else
			{
				usage();
			}
		    
		}
		catch(InvalidPathException ipe)
		{
		    usage();
		}
    }
}

