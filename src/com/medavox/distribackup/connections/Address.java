package com.medavox.distribackup.connections;
public class Address
{
	/*host (IP/IPv6/hostname)
	listenPort: short
	isUp:boolean (if we can't connect, can others in the network?)
	socket:Socket(null if isUp == false)
	upSpeed:int(KB/s)
	downSpeed:int(KB/s)
		how do we measure these?
	lastSeen:Date-time*/
	boolean isOnline;
	short listenPort;
	
}
