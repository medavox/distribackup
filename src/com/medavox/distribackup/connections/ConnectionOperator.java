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
import com.medavox.distribackup.filesystem.FileInfo;
import com.medavox.distribackup.filesystem.FileDataChunk;

/**This class handles communication over an individual Socket connection, 
 * after its initialisation. */
public class ConnectionOperator extends Thread
{
	/*ADDRESS               ((byte)0x0A, -2),
	BYTE_ARRAY              ((byte)0x0B, -1),
	PEER_INFO               ((byte)0x0C, -2),
	DIRECTORY_INFO          ((byte)0x0D, -2),
	LIST                    ((byte)0x0E, -1),
	FILE_INFO               ((byte)0x0F, -2),
	FILE_DATA_CHUNK         ((byte)0x12, -2),
	FILE_REQUEST            ((byte)0x13, -2),
	GREETING                ((byte)0x14, 18),
	TREE_STATUS_REQ         ((byte)0x16,  0),
	UPDATE_ANNOUNCE         ((byte)0x17, -2),
	HLIST                   ((byte)0x18, -1);*/
	BufferedInputStream bis;
	BufferedOutputStream bos;
	Socket socket;
    private UUID connectedPeer;
    private Peer owner;
    
	public ConnectionOperator(Socket s, Peer owner) throws IOException
	{
		this.socket = s;
		bis = new BufferedInputStream(s.getInputStream());
		bos = new BufferedOutputStream(s.getOutputStream());
        this.owner = owner;
	}
	
	/**Checks that local and remote program versions match*/
	public int checkVersions() throws IOException
	{
		//check if we've seen the connecting peer before
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
	UUID exchangeUUIDs() throws IOException
	{
		//package up data into a single byte[] before sending, 
		//(as opposed to sending each part as we create it), to minimise packets
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
		
		UUID theirUUID = new UUID(theirUUIDmsb, theirUUIDlsb);
        connectedPeer = theirUUID;
        return theirUUID;
	}
		
	public void announceExit() throws IOException
	{
		byte[] exitAnnounce = {Message.EXIT_ANNOUNCE.IDByte};
		bos.write(exitAnnounce, 0, 1);
		bos.flush();
	}
	
	public void requestMorePeers(/*PeerInfo[] knownPeers*/) throws IOException
	{
		byte[] reqForPeers = {Message.REQ_FOR_PEERS.IDByte};
		bos.write(reqForPeers, 0, 1);
		bos.flush();
	}
    
    public void sendPeerInfoRequest()
    {
        byte[] peerInfoReq = {Message.PEERINFO_REQUEST.IDByte};
        bos.write(reqForPeers, 0, 1);
		bos.flush();
    }
    
    
	
	public void sendPeerInfoList(PeerInfo[] peers) // TODO
	{
		byte IDByte = Message.PEER_INFO.IDByte;
        
	}
	
	public void sendTreeStatus() // TODO
	{
		
	}
	
	public void sendFileData(FileDataChunk fdc)
	{
        try
        {
            byte IDByte = Message.FILE_DATA_CHUNK.IDByte;
            byte[] dataChunkBin = BinaryTranslator.fileDataChunkToBytes(fdc);
            byte[] message = BinaryTranslator.concat(IDByte, dataChunkBin);
            bos.write(message, 0, message.length);
            bos.flush();
        }
        catch(Exception e)
        {
            reportException(e);
        }
	}
		
	public void requestFile(FileInfo fi) // TODO
	{//FileRequest messages are just wrappers around a FileInfo, so construct it here
    //having two seperate length field for this seems unecessary, but is consistent with the current spec
    //maybe it can be changed later
        try
        {
            byte IDByte = Message.FILE_REQUEST.IDByte;
            byte[] fileInfoBin = BinaryTranslator.fileInfoToBytes(fi);
            byte[] messageLength = BinaryTranslator.intToBytes(fileInfoBin.length);
            
            byte[] message = BinaryTranslator.concat(IDByte, messageLength, fileInfoBin);
            bos.write(message, 0, message.length);
            bos.flush();
        }
        catch(Exception e)
        {
            reportException(e);
        }
	}
    
    private void reportException(Exception e)
    {
        System.err.println("ERROR: ConnectionOperator thread \""+this.getName()+
            "\" reports "+e);
        e.printStackTrace();
    }
    
	/**Binary data receiver. Gathers enough bytes to form the next complete 
     message, translates it back to java types, wraps it in a ReceivedMessage
     * which contains info about originating connection and Peer, then sends it
     * to the Peer's IncomingMessageProcessor*/
	public void run() // TODO
	{/**a queue of enums which each represent incoming events 
    a single event handling thread deals with each event in order
    each event enum will need info about it attached, like WHICH peer announced 
    its exit. Connection operators receiving bytes (which they then decode into
    messages) will add to the queue*/
		while(!socket.isClosed())
        {
            try
            {
                int nextID = bis.read();
                if(nextID == -1)//TODO
                {//stream has closed or "end has been reached"
                    //guard clause
                }
                byte nextIDByte = (byte)nextID;
                Message nextMessage = Message.getMessageTypeFromID(nextIDByte);
                if(nextMessage == null)//TODO
                {//guard clause
                    //another error; no Message was found with that IDByte
                }
                
                if(nextMessage.length == 0)//TODO
                {//this is a no-payload message, so we're ready to send it off
                    //...
                    ReceivedMessage rxmsg = new 
                        ReceivedMessage(nextMessage, connectedPeer, this);
                        
                    owner.addToQueue(rxmsg);
                }
                
                int nextLength = -1;
                
                if(nextMessage.length < 0)
                {//incoming message is variable-length
                //read next 4 (or 8) bytes (the length field) to work out
                //how many more bytes we need to read
                    int lengthLength;
                    if(nextMessage == Message.LIST ||
                        nextMessage == Message.HLIST)
                    {//currently, Lists and HLists employ a long (not an int) for their length fields
                        lengthLength = 8;
                    }
                    else
                    {
                        lengthLength = 4;
                    }
                    
                    //get length of message from length field
                    byte[] lengthBytes = new byte[lengthLength];
                    bis.read(lengthBytes, 0, lengthLength);
                    nextLength = BinaryTranslator.bytesToInt(lengthBytes);
                }
                else
                {//Message is fixed-length; read the value from the Message enum
                    nextLength = nextMessage.length;
                }
            
                //read the entire message into a byte[]
                byte[] messageBodyBin = new byte[nextLength];
                bis.read(messageBodyBin, 0, nextLength);
                
                //convert the byteArray into a Communicable
                Communicable details = BinaryTranslator.bytesToCommunicable(messageBodyBin, nextMessage);
                
                //construct a ReceivedMessage and add it to the IMP queue
                ReceivedMessage rxmsg = new
                    ReceivedMessage(nextMessage, connectedPeer, this, details);
                
                owner.addToQueue(rxmsg);
                //convert this into ReceivedMessage
            }
            catch(Exception e)
            {
                reportException(e);
            }
        }
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
