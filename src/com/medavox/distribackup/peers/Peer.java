package com.medavox.distribackup.peers;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;

import com.medavox.distribackup.connections.*;
import com.medavox.distribackup.peers.*;
import com.medavox.distribackup.filesystem.*;

/* There must be a local state object which records open sockets, and 
 * which peers they pertain to*/
public abstract class Peer extends Thread
{/* PeerInfo				EXISTS
	Archive Status			EXISTS
	Request For Peers		EXISTS
	Request All Files		EXISTS
	File Data Chunk			DOING
	File Request			EXISTS
	Greeting				DONE
	Exit Announcement		EXISTS
	Archive Status Request	EXISTS
	Update Announcement		EXISTS
	"no haz" FileReq Reply	EXISTS
	PeerInfo Request		
	"haz nao" announcement
	Fresh Peers*/

	public static final int MAX_CHUNK_SIZE = 4194304;//4MB
	public static final int MIN_CHUNK_SIZE = 32768;//32KB
	protected int listenPort;
	//private String defaultRoot;//must be specified by subclass
	public Path root;
	protected String cacheDir = ".distribackup-cache";
	
	PeerInfo publisherInfo;
	protected UUID publisherUUID;
	public static UUID myUUID;
    
	protected ConcurrentMap<UUID, PeerInfo> peers = new ConcurrentHashMap<UUID, PeerInfo>();
	
	protected Queue<ReceivedMessage> messageQueue = new ConcurrentLinkedQueue<ReceivedMessage>();
    
    protected ArchiveInfo filesToDownload = new ArchiveInfo(-1, new FileInfo[0]);
    
    protected ArchiveInfo globalArchiveState = new ArchiveInfo( -1, new FileInfo[0]);//initialise empty, then get state from network
	
	public static final short version = 2;//increment this manually on every release
	
