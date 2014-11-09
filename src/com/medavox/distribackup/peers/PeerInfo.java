package com.medavox.distribackup.peers;

import java.net.*;

/**Stores information about peers*/
public class PeerInfo
{
	//how do you serialise a socket? and why bother sending it?
	Socket socket;
	int version;
	public PeerInfo(Socket s, int version)
	{
		this.socket = s;
		this.version = version;
	}
	/*peers usually have open sockets, but they won't when they leave
	but we still need to store iformation about them like last seen, bandwidth, local copy state...
	allow to have multiple sockets
	* host (IP/IPv6/hostname)
	* port
	* multiple sockets
	peerINFO
		UUID
		host:string/ip
		port:int
		isUp:boolean (if we can't connect, can others in the network?)
		socket:Socket(null if isUp == false)
		upSpeed:int(KB/s)
		downSpeed:int(KB/s)
			how do we measure these?
		local filesystem state (info may be stale)
		lastSeen:Date-time
		version:int
	*/
}
