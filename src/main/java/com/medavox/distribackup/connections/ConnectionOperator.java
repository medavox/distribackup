package com.medavox.distribackup.connections;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.Arrays;

import com.medavox.distribackup.peers.Peer;
import com.medavox.distribackup.peers.PeerInfo;
import com.medavox.distribackup.filesystem.FileInfo;
import com.medavox.distribackup.filesystem.FileDataChunk;
import com.medavox.distribackup.filesystem.FileInfoBunch;

/**Handles communication over an individual Socket connection. 
 * One instance per connection. Has its own listening thread,
 * which passes received messages to Peer's Incoming Message Processing Thread.*/
public class ConnectionOperator extends Thread
{/* PeerInfo				EXISTS
	Archive Status			EXISTS
	Request For Peers		DONE
	Request All Files		DONE
	File Data Chunk			EXISTS
	File Request			EXISTS
	Greeting				DONE
	Exit Announcement		EXISTS
	Archive Status Request	DONE
	Update Announcement		EXISTS
	"no haz" FileReq Reply	EXISTS
	PeerInfo Request		DONE
	"haz nao" announcement	EXISTS
	More Peers				EXISTS*/
	
	BufferedInputStream bis;
	BufferedOutputStream bos;
	Socket socket;
    protected UUID connectedPeer;
    protected Peer owner;
    //if/when we add connection speed measuring, this is where it will go
	public ConnectionOperator(Socket s, Peer owner) throws IOException
	{
		this.socket = s;
		bis = new BufferedInputStream(s.getInputStream());
		bos = new BufferedOutputStream(s.getOutputStream());
        this.owner = owner;
        

    	int handshook = checkVersions();
    	if(handshook == -1)
		{
    		close();
			throw new IOException("Connecting Peer "+s.getInetAddress()+
					" has wrong version!");
		}
		System.out.println("new connection to "+s.getInetAddress());
		connectedPeer = exchangeUUIDs();
	}
    /*
    public ConnectionOperator(Address a, Peer owner)//TODO: implement
    {
        
    }*/
	
	/**Checks that local and remote program versions match*/
	protected int checkVersions() throws IOException
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

