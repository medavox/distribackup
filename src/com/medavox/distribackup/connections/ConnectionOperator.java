package com.medavox.distribackup.connections;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.net.Socket;
import java.util.UUID;
import java.util.Arrays;

import com.medavox.distribackup.peers.Peer;
import com.medavox.distribackup.peers.PeerInfo;

/**This class handles communication over an individual Socket connection, 
 * after its initialisation. */
public class ConnectionOperator extends Thread
{
        
        /*ADDRESS               ((byte)0x0A, -2),
        BYTE_ARRAY              ((byte)0x0B, -1),
        PEER_INFO               ((byte)0x0C, -2),
        DIRECTORY_INFO  ((byte)0x0D, -2),
        LIST                    ((byte)0x0E, -1),
        FILE_INFO               ((byte)0x0F, -2),
        FILEDATA                ((byte)0x12, -2),
        FILE_REQUEST    ((byte)0x13, -2),
        GREETING                ((byte)0x14, 18),
        TREE_STATUS_REQ ((byte)0x16,  0),
        UPDATE_ANNOUNCE ((byte)0x17, -2),
        HLIST                   ((byte)0x18, -1);*/
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
    UUID greeting() throws IOException
    {
        //package up data into a single bytestream before sending, 
        //(as opposed to sending each bit as we create it), to minimise packets
        byte[] UUIDmsb = BinaryTranslator.longToBytes(Peer.myUUID.getMostSignificantBits());
        byte[] UUIDlsb = BinaryTranslator.longToBytes(Peer.myUUID.getLeastSignificantBits());
	byte[] greetingSend = BinaryTranslator.concat(Message.GREETING.IDByte, UUIDmsb, UUIDlsb);
        bos.write(greetingSend);
        bos.flush();
        
        //wait for greeting back
        byte[] theirGreeting = new byte[Message.GREETING.length-2];//minus header
	bis.read(theirGreeting, 2, theirGreeting.length);//again, skip header
                
	long theirUUIDmsb = BinaryTranslator.bytesToLong(Arrays.copyOfRange(theirGreeting, 0, 7));
	long theirUUIDlsb = BinaryTranslator.bytesToLong(Arrays.copyOfRange(theirGreeting, 8, 15));
	
	return new UUID(theirUUIDmsb, theirUUIDlsb);
    }
        
    void announceExit() throws IOException
    {
	byte[] exitAnnounce = {Message.EXIT_ANNOUNCE.IDByte};
	bos.write(exitAnnounce, 0, 1);
	bos.flush();
    }
    
    void requestMorePeers(PeerInfo[] knownPeers) throws IOException
    {
	byte[] reqForPeers = {Message.REQ_FOR_PEERS.IDByte};
	bos.write(reqForPeers, 0, 1);
	bos.flush();
    }
    
    void sendPeerInfo(PeerInfo[] peers) // TODO
    {
	
    }
    
    void sendTreeStatus() // TODO
    {
	
    }
    
    void sendFileData() // TODO
    {
	
    }
        
    void requestFile(Path p) // TODO
    {
	
    }
    /*RECEIVER: Handling incoming data*/
    public void run() // TODO
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
