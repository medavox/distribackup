package com.medavox.distribackup.peers;

import java.net.*;
import com.medavox.distribackup.connections.Address;
import java.util.UUID;
import java.net.Socket;
/**Stores information about peers*/

//1. UUID
//3. GlobalRevisionNumber| ULong
//5. Addresses           | List:Address

public class PeerInfo
{
	Address[] addresses;
	UUID uuid;
	long globalRevisionNumber;
	//Socket socket;
	
	public PeerInfo()
	{
		//this.socket = s;
	}
	/*multiple sockets*/
}
