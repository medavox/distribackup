package com.medavox.distribackup.peers;

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;

import com.medavox.distribackup.connections.*;

public abstract class Peer
{
	private int listenPort = 1210;
    private String defaultRoot = "/home/scc/distribackup/publisher-root";
    public Path root;
    private List<PeerInfo> peers = new LinkedList<PeerInfo>();
    UUID myUUID;
	//public void newSocket(Socket s);
	static final short version = 1;
	
	public void newSocket(Socket s)
	{
		try
		{
			BufferedInputStream bis = new BufferedInputStream(s.getInputStream());
			BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
			int handshook = handshake(bis, bos);
			if(handshook == -1)
			{
				System.err.println("Error! Connecting Peer "+s.getInetAddress()+" has wrong version!");
				bis.close();
				bos.close();
				s.close();
				return;
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
	}
	
	private Peer(Path root, int port)
	{
		this.root = root;
		this.listenPort = port;
		myUUID = UUID.randomUUID();//The peer should keep this safe, as it uniquely IDs it on the network
		Listener listenHook = new Listener(port, this);
	}
	

	public int handshake(BufferedInputStream bis, BufferedOutputStream bos) throws IOException
    {
		//do some basic handshake stuff
		//check versions match
		//see if we've seen the connecting peer before
		//if not, create a new peer object (query for some info)
		//if we have, then add this socket to that peer's open sockets
		
		//construct magic number and version number byte array
		byte[] ret = new byte[2];
		//short -> bytes
		ret[1] = (byte)(version & 0xff);
		ret[0] = (byte)(version >> 8);
		
		//"Be Selfless" + version:short
		byte[] versionBytes = {(byte)0xBE, (byte)0x5E, (byte)0x1F, (byte)0x1E, (byte)0x55, ret[0], ret[1]};
		
		bos.write(versionBytes, 0, versionBytes.length);//send our version string
		bos.flush();
		
		//read their version string
		byte[] theirVersion = new byte[versionBytes.length];
		bis.read(theirVersion, 0, theirVersion.length);
		
		//their version bytes -> short
		short theirVer = (short) (theirVersion[theirVersion.length-1] | (theirVersion[theirVersion.length-2] << 8));

		if(version != theirVer)
		{
			System.err.println("Our version:   "+version);
			System.err.println("Their version: "+theirVer);
			return -1;
		}
		
		boolean isNewPeer = true;//STOPGAP: REMOVE
		if(isNewPeer)
		{
			//byte[] version;
		}
		else
		{
			
		}
		return 0;
    }
}
