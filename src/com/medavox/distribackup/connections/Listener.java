package com.medavox.distribackup.connections;

import java.io.*;
import java.net.*;
import java.util.*;
import com.medavox.distribackup.peers.Peer;

public class Listener extends Thread
{
	static PrintStream o = System.out;
	Peer owner;
	private int port = 1210;//default value
	private List<Socket> sockets = new LinkedList<Socket>();
	private final int MAX_BACKLOG = 50;
	
	public Listener(int port, Peer owner)
	{
		this.port = port;
		this.owner = owner;
	}
	public void run()
	{
		try
		{
			ServerSocket svr = new ServerSocket(port, MAX_BACKLOG);
			o.println("listening on port "+port);
			while(true)
			{//on a new socket connection:
				Socket s = svr.accept();
				owner.newIncomingSocket(s);//callback: pass new Socket back to main thread
				//sockets.add(s);
				//o.println("connection "+connectionNumber+" established");
			}
		}
		catch(Exception e)
		{
			System.err.println("CRASHED on connection NODATA");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
