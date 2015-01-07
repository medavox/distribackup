package com.medavox.distribackup.peers;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;

import com.medavox.distribackup.connections.*;
import com.medavox.distribackup.peers.*;
import com.medavox.distribackup.filesystem.*;
/* There must be a local state object which records open sockets, and which peers they pertain to
*/
public abstract class Peer
{
	private int listenPort;
    private String defaultRoot = "/home/scc/distribackup/publisher-root";
    public Path root;
    ConcurrentHashMap<UUID, PeerInfo> peers = new ConcurrentHashMap<UUID, PeerInfo>();
    PeerInfo publisherInfo;
    UUID publisherID;
    public static UUID myUUID;
	
	public static final short version = 1;//increment this manually on every release
	List<ConnectionOperator> openConnections = new ArrayList<ConnectionOperator>();
	public Peer(Path root, int port)
	{
		this.root = root;
		this.listenPort = port;
		myUUID = UUID.randomUUID();//need to keep this safe, as it uniquely IDs us on the network
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
	public void newIncomingSocket(Socket s)
	{
		try
		{
			ConnectionOperator co = new ConnectionOperator(s);
			int handshook = co.checkVersions();
			System.out.println("incoming connection from "+s.getInetAddress());
			
			if(handshook == -1)
			{
				System.err.println("Error! Connecting Peer "+s.getInetAddress()+" has wrong version!");
				co.close();
				return;
			}
			
			//send a greeting, expect a greeting
				//send our UUID
				//get theirs
			
			//construct a new PeerInfo based on data requested and received

			//check it with the local PeerInfo store
			
			boolean isNewPeer = true;//STOPGAP: REMOVE
			if(isNewPeer)
			{
				//byte[] version;
			}
			else
			{
				
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
			newIncomingSocket(s);
			/*bis = new BufferedInputStream(s.getInputStream());
			bos = new BufferedOutputStream(s.getOutputStream());
			
			int handshook = checkVersions(bis, bos);
			
			if(handshook == -1)
			{
				System.err.println("Error! Connecting Peer "+s.getInetAddress()+" has wrong version!");
				bis.close();
				bos.close();
				s.close();
				//return;
			}*/
		}
		catch(UnknownHostException uhe)
		{
			//TODO
		}
		catch(IOException ioe)
		{
			//TODO
		}
		
		//send UUID
		
	}
	
	public void sendFile(Path file, BufferedOutputStream bos) throws IOException
	{
		String fileName = file.getFileName().toString();
		
		String location = file.relativize(root).toString();
		
		byte[] fileArray;
		fileArray = Files.readAllBytes(file);
		FileTransfer ft = new FileTransfer(fileName, location, fileArray);
		
		ObjectOutputStream objos = new ObjectOutputStream(bos);
		
		objos.writeObject(ft);
	}
}
