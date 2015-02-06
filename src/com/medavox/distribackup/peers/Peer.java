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
	private int listenPort;
	private String defaultRoot = "/home/scc/distribackup/publisher-root";
	public Path root;
	Map<UUID, PeerInfo> peers = new ConcurrentHashMap<UUID, PeerInfo>();
	PeerInfo publisherInfo;
	UUID publisherID;
	public static UUID myUUID;
	private static IncomingMessageProcessor IMP = null;
    private Queue<ReceivedMessage> messageQueue = new ConcurrentLinkedQueue<ReceivedMessage>();
	
	public static final short version = 1;//increment this manually on every release
	List<ConnectionOperator> openConnections = new ArrayList<ConnectionOperator>();//may need to become concurrent
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
			ConnectionOperator co = new ConnectionOperator(s);
			int handshook = co.checkVersions();
			System.out.println("incoming connection from "+s.getInetAddress());
			
			if(handshook == -1)
			{
				System.err.println("Error! Connecting Peer "+s.getInetAddress()+
                    " has wrong version!");
				co.close();
				return;
			}
			
			UUID newPeerUUID = co.exchangeGreetings();
			
			if(!peers.containsKey(newPeerUUID))
			{//ERROR:
				//how do we get the GRN of a peer we've just connected to,
				//in order to create a new PeerInfo about it?
				co.sendPeerInfoRequest();
				//now we've sent out for the PerInfo we need,
				//we have to wait, or handle adding to peers List in a callback
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

    public void run()
    {
        while(true)
        {//spins until there is something in the queue
            if(!messageQueue.isEmpty())
            {
                ReceivedMessage next = messageQueue.remove();
                //TODO: probably a big old switch statement
                switch(next.getType())
                {/* Request For Peers
                    Request All Files
                    File Data Chunk
                    File Request
                    Greeting
                    Exit Announcement
                    File Tree Status Req
                    Update Announcement*/
                    case FILE_DATA_CHUNK:
                        handleFileDataChunk(next);
                    break;
                    
                    case FILE_REQUEST:
                        handleFileRequest(next);
                    break;
                    
                    case PEER_REQUEST:
                        handlePeerRequest(next);
                    break;
                    
                    case EXIT_ANNOUNCEMENT:
                        handleExitAnnouncement(next);
                    break;
                    
                    case REQ_ALL_FILES:
                        handleAllFilesRequest(next);
                    break;
                    
                    case TREE_STATUS_REQ:
                        handleFileTreeStatusRequest(next);
                    break;
                    
                    case UPDATE_ANNOUNCE:
                        handleUpdateAnnouncement(next);
                    break;
                }
            }
        }
    }
    
    public void handleFileDataChunk(ReceivedMessage rxmsg)
    {
        //check file exists
        //if there are more pieces on the way before this update is finished,
        //store them in some kind of cache
        FileDataChunk fdc = (FileDataChunk)rxmsg.getCommunicable();
        if(fdc.isWholeFile())
        {//we have the whole file, check whether we have the whole UPDATE
            
        }
        else
        {
            
        }
        
    }
    
    public void handlePeerRequest(ReceivedMessage pr)
    {
        
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
    
    public void handleFileTreeStatusRequest(ReceivedMessage ftsr)
    {
        
    }
    
    public void handleUpdateAnnouncement(ReceivedMessage ua)
    {//do some basic validation to detect peers spoofing as publisher
        if(this instanceof Publisher //if WE are the publisher
        || !(publisherUUID.equals(ua.getUUID()) ))//or their UUID is not the Publisher's
        {
			
		}
		else
		{//everything is fine, no spoofing here
			make TreeStatus into a list ofFileInfos
			give FileInfos a isDirectory:boolean property
		}
    }
    
    public void addToQueue(ReceivedMessage rxmsg)
    {
        messageQueue.add(rxmsg);
    }
}
