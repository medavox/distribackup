package com.medavox.distribackup.connections;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

public class IncomingMessageProcessor extends Thread
{
    Queue<Message> messageQueue = new ConcurrentLinkedQueue<Message>();
    public void run()
    {
        
    }
}
