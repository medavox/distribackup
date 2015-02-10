import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

import com.medavox.distribackup.connections.ConnectionOperator;
import com.medavox.distribackup.connections.ReceivedMessage;
import com.medavox.distribackup.filesystem.ArchiveInfo;
import com.medavox.distribackup.filesystem.FileDataChunk;
import com.medavox.distribackup.filesystem.FileInfo;
import com.medavox.distribackup.filesystem.UpdateAnnouncement;
import com.medavox.distribackup.peers.*;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
public class Subscriber extends Peer
{
    private static String defaultHost = "127.0.0.1";
    private static int defaultConnectPort = 1210;
    private static int defaultListeningPort = 1211;
    private static Path defaultRoot = Paths.get("/home/scc/distribackup/subscriber-root");
    
    private ArchiveInfo localArchiveState = new ArchiveInfo( -1, new FileInfo[0]);
    private ArchiveInfo toDownload = new ArchiveInfo(-1, new FileInfo[0]);
    
    //private String defaultRoot = "subscriber-root";
    public Subscriber(Path root, String host, int port)
    {
    	super(root, 1211);
	
        connect(host, port);
    }
    
    public void receiveUpdateAnnouncement(ReceivedMessage ua)
    {//do some basic validation to detect peers spoofing as publisher
        if(!(publisherUUID.equals(ua.getUUID()) ))//if their UUID is not the Publisher's
        {
        	PeerInfo pi = peers.get(ua.getUUID());
			//someone is pretending to be/thinks it's the Publisher!
        	System.err.println("WARNING: Peer \""+pi+
        			"\" is pretending to be the Publisher!");
		}
		else
		{//everything is fine, no spoofing here
			//get list of changed files
			//get the ArchiveInfo from the Update Announcement
			UpdateAnnouncement update = (UpdateAnnouncement)ua.getCommunicable();
			//update local archive and list of files we need
			globalArchiveState.update(update.getGRN(), update.getFiles());
			filesToDownload.update(update.getGRN(), update.getFiles());
			
			System.out.println("Received Update Announcement");
			
			//request new files
			//ask a different random peer for each file
			for(FileInfo fi : filesToDownload)
			{
				//get a random peer who has an open connection with us
				//TODO: try to open a connection rather than move on
				PeerInfo randomPeer;
				do
				{
					
					Random r = new Random();
					Set<UUID> keys = peers.keySet();
					UUID[] uuids = new UUID[keys.size()];
					keys.toArray(uuids);
					UUID randomID = uuids[r.nextInt(uuids.length)];
					randomPeer = peers.get(randomID);
				}while(!randomPeer.hasOpenConnection());
				//request the file
				randomPeer.getOpenConnection().requestFile(fi);
			}
		}
    }
    
    public void handleFileRequest(ReceivedMessage fr)//TODO
    {  	//if we have the file and the right version,
	    	//construct a FileDataChunk for it
	    	//then send it to the relevant peer
    	//if we don't,
    		//send a NO_HAZ containing the FileInfo we were sent
    	
    	FileInfo fi = (FileInfo)fr.getCommunicable();
    	System.out.println("Received File Request:"+fi);
    	
    	boolean hasFile = localArchiveState.containsKey(fi.toString());
    	FileInfo extantFileInfo = localArchiveState.getFileInfoWithPath(fi.toString());
    	boolean isRightVersion = (fi.getRevisionNumber() == extantFileInfo.getRevisionNumber());
    	
    	if(hasFile && isRightVersion)
    	{//construct a FileDataChunk
    		File fsFile = new File(fi.toString());
    		
    		//check the file exists and is what it's meant to be (file/directory)
    		if(fsFile.exists())
    		{
    			if(fi.isDirectory()
    			&& fsFile.isDirectory())
    			{
    				//nobody should request a directory
    				//just create one with the right name!
    				System.err.println("someone tried to request a directory!");
    				//although this could be extended later to mean a
    				//request of its CONTENTS...
    			}
    			else if(!fi.isDirectory()
    			&& !fsFile.isDirectory())
    			{
					ConnectionOperator co = fr.getConnection();
					//break file into 1 or more chunks to send
					
					//TODO: this loads the entire file into memory!
					FileDataChunk[] fdcs = createFileDataChunks(fi);
					
					for(int i = 0; i < fdcs.length; i++)
					{
						co.sendFileDataChunk(fdcs[i]);
					}
    			}
    			else//either:  
    			{//TODO:ERRORS
    				//FileInfo == File, but filesystem == Dir
    				//FileInfo == Dir, but filesystem == File
    			}
    		}
    	}
    	else//we don't have it
    	{//send a NO_HAZ with the FileInfo they gave us attached
    		ConnectionOperator co = fr.getConnection();
    		FileInfo[] unavailableFiles = {fi};
    		co.sendNoHazFile(unavailableFiles);
    	}
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
		//replace inappropriately changed copies with fresh copies from the Publisher
		//request replacement copy from Publisher
		ConnectionOperator co = peers.get(publisherUUID).getOpenConnection();
		FileInfo fi = globalArchiveState.getFileInfoWithPath(file);
		co.requestFile(fi);
    }
}
