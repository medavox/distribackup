package com.medavox.distribackup.peers;

import java.io.IOException;

import com.medavox.distribackup.connections.ConnectionOperator;
/**Performs cleanup tasks when the program is closed.*/
public class CloseHook extends Thread
{
	Peer owner;
	CloseHook(Peer owner)
	{
		this.owner = owner;
	}
	@Override
	public void run()
	{
		System.out.println(/*^C*/"losing Time!");
		owner.threadsEnabled = false;//tell looping threads to cease looping
		//announce exit
		for(ConnectionOperator co : owner.openConnections)
		{
			//System.out.println("co:"+co);
			try
			{
				co.announceExit();
				co.close();
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			}
			
		}

	}

}