	public Peer(Path root, int port)
	{
		this.root = root;
		this.listenPort = port;
		myUUID = UUID.randomUUID();//this uniquely IDs us on the network
		Listener listenHook = new Listener(port, this);
		try
		{
			FilesystemWatcher fsw = new FilesystemWatcher(root, this);
			fsw.start();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
		listenHook.start();
	}
	/**Called by FilesystemWatcher whenever a filesystem change is detected*/
	public abstract void fileChanged(Path file, String eventType);
	
	/**Called by Listener's thread whenever someone connects to the ServerSocket
	 * and creates a new Socket.*/
	public void setupNewSocket(Socket s)
	{
		try
		{
			ConnectionOperator co = new ConnectionOperator(s, this);
			int handshook = co.checkVersions();
			System.out.println("incoming connection from "+s.getInetAddress());
			
			if(handshook == -1)
			{
				System.err.println("Error! Connecting Peer "+s.getInetAddress()+
                    " has wrong version!");
				co.close();
				return;
			}
			
			UUID newPeerUUID = co.exchangeUUIDs();
			
			if(!peers.containsKey(newPeerUUID))
			{//if we've not seen this Peer before:
				//ask for its PeerInfo object.
				//It will be received by the Incoming Message Processing Thread,
				//and added to the peers list
				co.requestPeerInfo();
			}
			else
			{//we've already seen this Peer before
				//add this new Connection info to its of connections
			}
			//TODO:
			//construct a new PeerInfo based on data requested and received
			//check it with the local PeerInfo store
			
			
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	protected FileInfo pathToFileInfo(Path p, long revisionNumber)
	{
		Path relativePath = root.relativize(p);
		System.out.println("passed path:   "+p);
		System.out.println("relative path: "+relativePath);
    	String name = relativePath.getFileName().toString();
    	Path relPath = relativePath.getParent();//use an empty string if the path is null
    	String path = (relPath == null ? "" : relPath.toString());
    	
    	File asFile = p.toFile();
    	FileInfo newFile = new FileInfo(name, path);//if it's a directory
		
    	if(!asFile.isDirectory())
    	{//is a file, so use the Filewise constructor for FileInfo
			try
			{
				long fileSize = Files.size(p);
		
		    	byte[] checksum = FileUtils.checksum(asFile);
		    	newFile = new FileInfo(name, path, fileSize, revisionNumber, checksum);
			} 
			catch (IOException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
    	}
    	return newFile;
	}
	
	/**TODO:
	 store 1 bis-bos pair per socket entry
		or create one when a socket is referenced from the hashmap*/
	public void connect(String host, int port)
	{
		try
		{
			Socket s = new Socket(host, port);
			setupNewSocket(s);
		}
		catch(UnknownHostException uhe)
		{
			//TODO
		}
		catch(IOException ioe)
		{
			//TODO
		}
	}
	/**The Incoming Message Processing Thread (IMPT).*/
    public void run()
    {
        while(true)
        {//spins until there is something in the queue
            if(!messageQueue.isEmpty())
            {
                ReceivedMessage next = messageQueue.remove();
                //TODO: probably a big old switch statement
                switch(next.getType())
                {/* PeerInfo				DONE
					Archive Status			DONE
					Request For Peers		DONE
					Request All Files		DONE
					File Data Chunk			DONE
					File Request			DONE
					Greeting				N/A
					Exit Announcement		DONE
					Archive Status Request	DONE
					Update Announcement		DONE
					"no haz" FileReq Reply	DONE
					PeerInfo Request
					"haz nao" announcement	DONE
					More Peers*/
					
					case PEER_INFO:
						receivePeerInfo(next);
					break;
					
					case ARCHIVE_STATUS:
						receiveArchiveStatus(next);
					break;
					
					case REQ_FOR_PEERS:
                        handleRequestForPeers(next);
                    break;
                    
                    case REQ_ALL_FILES:
                        handleAllFilesRequest(next);
                    break;
					
                    case FILE_DATA_CHUNK:
                        receiveFileDataChunk(next);
                    break;
                    
                    case FILE_REQUEST:
                        handleFileRequest(next);
                    break;
                    //greeting is implemented differently; not here
                    case EXIT_ANNOUNCE:
                        receiveExitAnnouncement(next);
                    break;
                    
                    case TREE_STATUS_REQ:
                        handleArchiveStatusRequest(next);
                    break;
                    
                    case UPDATE_ANNOUNCE:
                        receiveUpdateAnnouncement(next);
                    break;
                    
                    case NO_HAZ:
						receiveNoHaz(next);
					break;
					
                    case PEER_INFO_REQ:
                    	handlePeerInfoRequest(next);
                    break;
					
					case HAZ_NAO:
						receiveAcquisitionAnnouncement(next);
					break;
					
					case MORE_PEERS:
						receiveMorePeers(next);
					break;
                }
            }
        }
    }
    private void handlePeerInfoRequest(ReceivedMessage next)
    {
		// TODO Auto-generated method stub
		
	}
	private void receiveMorePeers(ReceivedMessage next)
	{
		// TODO Auto-generated method stub
		
	}
	/**Waits until we have the whole update before pushing files to the visible copy.
	Updates should be relatively small anyway, and this prevents broken half-states from occurring*/
    public void receiveFileDataChunk(ReceivedMessage rxmsg)
    {
    	
        //check file exists
        //if there are more files or pieces on the way before this update is finished,
        //store them in some kind of cache
        FileDataChunk fdc = (FileDataChunk)rxmsg.getCommunicable();
        System.out.println("received file data for \""+fdc.getFileInfo().getName()+"\"");
        if(fdc.isWholeFile())
        {//we have the whole file,
			//do we now have all pieces of the update?
        	//we're not doing per-revision pushes anymore
            /*FileInfo fi = fdc.getFileInfo();
            filesToDownload.remove(fi.toString());
            if(filesToDownload.getSize() == 0)//if 
            {
            	
            }*/
        	//push the file into the archive
        	FileInfo fi = fdc.getFileInfo();
        	String sep = FileSystems.getDefault().getSeparator();
        	Path newPath = root.resolve(fi.getPath()+sep+fi.getName());//create path to file
        	File newFile = newPath.toFile();
        	System.out.println("created path: "+newFile);
        	try
        	{
        		if(newFile.exists())
        		{
        			newFile.delete();
        		}
        		newFile.createNewFile();
        		FileOutputStream fos = new FileOutputStream(newFile);
        		fos.write(fdc.getPayload());
        		fos.flush();
        		fos.close();
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        		System.exit(1);
        	}
        	
        }
        else
        {//store this piece of the file in the cache until we have all the pieces of the update
            
        }
        
    }
    
    public void handleRequestForPeers(ReceivedMessage pr)
    {
        
    }
    
    public void receivePeerInfo(ReceivedMessage pi)
    {
		PeerInfo peerInfo = (PeerInfo)pi.getCommunicable();
		UUID uuid = pi.getUUID();
		
		peers.containsKey(uuid);
	}
    
    public void handleFileRequest(ReceivedMessage fr)
    {
        
    }
    
    public void handleAllFilesRequest(ReceivedMessage afr)
    {
        
    }
    
    public void receiveExitAnnouncement(ReceivedMessage ea)
    {
        
    }
    
    public void handleArchiveStatusRequest(ReceivedMessage ftsr)
    {
        
    }
    
    public void receiveArchiveStatus(ReceivedMessage as)
    {
		
	}
	
	public void receiveNoHaz(ReceivedMessage nh)
	{
		
	}
	
	public void receiveAcquisitionAnnouncement(ReceivedMessage aa)
	{
		
	}
    
    public void receiveUpdateAnnouncement(ReceivedMessage ua)
    {//do some basic validation to detect peers spoofing as publisher
        if(publisherUUID.equals(myUUID) //if WE are the publisher
        || !(publisherUUID.equals(ua.getUUID()) ))//or their UUID is not the Publisher's
        {
			//someone is pretending to be the Publisher!
		}
		else
		{//everything is fine, no spoofing here
			//get list of changed files
			//get the ArchiveInfo from the Update Announcement
			
		}
    }
    
    public void addToQueue(ReceivedMessage rxmsg)
    {
        messageQueue.add(rxmsg);
    }
}
