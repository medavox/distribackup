//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.IOException;

import com.medavox.distribackup.connections.ConnectionOperator;
import com.medavox.distribackup.connections.ReceivedMessage;
import com.medavox.distribackup.filesystem.ArchiveInfo;
//import com.medavox.distribackup.filesystem.FileDataChunk;
import com.medavox.distribackup.filesystem.FileInfo;
import com.medavox.distribackup.filesystem.FileInfoBunch;
import com.medavox.distribackup.peers.*;

import java.net.ConnectException;
import java.nio.file.*;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**The main entry point for peers subscribed to updates from the Publisher.*/
public class Subscriber extends Peer
{
    private static String defaultHost = "127.0.0.1";
    private static int defaultConnectPort = 1210;
    private static int defaultListeningPort = 1211;
    private static Path defaultRoot = Paths.get("/home/scc/distribackup/subscriber-root");
    private ConcurrentMap<FileInfo, UUID> requestedFiles = new ConcurrentHashMap<FileInfo, UUID>();
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
				System.out.println("toDownload:"+filesToDownload);
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
    	System.out.println("toDownload:"+filesToDownload);
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
			    break;
			case 2:
			    port = defaultConnectPort;
			    host = args[1];
			    break;
			case 1:
				initRoot = Paths.get(args[0]);
				break;
			case 0:
				usage();
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
		System.out.println("GlobalArchiveState files:"+globalArchiveState);
		System.out.println("fi:"+fi);
		co.requestFile(fi);
    }
    private class FileBeggar implements Runnable
    {
    	//private internal classes can access parent class's variables anyway 
    	private Subscriber owner;
    	
    	public FileBeggar(Subscriber owner)
    	{
    		this.owner = owner;
    	}

    	@Override
    	public void run()
    	{
    		while(true)
    		{
    			try
    			{
    				wait(2000);
    			}
    			catch(InterruptedException ie)
    			{
    				ie.printStackTrace();
    			}
    			for(FileInfo fi : globalArchiveState.getFiles())
    	    	{
    	    		//if this file isn't in the localArchiveState, then we don't have it
    	    		if(!localArchiveState.contains(fi))
    	    		{
    	    			//request this file from a random peer
    	    			
    	    			//select random peer from known peers
    	    			PeerInfo[] peerArray = new PeerInfo[peers.size()];
    	    			peers.values().toArray(peerArray);
    	    			Random r = new Random();
    	    			PeerInfo pi = peerArray[r.nextInt()];
    	    			
    	    			ConnectionOperator co = null;
    	    			int peersTried = 0;
    	    			while(peersTried < peerArray.length)//keep looking for a connected or connectable 
    	    			{//peer who we can request the file from
	    	    			if(pi.hasOpenConnection())
	    	    			{
	    	    				co = pi.getOpenConnection();
	    	    				break;
	    	    			}
	    	    			else
	    	    			{//we have no open ConnectionOperators with this PeerInfo 
	    	    				try
	    	    				{
	    	    					co = pi.newConnection(owner);
	    	    					break;
	    	    				}
	    	    				catch(ConnectException ce)
	    	    				{//can't connect connect to this peer
	    	    					System.err.println("Could't connect to "+pi);
	    	    					System.err.println("Trying another peer...");
	    	    					peersTried++;
	    	    					continue;
	    	    				}
	    	    			}
    	    			}
    	    			if(co == null)
    	    			{
    	    				System.err.println("ERROR: couldn't connect to any other peers!");
    	    				System.err.println("Either this archive is empty, or your network connection is down.");
    	    				System.err.println("Fix the problem/ask someone to help fix the problem, and try again.");
    	    				System.exit(1);
    	    				
    	    			}
    	    			else
    	    			{
    	    				co.requestFile(fi);
    	    			}
    	    		}
    	    	}
    		}
    	}
    }
}
