package com.medavox.distribackup.connections;

import java.net.InetAddress;

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
4. Using IPv6          | bitfield<2>
5. IP/hostname         | ByteArray.4/String
6. listenPort          | UShort
7. lastKnownTimeOnline | Long (ms since epoch)*/
	boolean isOnline;
	short listenPort;

	InetAddress addr;
	public Address(String hostname, char port)
	{
		

		addr = InetAddress.getByName(
	}
	
	public Address(byte[] ipAddress, boolean ipv6, char port)
	{
		
	}
}
=
