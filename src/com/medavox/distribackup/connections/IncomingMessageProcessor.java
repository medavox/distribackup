package com.medavox.distribackup.connections;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

import com.medavox.distribackup.filesystem;

/**Singleton to handle all incoming messages, after they have been parsed from
 * incoming connections by ConnectionOperator.*/
public abstract class IncomingMessageProcessor extends Thread
{
    private static IncomingMessageProcessor IMP = null;
    private Queue<ReceivedMessage> messageQueue = new ConcurrentLinkedQueue<ReceivedMessage>();
    public void run()
    {
        while(true)
        {//spins until there is something in the queue
            if(!messageQueue.isEmpty())
            {
                ReceivedMessage next = messageQueue.remove();
                //TODO: probably a big old switch statement
                switch(next.getType())
                {/* Request For Peers
                    Request All Files
                    File Data Chunk
                    File Request
                    Greeting
                    Exit Announcement
                    File Tree Status Req
                    Update Announcement*/
                    case FILE_DATA_CHUNK:
                        handleFileDataChunk(next);
                    break;
                    
                    case FILE_REQUEST:
                        handleFileRequest(next);
                    break;
                    
                    case PEER_REQUEST:
                        handlePeerRequest(next);
                    break;
                    
                    case EXIT_ANNOUNCEMENT:
                        handleExitAnnouncement(next);
                    break;
                    
                    case REQ_ALL_FILES:
                        handleAllFilesRequest(next);
                    break;
                    
                    case TREE_STATUS_REQ:
                        handleFileTreeStatusRequest(next);
                    break;
                }
            }
        }
    }
    
    public void handleFileDataChunk(ReceivedMessage rxmsg)
    {
        //check file exists
        //if there are more pieces on the way before this update is finished,
        //store them in some kind of cache
        FileDataChunk fdc = (FileDataChunk)rxmsg.getCommunicable();
        if(fdc.isWholeFile())
        {//we have the whole file, check whether we have the whole UPDATE
            
        }
        else
        {
            
        }
        
    }
    
    public void handlePeerRequest(ReceivedMessage pr)
    {
        
    }
    
    public void handleFileRequest(ReceivedMessage fr)
    {
        
    }
    
    public void handleAllFilesRequest(ReceivedMessage afr)
    {
        
    }
    
    public void handleExitAnnouncement(ReceivedMessage ea)
    {
        
    }
    
    public void handleFileTreeStatusRequest(ReceivedMessage ftsr)
    {
        
    }
    
    public void addToQueue(ReceivedMessage rxmsg)
    {
        messageQueue.add(rxmsg);
    }
    
    private IncomingMessageProcessor()
    {
        
    }
    
    public static IncomingMessageProcessor getIncomingMessageProcessor()
    {
        return getIMP();
    }
    
    public static IncomingMessageProcessor getIMP()
    {
        if(IMP == null)
        {
            IMP = new IncomingMessageProcessor();
        }
        return IMP;
    }
    
}
