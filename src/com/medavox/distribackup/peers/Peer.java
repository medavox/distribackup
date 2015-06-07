package com.medavox.distribackup.peers;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
	public static Path root;
	protected String cacheDir = ".distribackup-cache";
	protected String sep = FileSystems.getDefault().getSeparator();
	
	PeerInfo publisherInfo;
	protected UUID publisherUUID;
	public static UUID myUUID;
	public boolean threadsEnabled = true;
    
	protected FileSystemWatcher fsw;
	
	protected ConcurrentMap<UUID, PeerInfo> peers = new ConcurrentHashMap<UUID, PeerInfo>();
	protected Queue<ReceivedMessage> messageQueue = new ConcurrentLinkedQueue<ReceivedMessage>();
    protected ArchiveInfo filesToDownload = new ArchiveInfo(-1, new FileInfo[0]);
    public ArchiveInfo globalArchiveState = new ArchiveInfo( 0, new FileInfo[0]);//initialise empty, then get state from network
	
	public static final short version = 2;//increment this manually on every release
	
    /**The main program core. Contains all functionality common to both the Publisher and 
     * Subscriber, which is most of the network communication logic (both incoming message
     * processing and sending messages), setup and initialisation of most data structures,
     * and various global constants and values, such as protocol version, and archive directory
     * location */
	public Peer(Path root, int port)
	{
		Peer.root = root;
		this.listenPort = port;
		Peer.myUUID = UUID.randomUUID();//this uniquely IDs us on the network
		Listener listenHook = new Listener(port, this);
		Runtime.getRuntime().addShutdownHook(new CloseHook(this));
		try
		{
			fsw = new FileSystemWatcher(root, this);
			fsw.start();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
		listenHook.start();
		this.start();//start Incoming Message Processing Thread
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
			
			UUID newPeerUUID = co.exchangeUUIDs();
			//after boilerplate is done, start listening thread
			co.start();
			if(!peers.containsKey(newPeerUUID))
			{//if we've not seen this Peer before:
				//ask for its PeerInfo object.
				//It will be received by the Incoming Message Processing Thread,
				//and added to the peers list
				System.out.println("We've not seen this peer before!");
				co.requestPeerInfo();
			}
			else
			{//we've already seen this Peer before
				//add this new Connection info to its connections pile
				System.out.println("We already know this peer.");
				peers.get(newPeerUUID).addConnection(co);
			}
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
			uhe.printStackTrace();
			//TODO
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			//TODO
		}
	}
	
	public boolean isPublisher()
	{
		return myUUID.equals(publisherUUID);
	}
	
	/**The Incoming Message Processing Thread (IMPT).*/
    public void run()
    {
        while(threadsEnabled)
        {//spins until there is something in the queue
            if(!messageQueue.isEmpty())
            {
                ReceivedMessage next = messageQueue.remove();
                //TODO: probably a big old switch statement
                switcheroo:
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
					break switcheroo;
					
					case ARCHIVE_STATUS:
						receiveArchiveStatus(next);
					break switcheroo;
					
					case REQ_FOR_PEERS:
                        handleRequestForPeers(next);
                    break switcheroo;
                    
                    case REQ_ALL_FILES:
                        handleAllFilesRequest(next);
                    break switcheroo;
					
                    case FILE_DATA_CHUNK:
                        receiveFileDataChunk(next);
                    break switcheroo;
                    
                    case FILE_REQUEST:
                        handleFileRequest(next);
                    break switcheroo;
                    //greeting is implemented differently; not here
                    case EXIT_ANNOUNCE:
                        receiveExitAnnouncement(next);
                    break switcheroo;
                    
                    case TREE_STATUS_REQ:
                        handleArchiveStatusRequest(next);
                    break switcheroo;
                    
                    case UPDATE_ANNOUNCE:
                        receiveUpdateAnnouncement(next);
                    break switcheroo;
                    
                    case NO_HAZ:
						receiveNoHaz(next);
					break switcheroo;
					
                    case PEER_INFO_REQ:
                    	handlePeerInfoRequest(next);
                    break switcheroo;
					
					case HAZ_NAO:
						receiveAcquisitionAnnouncement(next);
					break switcheroo;
					
					case MORE_PEERS:
						receiveMorePeers(next);
					break switcheroo;
					
					default:
						System.err.println("ERROR: Received unknown Message:"+next.getType());
						System.exit(1);
                }
            }
            else//wait before checking again, so as not to waste CPU cycles
            {
            	try
            	{
            		sleep(200);
            	}
            	catch(InterruptedException ie)
            	{
            		System.err.println("Can't a thread catch a few ms sleep around here?!");
            		ie.printStackTrace();
            	}
            }
        }
    }
    public void handlePeerInfoRequest(ReceivedMessage next)//TODO
    {
    	System.out.println("Received PeerInfo Request");
    	ConnectionOperator co = next.getConnection();
    	try
    	{
    		co.sendPeerInfo();
    	}
    	catch(IOException ioe)
    	{
    		System.err.println("FAILED to send Peer Info!");
    		ioe.printStackTrace();
    	}
	}
	public void receiveMorePeers(ReceivedMessage next)//TODO
	{
		
	}
	
	public abstract void finishedDownloadingFile(FileInfo cfi);
	
	/**Waits until we have the whole update before pushing files to the visible copy.
	Updates should be relatively small anyway, and this prevents broken half-states from occurring*/
    public void receiveFileDataChunk(ReceivedMessage rxmsg)//TODO
    {
        //check file exists
        //if there are more files or pieces on the way before this update is finished,
        //store them in some kind of cache
        FileDataChunk fdc = (FileDataChunk)rxmsg.getCommunicable();
        System.out.println("received file data for \""+fdc.getFileInfo().getName()+"\"");
        if(fdc.isWholeFile())
        {//we have the whole file,
			//do we now have all pieces of the update?
        	//we're not doing per-revision fs pushes anymore
            /*FileInfo fi = fdc.getFileInfo();
            filesToDownload.remove(fi.toString());
            if(filesToDownload.getSize() == 0)//if 
            {
            	
            }*/
        	//push the file into the archive
        	FileInfo fi = fdc.getFileInfo();
        	String sep = FileSystems.getDefault().getSeparator();
        	//Path fsRoot = Paths.get("/");
        	//Path newPath = fsRoot.resolve(fi.getPath()+sep+fi.getName());//create path to file
        	//System.out.println("Path object:"+newPath);
        	//File newFile = newPath.toFile();
        	File newFile = new File(root+sep+fi.getPath()+sep+fi.getName());
        	System.out.println("created entry: "+newFile);
        	try
        	{
        		//ignore changes to this file while we create/update it
        		//System.out.println("adding "+fi+" to ignore list");
        		fsw.ignoreList.add("ENTRY_CREATE:"+fi.toString());
        		fsw.ignoreList.add("ENTRY_MODIFY:"+fi.toString());
        		if(newFile.exists())
        		{
        			fsw.ignoreList.add("ENTRY_DELETE:"+fi.toString());
        			newFile.delete();
        		}
        		newFile.createNewFile();
        		FileOutputStream fos = new FileOutputStream(newFile);
        		fos.write(fdc.getPayload());
        		fos.flush();
        		fos.close();
        		finishedDownloadingFile(fi);//subclass callback to deal with download
        	}
        	catch(Exception e)
        	{
        		e.printStackTrace();
        		System.exit(1);
        	}
        }
        else
        {//store this piece of the file in the cache until we have all the pieces of the update
            //TODO
        }
    }
    
    /**Assumes:- 
     * The requested file exists (we have the file and the right version),
     * The requested file is appropriately a file,
     * The requested file is the right version.
     * It's the responsibility of the caller to make sure these are true.*/
    public FileDataChunk getFileDataChunk(FileInfo fi, int pieceNum)//TODO:loads entire file into memory!
    {
    	File fsFile = new File(root.toString()+sep+fi.getPath()+sep+fi.getName());
    		
    	assert !fi.isDirectory() && !fsFile.isDirectory();
		//if the file is small enough, send the whole thing
		if(fsFile.length() <= MAX_CHUNK_SIZE)
		{
			if(pieceNum != 0)
			{
				throw new ArrayIndexOutOfBoundsException(
						"this file doesn't have that many pieces!");
			}
			try(FileInputStream fis = new FileInputStream(fsFile))
			{
				int lengthAsInt = (int)fsFile.length();
				byte[] payload = new byte[lengthAsInt];
				fis.read(payload, 0, lengthAsInt);
				return new FileDataChunk(fi, payload, 0);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}
		else
		{//otherwise, send chunks of max size Peer.MAX_CHUNK_SIZE
			int minChunks = (int)Math.ceil(fsFile.length() / MAX_CHUNK_SIZE);
			if(pieceNum >= minChunks)
			{
				throw new ArrayIndexOutOfBoundsException(
						"this file doesn't have that many pieces!");
			}
			//we need to address file positions past 2GB in order to read them,
			//which is impossible with FileInputStream's offset:int.
			//so we need a read method which has an offset:long
			//thus, the use of ByteBuffer
			
			//if this is the last chunk, make the readLength only up to the last byte
			long offset = (long)pieceNum*(long)MAX_CHUNK_SIZE;
			int readLength = (pieceNum==minChunks-1 ? (int)(fsFile.length()-offset) : MAX_CHUNK_SIZE);
			
			Path path = fsFile.toPath();
			try(FileChannel fc = FileChannel.open(path, StandardOpenOption.READ))
			{
				ByteBuffer bb = ByteBuffer.allocate(readLength);
				fc.read(bb, offset);
				
				if(bb.hasArray())
				{
					byte[] payload = bb.array();
					return new FileDataChunk(fi, payload, offset);
				}
				else
				{
					System.err.println("There's no backing array for the byte buffer!");
					System.exit(1);
				}
				
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
				System.exit(1);
			}
		}
		//should never reach here
		System.err.println("Reached impossible state!");
		System.exit(1);
		return null;//stupid compiler
    }
    
    public abstract void handleFileRequest(ReceivedMessage fr);
    
    protected void handleFileRequest(ReceivedMessage fr, boolean hasFile, boolean isRightVersion)
    {
    	FileInfo fi = ((FileInfoBunch)fr.getCommunicable()).getFiles()[0];
    	//System.out.println("requested file: "+fi);
    	if(hasFile && isRightVersion)
    	{//construct a FileDataChunk
    		
    		File fsFile = new File(root.toString()+sep+fi.getPath()+sep+fi.getName());
    		System.out.println("as File:"+fsFile);
    		System.out.println("we have version "+fi.getRevisionNumber()+" of \""+fi.getName()+"\"!");
    		//check the file exists and is what it's meant to be (file/directory)
    		if(fsFile.exists())
    		{
    			//System.out.println("\""+fi.getName()+"\" exists!");
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
					//System.out.println("It's a file when it should be");
					if(fsFile.length() ==0)
					{
						System.out.println("\""+fi.getName()+"\" is zero-length");
					}
					if(fsFile.length() <= MAX_CHUNK_SIZE)
					{
						FileDataChunk fdc = getFileDataChunk(fi, 0);
						System.out.println("Sending whole file as one chunk...");
						co.sendFileDataChunk(fdc);
					}
					else
					{
						int minChunks = (int)Math.ceil(fsFile.length() / MAX_CHUNK_SIZE);
						System.out.println("File will be sent in "+minChunks+" chunks");
						for(int i = 0; i < minChunks; i++)
						{
							System.out.println("Sending chunk "+i+"...");
							FileDataChunk fdc = getFileDataChunk(fi, i);
							co.sendFileDataChunk(fdc);
						}	
					}
    			}
    			else//either:  
    			{//TODO:ERRORS
    				System.err.println("ERROR: either:-\n"+
    			"FileInfo is File, but filesystem is Dir; or"+
    				"FileInfo is Dir, but filesystem is File");
    				//FileInfo is File, but filesystem is Dir; or
    				//FileInfo is Dir, but filesystem is File
    			}
    		}
    		else
    		{
    			System.err.println("file doesn't exist, apparently");
    		}
    	}
    	else//we don't have it
    	{//send a NO_HAZ with the FileInfo they gave us attached
    		System.out.println("we don't have version "+fi.getRevisionNumber()+" of \""+fi.getName()+"\"!");
    		ConnectionOperator co = fr.getConnection();
    		FileInfo[] unavailableFiles = {fi};
    		co.sendNoHazFile(unavailableFiles);
    	}	
    }
    
    public void handleRequestForPeers(ReceivedMessage pr)
    {
        ConnectionOperator co = pr.getConnection();
        PeerInfo[] knownPeers = new PeerInfo[peers.size()];
        peers.values().toArray(knownPeers);
        try
        {
        	co.sendMorePeers(knownPeers);
        }
        catch(IOException ioe)
        {
        	ioe.printStackTrace();
        	System.err.println("flippin' donkey bashers");
        }
    }
    
    public void receivePeerInfo(ReceivedMessage pi)//TODO
    {
    	System.out.println("Received new PeerInfo");
		PeerInfo peerInfo = (PeerInfo)pi.getCommunicable();
		UUID uuid = pi.getUUID();
		
		if(peerInfo.isPublisher())
		{
			System.out.println("Received PeerInfo is Publisher's");
			if(publisherUUID == null)
			{//we've just found the publisher
				//add it in!
				publisherUUID = uuid;
			}
		}
		
		if(peers.containsKey(uuid))
		{
			System.out.println("TODO: known peer, update existing record");
			//TODO
		}
		else
		{
			System.out.println("adding new PeerInfo to list of known peers...");
			if(peers.isEmpty())
			{
				System.out.println("This is our first peer!");
				//TODO: send a allFileRequest, or maybe just an archiveStateRequest
				System.out.println("Requesting archive state...");
				ConnectionOperator co = pi.getConnection();
				try
				{
					co.requestArchiveStatus();
				}
				catch(IOException ioe)
				{
					System.err.println("ERROR: Failed to request archive status!");
					ioe.printStackTrace();
					System.exit(1);
				}
			}
			peerInfo.addConnection(pi.getConnection());//add this connection 
			peers.putIfAbsent(uuid, peerInfo);
		}
	}

    public void handleAllFilesRequest(ReceivedMessage afr)//TODO
    {
        
    }
    
    public void receiveExitAnnouncement(ReceivedMessage ea)//TODO
    {
    	System.out.println("Received Exit Announcement");
        //close the connection we received this on,
    	//and remove it from the PeerInfo's list of open connections
    	ConnectionOperator co = ea.getConnection();
    	PeerInfo p = peers.get(ea.getUUID()); 
    	p.removeConnection(co);
    	
    	try
    	{
    		co.close();
    	}
    	catch(IOException ioe)
    	{
    		System.err.println("ERROR: Failed to closed Connection for peer "+p);
    		ioe.printStackTrace();
    	}
    	
    }
    
    public void handleArchiveStatusRequest(ReceivedMessage ftsr)
    {
        ConnectionOperator co = ftsr.getConnection();
        try
        {
        	co.sendArchiveStatus();
        }
        catch(IOException ioe)
        {
        	ioe.printStackTrace();
        }
    }
    
    public void receiveArchiveStatus(ReceivedMessage as)//TODO
    {//for any files we don't have (or don't have the latest version of),
    	//request them from any peer
    	//expect 1 or more of these requests to be replied with NO HAZ
    	FileInfoBunch fib = (FileInfoBunch)as.getCommunicable();
    	if(fib.getGRN() > globalArchiveState.getGRN())
    	{//replace our outdated globalArchiveInfo
    		globalArchiveState = fib.toArchiveInfo();//and convert it to ArchiveInfo
    	}
    	else
    	{
    		System.err.println("WARNING: received global archive info "
    				+"isn't newer than ours)"/* from "+	peers.get(as.getUUID())*/);
    		System.err.println("Our Global Revision number: "+globalArchiveState.getGRN());
    		System.err.println("Their Global Revision num : "+fib.getGRN());
    	}
	}
	
	public void receiveNoHaz(ReceivedMessage nh)//TODO: implement
	{//TODO: what to do when NO-ONE has a file you need
    	//(reconsider your life choices)
		
	}
	
	public void receiveAcquisitionAnnouncement(ReceivedMessage aa)//TODO
	{
		
	}
    
    public abstract void receiveUpdateAnnouncement(ReceivedMessage ua);
    
    public void addToQueue(ReceivedMessage rxmsg)
    {
        messageQueue.add(rxmsg);
    }
}
