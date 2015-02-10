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
			//after boilerplate is done, start listening thread
			co.start();
			this.start();//start Incoming Message Processing Thread
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
                    	System.out.println("plip plop");
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
        }
    }
    public void handlePeerInfoRequest(ReceivedMessage next)//TODO
    {
    	System.out.println("Received PeerInfo Request");
	}
	public void receiveMorePeers(ReceivedMessage next)//TODO
	{
		
	}
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
		File fsFile = new File(fi.toString());
    		
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
    	FileInfo fi = (FileInfo)fr.getCommunicable();
    	
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
					
					if(fsFile.length() <= MAX_CHUNK_SIZE)
					{
						FileDataChunk fdc = getFileDataChunk(fi, 0);

						co.sendFileDataChunk(fdc);
					}
					else
					{
						int minChunks = (int)Math.ceil(fsFile.length() / MAX_CHUNK_SIZE);
						for(int i = 0; i < minChunks; i++)
						{
							FileDataChunk fdc = getFileDataChunk(fi, i);
							co.sendFileDataChunk(fdc);
						}	
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
    
    public void handleRequestForPeers(ReceivedMessage pr)//TODO
    {
        
    }
    
    public void receivePeerInfo(ReceivedMessage pi)//TODO
    {
    	System.out.println("Received new PeerInfo");
		PeerInfo peerInfo = (PeerInfo)pi.getCommunicable();
		UUID uuid = pi.getUUID();
		
		if(peers.containsKey(uuid))
		{
			System.out.println("TODO: known peer, update existing record");
			//TODO
		}
		else
		{
			System.out.println("adding new PeerInfo to list of known peers...");
			peers.putIfAbsent(uuid, peerInfo);
		}
	}

    public void handleAllFilesRequest(ReceivedMessage afr)//TODO
    {
        
    }
    
    public void receiveExitAnnouncement(ReceivedMessage ea)//TODO
    {
        
    }
    
    public void handleArchiveStatusRequest(ReceivedMessage ftsr)//TODO
    {
        
    }
    
    public void receiveArchiveStatus(ReceivedMessage as)//TODO
    {
		
	}
	
	public void receiveNoHaz(ReceivedMessage nh)//TODO
	{
		
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
