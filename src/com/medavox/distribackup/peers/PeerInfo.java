package com.medavox.distribackup.peers;

import java.net.*;
import com.medavox.distribackup.connections.Address;
import java.util.UUID;
import java.net.Socket;
/**Stores information about peers*/
public class PeerInfo
{
	Address[] addresses;
	UUID uuid;
	long globalRevisionNumber;
	Socket socket;
	
	public PeerInfo(Socket s)
	{
		this.socket = s;
	}
	/*multiple sockets*/
}
