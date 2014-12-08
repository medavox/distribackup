package com.medavox.distribackup.peers;

import java.net.*;

/**Stores information about peers*/
public class PeerInfo
{
	Address[] addresses;
	UUID uuid;
	long globalRevisionNumber;
	public PeerInfo(Socket s, int version)
	{
		this.socket = s;
		this.version = version;
	}
	/*multiple sockets*/
}