		//System.out.println("Our version:   "+version);
		//System.out.println("Their version: "+theirVer);
		if(version != theirVer)
		{
			return -1;
		}
		return 0;
	}
	
	/*SENDING:Methods to send data down the socket*/
	
	/**Sends a greeting (containing our UUID), waits for a greeting in reply, and returns the received UUID */
	protected UUID exchangeUUIDs() throws IOException
	{
		//package up data into a single byte[] before sending, 
		//(as opposed to sending each part as we create it), to minimise packets
		System.out.println("sending my UUID");
		
		byte[] UUIDmsb = BinaryTranslator.longToBytes(Peer.myUUID.getMostSignificantBits());
		byte[] UUIDlsb = BinaryTranslator.longToBytes(Peer.myUUID.getLeastSignificantBits());
		byte[] greetingSend = BinaryTranslator.concat(Message.GREETING.IDByte, UUIDmsb, UUIDlsb);
		bos.write(greetingSend);
		bos.flush();
		
		//wait for greeting back
		
		byte[] theirIDByte = new byte[1];
		
		bis.read(theirIDByte, 0, 1);
		
		byte[] msb = new byte[8];
		byte[] lsb = new byte[8];
        
		bis.read(msb, 0, 8);
		bis.read(lsb, 0, 8);
		
		long theirUUIDmsb = BinaryTranslator.bytesToLong(msb);
		long theirUUIDlsb = BinaryTranslator.bytesToLong(lsb);
		
		System.out.println("Received a UUID.");
		
		UUID theirUUID = new UUID(theirUUIDmsb, theirUUIDlsb);
        connectedPeer = theirUUID;
        return theirUUID;
	}
		
	public void announceExit() throws IOException
	{
		System.out.println("Announcing exit");
		byte[] exitAnnounce = {Message.EXIT_ANNOUNCE.IDByte};
		bos.write(exitAnnounce, 0, 1);
		bos.flush();
	}
	
	public void announceHaveNowGotFile(FileInfo newlyAcquiredFile)//TODO: implement
	{
		
	}
	
	public void requestMorePeers(/*PeerInfo[] knownPeers*/) throws IOException
	{
		byte[] reqForPeers = {Message.REQ_FOR_PEERS.IDByte};
		bos.write(reqForPeers, 0, 1);
		bos.flush();
	}
	
	public void sendPeerInfo() throws IOException
	{
		System.out.println("Sending my PeerInfo...");
		//TODO: how do we find out addresses we are known by?
		PeerInfo me = new PeerInfo(Peer.myUUID, owner.isPublisher(), new Address[0]);
		
		byte[] peerInfoBytes = BinaryTranslator.peerInfoToBytes(me, owner.isPublisher());
		byte[] fullMsg = BinaryTranslator.concat(Message.PEER_INFO.IDByte, peerInfoBytes);
		//write out
		bos.write(fullMsg, 0, fullMsg.length);
		bos.flush();
	}
	
	public void sendUpdateAnnouncement(FileInfoBunch ua)throws IOException
	{
		System.out.println("Sending Update Annnouncement");
		byte IDByte = Message.UPDATE_ANNOUNCE.IDByte;
		byte[] uaBytes = BinaryTranslator.fileInfoBunchToBytes(ua);
		byte[] toSend = BinaryTranslator.concat(IDByte, uaBytes);
		bos.write(toSend, 0, toSend.length);
		bos.flush();
	}
    
    public void requestPeerInfo() throws IOException
    {
    	System.out.println("Requesting PeerInfo...");
        byte[] peerInfoReq = {Message.PEER_INFO_REQ.IDByte};
        bos.write(peerInfoReq, 0, peerInfoReq.length);
		bos.flush();
    }
	
	public void sendMorePeers(PeerInfo[] morePeers) throws IOException
	{
		byte IDByte = Message.MORE_PEERS.IDByte;
		byte[][] peerBytes = new byte[morePeers.length][];
        for(int i = 0; i < morePeers.length; i++)
        {
        	peerBytes[i] = BinaryTranslator.peerInfoToBytes(morePeers[i], false);
        }
        
        byte[] morePeersBytes = BinaryTranslator.listToBytes(peerBytes, Message.PEER_INFO.IDByte);
        byte[] toSend = BinaryTranslator.concat(IDByte, morePeersBytes);
        
        bos.write(toSend, 0, toSend.length);
		bos.flush();
	}
	
	public void sendArchiveState() throws IOException
	{
		sendArchiveStatus();
	}
	
	public void sendArchiveStatus() throws IOException
	{
		System.out.println("Sending Archive State...");
		//File f = Peer.root.toFile();
		//pull existing data from globalArchiveState
		byte idByte = Message.ARCHIVE_STATUS.IDByte;
		byte[] b = BinaryTranslator.fileInfoBunchToBytes(owner.globalArchiveState);
		byte[] msg = BinaryTranslator.concat(idByte, b);
		
		bos.write(msg, 0, msg.length);
		bos.flush();
	}

	
	public void sendFileDataChunk(FileDataChunk fdc)
	{
		System.out.println("sending file data \""+fdc.getFileInfo().getName()+"\"");
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
	
	public void sendNoHazFile(FileInfo[] unavailableFiles)//TODO: implement
	{
		
	}
	
	public void requestArchiveStatus() throws IOException
	{
        byte[] archiveStatusReq = {Message.TREE_STATUS_REQ.IDByte};
        bos.write(archiveStatusReq, 0, 1);
		bos.flush();
	}
		
	public void requestFile(FileInfo fi)
	{//FileRequest messages are just wrappers around a FileInfo, so construct it here
    //having two separate length fields for this seems unnecessary, but is consistent with the current spec
    //maybe it can be changed later
		//String separ = (fi.getPath().length() == 0 ? "" : owner.sep);
		System.out.println("Requesting file:"+fi.getPath()+" "+fi.getName());
        try
        {
            byte IDByte = Message.FILE_REQUEST.IDByte;
            FileInfo[] fiWrapper = {fi};
            FileInfoBunch fib = new FileInfoBunch(fiWrapper);
            byte[] fileInfoBin = BinaryTranslator.fileInfoBunchToBytes(fib);
            
            byte[] message = BinaryTranslator.concat(IDByte, fileInfoBin);
            bos.write(message, 0, message.length);
            bos.flush();
        }
        catch(Exception e)
        {
            reportException(e);
        }
	}
	
	public void requestAllFiles() throws IOException
	{
        byte[] allFilesReq = {Message.REQ_ALL_FILES.IDByte};
        bos.write(allFilesReq, 0, 1);
		bos.flush();
	}
	
	public UUID getConnectedUUID()
	{
		return connectedPeer;
	}

	public UUID getUUID()
	{
		return connectedPeer;
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
	public void run()
	{/**a queue of enums which each represent incoming events 
    a single event handling thread deals with each event in order
    each event enum will need info about it attached, like WHICH peer announced 
    its exit. Connection operators receiving bytes (which they then decode into
    messages) will add to the queue*/
		//now this CO is open for listening, add it to Peer's open connections list
		owner.openConnections.add(this);
		while(!socket.isClosed()
		&& owner.threadsEnabled)
        {
			//System.out.println("Start incoming socket scanning loop");
            try
            {
            	//wait(100);//slow things down a bit
                int nextID = bis.read();
                if(nextID == -1)//TODO: more robust error handling
                {//stream has closed or "end has been reached"
                	this.close();
                	break;
                }
                byte nextIDByte = (byte)nextID;
                Message nextMessage = Message.getMessageTypeFromID(nextIDByte);
                if(nextMessage == null)//TODO: more robust error handling
                {//guard clause
                    //another error; no Message was found with that IDByte
                	System.err.println("ERROR:No Message found with IDByte:"+nextID);
                	System.exit(1);
                }
                
                int nextLength = -1;
                
                if(nextMessage.length == 0)
                {//this is a no-payload message, so we're ready to send it off
                    ReceivedMessage rxmsg = new 
                        ReceivedMessage(nextMessage, connectedPeer, this);
                    owner.addToQueue(rxmsg);
                    continue;
                }
                else if(nextMessage.length < 0)
                {//incoming message is variable-length
                //read next 4 bytes (length field) to work out how many more 
                //bytes we need to read
                    byte[] lengthBytes = new byte[4];
                    bis.read(lengthBytes, 0, 4);
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
            catch(SocketException sce)
            {
            	System.out.println("Socket Closed.");
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
			owner.openConnections.remove(this);//remove self from open connections
		}
		catch(IOException ioe)
		{
			System.err.println("Unable to close Connection!");
		}
	}
}
