
//import com.medavox.util.Log
import java.util.*;
import java.net.*;
import java.io.*;
public class Connection extends Thread
{
	private BufferedInputStream bis;
	private BufferedOutputStream bos;
	ServerSocket svr;
	String host;
	int port;
	boolean serverMode;
	boolean canRun = true;
	Socket s;
	
	public Connection(int port)//this is an incoming connection
	{
		serverMode = true;
		this.port = port;
		this.start();
	}
	public Connection(String host, int port)//this is an outgoing connection
	{
		serverMode = false;
		this.port = port;
		this.host = host;
		this.start();
	}
	
	public void getNext()
	{
		
	}
	private static void leh(Exception e)
	{
		System.err.println("ERROR! meh, just dump it and exit.");
        e.printStackTrace();
        System.exit(1);
	}
	public void run()
	{
		try
		{
			if(serverMode)
			{
				//setup
				svr = new ServerSocket(port);
				s = svr.accept();
			}
			else
			{//the basic unit of communication will be a custom message class, with a payload, name, etc
				s = new Socket(host, port);
			}
			bis = new BufferedInputStream(s.getInputStream());
			bos = new BufferedOutputStream(s.getOutputStream());
		}
		catch(Exception e){Connection.leh(e);}
	}
	
	public void close()
	{
	    try
	    {
		bis.close();
		bos.close();
		s.close();
		if(serverMode)
		{
		    svr.close();
		}
	    }
	    catch(IOException eio)
	    {
		System.err.println("Unable to close Connection!");
		leh(ioe);
	    }
	}
	
}
