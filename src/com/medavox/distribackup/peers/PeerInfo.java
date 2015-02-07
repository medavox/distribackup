package com.medavox.distribackup.peers;

import com.medavox.distribackup.connections.Address;
import com.medavox.distribackup.connections.Communicable;
import java.util.UUID;
import java.util.List;

/*
1. UUID
3. GlobalRevisionNumber| Long
5. Addresses           | List:Address*/

//TODO:
//add Collection of open ConnectionOperators associated to this Peer
/**Stores information about peers*/
public class PeerInfo implements Communicable
{//do we need Addresses as well as ConnectionOperators?
//yes, becasue Addresses store info about offline connections, which we can plunder if our current connection goes down
//no, they're justa way of passing info between peers. Which would be lost without this anyway
//so, yes YES
//ERROR?:also, we don't care so much what GRN other Peers are on
//only if they can give us file (version)s we want
	private UUID uuid;
	private long globalRevisionNumber;
	public List<Address> addresses;//ERROR: Lists aren't thread-safe!
    private List<ConnectionOperator> openConnections = new ArrayList<ConnectionOperator>();//may need to become concurrent
	
	public PeerInfo(UUID uuid, long GRN, Address[] addresses)//TODO
	{
        this.uuid = uuid;
        this.globalRevisionNumber = GRN;
        
		//this.socket = s;
	}
	
	public UUID getUUID()
	{
		return uuid;
	}
    
    public boolean hasOpenConnection()//TODO
    {
        
    }
    
    
	
	public long getGlobalRevisionNumber()
	{
		return globalRevisionNumber;
	}
	
	public long getGRN()//sometimes, short method names are worth it
	{
		return globalRevisionNumber;
	}
	
	public void incrementGlobalRevisionNumber()
	{
		globalRevisionNumber++;
	}
	
	/**Short-named method to speed typing up what is (likely) 
	 * to be the most common operation on the GRN.*/
	public void incrGRN()
	{
		globalRevisionNumber++;
	}
	
	public void setGlobalRevisionNumber(long newGRN)
	{
		globalRevisionNumber = newGRN;
	}
	
	public void setGRN(long newGRN)
	{
		globalRevisionNumber = newGRN;
	}
}
