package com.medavox.distribackup.peers;

import com.medavox.distribackup.connections.Address;
import com.medavox.distribackup.connections.Communicable;
import com.medavox.distribackup.connections.ConnectionOperator;
import com.medavox.distribackup.filesystem.FileUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
//import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**Stores information about other peers on the network.*/
public class PeerInfo implements Communicable
{
//WARNING: we don't care so much what GRN other Peers are on
//only if they can give us file (version)s we want
	private UUID uuid;
	//private long globalRevisionNumber;
	//ERROR: Lists aren't thread-safe!
	/**A pool of known addresses from which to open new connections*/
	public List<Address> addresses = new ArrayList<Address>();
    private CopyOnWriteArrayList<ConnectionOperator> openConnections = new CopyOnWriteArrayList<ConnectionOperator>();//may need to become concurrent
    private String codeName;
	private boolean isPublisher;
    
	public PeerInfo(UUID uuid, boolean isPublisher, Address[] startingAddresses)//TODO
	{
        this.uuid = uuid;
        this.isPublisher = isPublisher;
        codeName = FileUtils.getCodeName(uuid);
		//this.socket = s;
        for(Address a : startingAddresses)
        {
        	addresses.add(a);
        }
	}
	
	public UUID getUUID()
	{
		return uuid;
	}
    
    public boolean hasOpenConnection()
    {
        return !openConnections.isEmpty();
    }
    
    /**Returns an open connection. This method has room for later improvement,
     * choosing which open connection to return intelligently, based on which
     * open connection is the least used or fastest.*/
    public ConnectionOperator getOpenConnection()
    {//return an open connection
    	try
    	{
    		return openConnections.get(0);
    	}
    	catch(IndexOutOfBoundsException nsee)
    	{
    		return null;
    	}
    }
    
    public ConnectionOperator newConnection(Peer owner) throws ConnectException
    {
    	for(Address a : addresses)
    	{//try to establish a new connection until either one is made
    		//or we run out of known connections to this peer
    		String host = a.getHostName();
    		int port = (int)(a.getPort());
			try
			{
				Socket s = new Socket(host, port);
				ConnectionOperator co = new ConnectionOperator(s, owner);
				openConnections.add(co);
				return co;
			}
			catch(UnknownHostException uhe)
			{
				System.err.println("Unknown Host occurred: \""+uhe.getMessage()+
						"\" while trying to connect at "+host+":"+port);
				uhe.printStackTrace();
			}
			catch(IOException ioe)
			{
				System.err.println("IOException occurred: \""+ioe.getMessage()+
						"\" while trying to connect at "+host+":"+port);
				//ioe.printStackTrace();
			}
    	}
    	throw new ConnectException("a connection to "+codeName+
    			" couldn't be made at any known address!");
    }
    
    public boolean isPublisher()
    {
    	return isPublisher;
    }
    
    public void addConnection(ConnectionOperator co)
    {
    	openConnections.add(co);
    }
    
    public void removeConnection(ConnectionOperator co)
    {
    	openConnections.remove(co);
    }
    
    public Address[] getAddresses()
    {
    	Address[] adds = new Address[addresses.size()];
    	addresses.toArray(adds);
    	return adds;
    }
	
	public String toString()//needed for debugging and error messages
	{
		//Random r = new Random();
		//int randomIndex = r.nextInt(addresses.size());
		//Address a = addresses.get(randomIndex);
		Address a = addresses.get(0);
		return "codename \""+codeName+"\" at "+a.getHostName()+":"+(int)a.getPort();
	}
}
