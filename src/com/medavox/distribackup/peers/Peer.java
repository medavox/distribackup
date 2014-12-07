package com.medavox.distribackup.peers;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;

import com.medavox.distribackup.connections.*;
import com.medavox.distribackup.peers.*;
import com.medavox.distribackup.fswatch.*;

public abstract class Peer
{
	private int listenPort;
    private String defaultRoot = "/home/scc/distribackup/publisher-root";
    public Path root;
    //private List<PeerInfo> peers = new LinkedList<PeerInfo>();
    ConcurrentHashMap<UUID, PeerInfo> peers = new ConcurrentHashMap<UUID, PeerInfo>();
    UUID myUUID;
	//public void newSocket(Socket s);
	
	public BufferedInputStream bis;
	public BufferedOutputStream bos;
	
	static final short version = 1;//increment this manually on every release
	
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
			System.out.println("incoming connection from "+s.getInetAddress());
			bis = new BufferedInputStream(s.getInputStream());
			bos = new BufferedOutputStream(s.getOutputStream());
			int handshook = checkVersions(bis, bos);
			if(handshook == -1)
			{
				System.err.println("Error! Connecting Peer "+s.getInetAddress()+" has wrong version!");
				bis.close();
				bos.close();
				s.close();
				return;
			}
			
			//get their UUID
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
	/**Checks that local and remote program versions match*/
	public int checkVersions(BufferedInputStream bips, BufferedOutputStream bops) throws IOException
    {
		//see if we've seen the connecting peer before
		//if not, create a new PeerInfo object (query for some info)
		//if we have, then add this socket to that peer's open sockets
		
		//construct magic number and version number byte array
		byte[] ret = new byte[2];
		//short -> bytes
		ret[1] = (byte)(version & 0xff);
		ret[0] = (byte)(version >> 8);
		
		//"Be Selfless" + version:short
		byte[] versionBytes = {(byte)0xBE, (byte)0x5E, (byte)0x1F, (byte)0x1E, (byte)0x55, ret[0], ret[1]};
		
		bops.write(versionBytes, 0, versionBytes.length);//send our version string
		bops.flush();
		
		//read their version string
		byte[] theirVersion = new byte[versionBytes.length];
		bips.read(theirVersion, 0, theirVersion.length);
		
		//their version bytes -> short
		short theirVer = (short) (theirVersion[theirVersion.length-1] | (theirVersion[theirVersion.length-2] << 8));

		System.out.println("Our version:   "+version);
		System.out.println("Their version: "+theirVer);
		if(version != theirVer)
		{
			return -1;
		}
		return 0;
    }
    
    /**TODO:
     store 1 bis-bos pair per socket entry
		or create one when a socket is referenced from the hashmap*/
    public void connect(String host, int port)
    {
		try
		{
			Socket s = new Socket(host, port);
			newSocket(s);
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
