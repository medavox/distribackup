package com.medavox.distribackup.peers;

//import java.net.*;
import com.medavox.distribackup.connections.Address;
import com.medavox.distribackup.connections.Communicable;
import java.util.UUID;
import java.util.List;
//import java.net.Socket;
/**Stores information about peers*/

/*
1. UUID
3. GlobalRevisionNumber| Long
5. Addresses           | List:Address
*/

public class PeerInfo implements Communicable
{
	private UUID uuid;
	private long globalRevisionNumber;
	public List<Address> addresses;
	//Socket socket;
	
	public PeerInfo(UUID uuid, long GRN, Address[] addresses)
	{
		//this.socket = s;
	}
	
	public UUID getUUID()
	{
		return uuid;
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
	
	/**Convenience method to speed up what is (likely) 
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
