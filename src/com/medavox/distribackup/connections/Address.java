package com.medavox.distribackup.connections;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

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
6. listenPort          | UShort (char)
7. lastKnownTimeOnline | Long (ms since epoch)*/

	private Date lastTimeKnownOnline;
	private boolean isOnline;
	private boolean usingHostName;
	private boolean IPv6;
	private char port;
	private InetAddress addr;
	
	public Address(String hostName, char port, boolean isOnline, long lastTimeOnline) throws UnknownHostException
	{
		this.IPv6 = false;
		usingHostName = true;
		addr = InetAddress.getByName(hostName);
		commonInit(port, isOnline, lastTimeOnline);
	}
	
	public Address(byte[] ipAddress, boolean IPv6, char port, boolean isOnline, long lastTimeOnline) throws UnknownHostException
	{
		//java works out if the address is IPv6 or IPv4 based on the number of bytes
		usingHostName = false;
		this.IPv6 = IPv6;
		addr = InetAddress.getByAddress(ipAddress);
		commonInit(port, isOnline, lastTimeOnline);
	}
	
	private void commonInit(char port, boolean isOnline, long timeOnlineLong)
	{
		this.port = port;
		this.isOnline = isOnline;
		lastTimeKnownOnline = new Date(timeOnlineLong);
	}
	
	public char getPort()
	{
		return port;
	}
	
	public boolean usingHostName()
	{
		return usingHostName;
	}
	
	public boolean isIPv6()
	{
		return IPv6;
	}
	
	public byte[] getRawIPAddress()
	{
		return addr.getAddress();
	}
	
	public String getHostName()
	{
		return addr.getHostName();
	}
	
	public boolean isOnline()//TODO
	{//either we use the built-in InetAddress.checkReachable() type thing (which restricts us to an ICMP echo over port 7,
		//or we implement something clever which allows us to check the right port
		
		/*results of checking could be:-
		they are online at this address,
		they aren't,
		they use a different protocol version,
		or that they aren't part of this swarm (anymore?)*/
		
		if(true /*we couldn't reach this address*/)//TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO 
		{
			isOnline = false;
		}
		else//we could reach them
		{
			isOnline = true;
			lastTimeKnownOnline = new Date();//we last saw them online right now
		}
		return isOnline;
	}
	
	public Date getLastKnownTimeOnline()
	{
		return lastTimeKnownOnline;
	}
}
