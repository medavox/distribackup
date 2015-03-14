package com.medavox.distribackup.peers;

import java.io.IOException;

import com.medavox.distribackup.connections.ConnectionOperator;

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
		owner.threadsEnabled = false;//tell looping threads to shut down
		//announce exit
		for(PeerInfo p : owner.peers.values())
		{
			if(p.hasOpenConnection())
			{
				ConnectionOperator co = p.getOpenConnection();
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

}
