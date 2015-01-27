package com.medavox.distribackup.connections;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

public class IncomingMessageProcessor extends Thread
{
    Queue<Communicable> messageQueue = new ConcurrentLinkedQueue<Communicable>();
    public void run()
    {
        while(true)
        {//spins until there is something in the queue
            if(!messageQueue.isEmpty())
            {
                Communicable next = messageQueue.remove();
                //TODO
            }
        }
    }
}
