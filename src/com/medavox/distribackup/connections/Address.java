package com.medavox.distribackup.connections;
public class Address//TODO: flesh this class out
{
	/*host (IP/IPv6/hostname)
	listenPort: short
	isUp:boolean (if we can't connect, can others in the network?)
	
	upSpeed:int(KB/s)
	downSpeed:int(KB/s)
		how do we measure these?
	lastSeen:Date-time*/
	
/*
2. isUp                | bitfield<0>
3. usingHostname(NotIP)| bitfield<1>
4. USing IPv6          | bitfield<2>
5. IP/hostname         | ByteArray.4/String
6. listenPort          | UShort
7. lastKnownTimeOnline | Long (ms since epoch)*/
	boolean isOnline;
	short listenPort;
	
}
