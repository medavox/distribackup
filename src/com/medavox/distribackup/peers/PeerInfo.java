package com.medavox.distribackup.peers;

import com.medavox.distribackup.connections.Address;
import com.medavox.distribackup.connections.Communicable;
import com.medavox.distribackup.connections.ConnectionOperator;
import com.medavox.distribackup.filesystem.FileUtils;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

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
	public List<Address> addresses;/**A pool of known addresses from which to open new connections*///ERROR: Lists aren't thread-safe!
    private List<ConnectionOperator> openConnections = new ArrayList<ConnectionOperator>();//may need to become concurrent
    private String codeName;
	
	public PeerInfo(UUID uuid, /*long GRN,*/ Address[] addresses)//TODO
	{
        this.uuid = uuid;
        //this.globalRevisionNumber = GRN;
        codeName = FileUtils.getRandomName();
		//this.socket = s;
	}
	
	public UUID getUUID()
	{
		return uuid;
	}
    
    public boolean hasOpenConnection()
    {
        return (openConnections.size() == 0);
    }
    /**Returns an open connection. This method has room for later improvement,
     * choosing which open connection to return intelligently, based on which
     * open connection is the least used or fastest.*/
    public ConnectionOperator getOpenConnection()
    {//return a (currently randomly chosen) open connection
    	Random r = new Random();
    	int randomIndex = r.nextInt(openConnections.size());
    	return openConnections.get(randomIndex);
    }
    
    public void addConnection(ConnectionOperator co)
    {
    	openConnections.add(co);
    }
	
	public String toString()//needed for debugging and error messages
	{
		Random r = new Random();
		int randomIndex = r.nextInt(addresses.size());
		Address a = addresses.get(randomIndex);
		return "codename \""+codeName+"\" at "+a.getHostName()+":"+(int)a.getPort();
	}
}
