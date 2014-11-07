import com.medavox.distribackup.peers.*;
import java.net.*;
public class Subscriber extends Peer
{
    public Subscriber(int port)
    {
        
    }
    
    public static void main (String args[])
    {//local listening port
		int port = 1210;
		Subscriber p = new Subscriber(port);
    }
    
    public void newSocket(Socket s)
    {
	/*open a different listening port
	 * connect to the publisher
	 * wait for update announcement, then
	 * wait for incoming files
	 * whenever one's finished, announce you have it and wait for requests
	 * if someone else announces they have it while you're waiting, request it from them
	 * */
    }
}

