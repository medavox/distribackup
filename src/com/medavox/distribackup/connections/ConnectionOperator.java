package com.medavox.distribackup.connections;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.net.Socket;
import java.util.UUID;

import com.medavox.distribackup.peers.Peer;
import com.medavox.distribackup.peers.PeerInfo;

/**This class handles communication over an individual Socket connection, after its initialisation. */
public class ConnectionOperator extends Thread
{
    BufferedInputStream bis;
    BufferedOutputStream bos;
    Socket socket;
	public ConnectionOperator(Socket s/*, PeerInfo*/) throws IOException
	{
	    this.socket = s;
	    bis = new BufferedInputStream(s.getInputStream());
	    bos = new BufferedOutputStream(s.getOutputStream());
	}
    
    /**Checks that local and remote program versions match*/
    public int checkVersions()
        throws IOException
    {
		//see if we've seen the connecting peer before
		//if not, create a new PeerInfo object (query for some info)
		//if we have, then add this socket to that peer's open sockets
		short version = Peer.version;
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

		System.out.println("Our version:   "+version);
		System.out.println("Their version: "+theirVer);
		if(version != theirVer)
		{
		    return -1;
		}
		return 0;
    }
    
	/*SENDING:Methods to send data down the socket*/
    
    /**Sends a greeting (containing our UUID), waits for a greeting in reply, and returns the received UUID */
	UUID greeting()
	{
        //package up data into a single bytestream before sending, 
        //(as opposed to sending each bit as we create it), to minimise packets
        byte[] UUIDmsb = BinaryTranslator.longToBytes(Peer.myUUID.getMostSignificantBits());
        byte[] UUIDlsb = BinaryTranslator.longToBytes(Peer.myUUID.getLeastSignificantBits());
		byte[] greetingSend = BinaryTranslator.concat(Message.GREETING, UUIDmsb, UUIDlsb);
        bos.write(greetingSend);
        bos.flush();
        
        //wait for greeting back
        byte[] theirGreeting = new byte[Message.GREETING.length-2];//minus header
		bis.read(theirGreeting, 2, theirGreeting.length);//again, skip header
	}
	
	void announceExit()
	{
		
	}
	
	void requestMorePeers(PeerInfo[] knownPeers)
	{
		
	}
	
	void sendPeerInfo(PeerInfo[] peers)
	{
		
	}
	
	void sendTreeStatus()
	{
		
	}
	
    void requestFile(Path p)
	{
		
	}
    /*RECEIVER: Handling incoming data*/
    public void run()
    {
        
    }
    
    public void close() throws IOException
	{
		try
	    {
            bis.close();
            bos.close();
            socket.close();
	    }
	    catch(IOException ioe)
	    {
            System.err.println("Unable to close Connection!");
	    }
	}
}
