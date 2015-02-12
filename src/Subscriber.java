import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.medavox.distribackup.connections.ConnectionOperator;
import com.medavox.distribackup.connections.ReceivedMessage;
import com.medavox.distribackup.filesystem.ArchiveInfo;
import com.medavox.distribackup.filesystem.FileDataChunk;
import com.medavox.distribackup.filesystem.FileInfo;
import com.medavox.distribackup.filesystem.FileInfoBunch;
import com.medavox.distribackup.peers.*;

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
			FileInfoBunch update = (FileInfoBunch)ua.getCommunicable();
			//update local archive and list of files we need
			globalArchiveState.update(update.getGRN(), update.getFiles());
			filesToDownload.update(update.getGRN(), update.getFiles());
			
			System.out.println("Received Update Announcement");
			
			//request new files
			//ask a different random peer for each file
			for(FileInfo fi : filesToDownload)
			{
				System.out.println("toDownload:"+filesToDownload.printAllFiles());
				if(!fi.isDirectory())
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
				else//it's a directory
				{//just make an empty directory
					String fsFile = root.toString()+sep+fi.getPath()+sep+fi.getName();
					try
					{
						Files.createDirectories(Paths.get(fsFile));
					}
					catch(IOException ioe)
					{
						ioe.printStackTrace();
					}
					//then remove this entry from filesToDownload
					
				}
			}
		}
    }
    
    public void handleFileRequest(ReceivedMessage fr)//TODO
    {  	//if we have the file and the right version,
	    	//construct a FileDataChunk for it
	    	//then send it to the relevant peer
    	//if we don't,
    		//send a NO_HAZ containing the FileInfo we were sent
    	
    	FileInfo fi = ((FileInfoBunch)fr.getCommunicable()).getFiles()[0];
    	System.out.println("Received File Request:"+fi);
    	
    	boolean hasFile = localArchiveState.containsKey(fi.toString());
    	FileInfo extantFileInfo = localArchiveState.getFileInfoWithPath(fi.toString());
    	boolean isRightVersion = (fi.getRevisionNumber() == extantFileInfo.getRevisionNumber());
    	
    	handleFileRequest(fr, hasFile, isRightVersion);
    }
    
    public void finishedDownloadingFile(FileInfo cfi)
    {
    	//fsw.ignoreList.update(cfi);
    	localArchiveState.update(cfi);
    	System.out.println("successfully removed "+cfi+" from toDownload:"+filesToDownload.remove(cfi));
    	System.out.println("toDownload:"+filesToDownload.printAllFiles());
    }
    
    public static void main (String args[])
    {//local listening port
		int port = defaultConnectPort;
		String host = defaultHost;
		Path initRoot = defaultRoot;
		switch(args.length)
		{
			case 3:
			    try
			    {
			    	port = Integer.parseInt(args[1]);
			    }
			    catch(NumberFormatException nfe)
			    {
					System.err.println("incorrect arguments. \""+args[1]+"\" is not a valid port number");
					usage();
			    }
			case 2:
			    port = defaultConnectPort;
			    host = args[1];
			
			case 1:
				initRoot = Paths.get(args[0]);
		}
		Subscriber p = new Subscriber(initRoot, host, port);
    }
    
    public static void usage()
    {
		System.out.println("Usage: java Subscriber <archive root> [Publisher Address] [port]");
		System.exit(1);
    }
    /**Subscriber user has edited files in error */
    public void fileChanged(Path file, String eventType)
    {
    	//System.err.println("ERROR: failed to ignore "+file);
    	//System.exit(0);
    	System.err.println("WARNING: Don't edit these files!");
		System.err.println("They are merely copies, your changes will be overwritten!");
		//replace inappropriately changed copies with fresh copies from the Publisher
		//request replacement copy from Publisher
		ConnectionOperator co = peers.get(publisherUUID).getOpenConnection();
		System.out.println("file:"+file+" event:"+eventType);
		Path relativePath = root.relativize(file);
		System.out.println("illegally changed file:"+relativePath.toString());
		FileInfo fi = localArchiveState.getFileInfo(relativePath.toString());//returns null?
		System.out.println("GAS files:"+globalArchiveState.printAllFiles());
		System.out.println("fi:"+fi);
		co.requestFile(fi);
    }
}
