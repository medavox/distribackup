package com.medavox.distribackup.peers;

import com.medavox.distribackup.connections.Address;
import com.medavox.distribackup.connections.Communicable;
import com.medavox.distribackup.connections.ConnectionOperator;
import com.medavox.distribackup.filesystem.FileUtils;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
//import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/*
1. UUID
3. GlobalRevisionNumber| Long
5. Addresses           | List:Address*/

//TODO:
//add Collection of open ConnectionOperators associated to this Peer
/**Stores information about peers*/
public class PeerInfo implements Communicable
{//do we need Addresses as well as ConnectionOperators?
//yes, because Addresses store info about offline connections, which we can plunder if our current connection goes down
//no, they're just a way of passing info between peers. Which would be lost without this anyway
//so, yes YES
//ERROR?:also, we don't care so much what GRN other Peers are on
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
