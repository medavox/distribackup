package com.medavox.distribackup.connections;

import java.io.*;
import java.net.*;
import com.medavox.distribackup.peers.Peer;
/**Assigns new Sockets to incoming connections. The first point of contact for new connections.
 * Further new connection set up is handled via a callback: Peer.setupNewSocket(Socket)*/
public class Listener extends Thread
{
	static PrintStream o = System.out;
	Peer owner;
	private int port = 1210;//default value
	private final int MAX_BACKLOG = 50;
	
	public Listener(int port, Peer owner)
	{
		this.port = port;
		this.owner = owner;
	}
	public void run()
	{
		ServerSocket svr;
		//get open port to listen on
		while(true)
		{
			try
			{
				System.out.println("Opening listening port: "+port+"...");
				svr = new ServerSocket(port, MAX_BACKLOG);
				break;
			}
			catch(BindException be)
			{
				System.err.println(be.getMessage());
				port++;
				continue;
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
				System.exit(1);
			}
		}
		while(owner.threadsEnabled)
		{//on a new socket connection:
			try
			{
				Socket s = svr.accept();
				owner.setupNewSocket(s);//callback: pass new Socket back to main thread
			}
			catch(Exception e)
			{
				System.err.println("CRASHED on connection NODATA");
				e.printStackTrace();
				System.exit(1);
			}
			
			//o.println("connection "+connectionNumber+" established");
		}
	}
}
