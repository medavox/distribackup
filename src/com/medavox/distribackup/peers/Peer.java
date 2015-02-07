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
{
	private final int MAX_CHUNK_SIZE = 4194304;//4MB
	private final int MIN_CHUNK_SIZE = 32768;//32KB
	private int listenPort;
	private String defaultRoot;//must be specified by subclass
	public Path root;
	Map<UUID, PeerInfo> peers = new ConcurrentHashMap<UUID, PeerInfo>();
	PeerInfo publisherInfo;
	UUID publisherUUID;
	public static UUID myUUID;
    private Queue<ReceivedMessage> messageQueue = new ConcurrentLinkedQueue<ReceivedMessage>();
    private ConcurrentMap<String, FileInfo> filesToDownload = new ConcurrentHashMap<String, FileInfo>();
	
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
				co.sendPeerInfoRequest();
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
                {/* PeerInfo
					Archive Status
					Request For Peers
					Request All Files
					File Data Chunk
					File Request
					Greeting
					Exit Announcement
					Archive Status Request
					Update Announcement
					"no haz" FileReq Reply
					PeerInfo Request
					"haz nao" announcement*/
					
					case PEER_INFO:
						receivePeerInfo(next);
					break;
					
					case ARCHIVE_STATUS:
						receiveArchiveStatus(next);
					break;
					
					case TREE_STATUS_REQ:
                        handleArchiveStatusRequest(next);
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
                        handleExitAnnouncement(next);
                    break;
                    
                    case UPDATE_ANNOUNCE:
                        receiveUpdateAnnouncement(next);
                    break;
                    
                    case NO_HAZ:
						receiveNoHaz(next);
					break;
					
					case HAZ_NAO:
						receiveAcquisitionAnnouncement(next);
					break;
                }
            }
        }
    }
    /**Waits until we have the whole update before pushing files to the visible copy.
	Updates should be relatively small anyway, and this prevents broken half-states from occurring*/
    public void receiveFileDataChunk(ReceivedMessage rxmsg)
    {
        //check file exists
        //if there are more files or pieces on the way before this update is finished,
        //store them in some kind of cache
        FileDataChunk fdc = (FileDataChunk)rxmsg.getCommunicable();
        if(fdc.isWholeFile())
        {//we have the whole file, do we now have all pieces of the update?
			
            
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
		
	}
    
    public void handleFileRequest(ReceivedMessage fr)
    {
        
    }
    
    public void handleAllFilesRequest(ReceivedMessage afr)
    {
        
    }
    
    public void handleExitAnnouncement(ReceivedMessage ea)
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
