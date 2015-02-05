package com.medavox.distribackup.connections;
public class ReceivedMessage
{
    private Message type;
    private UUID fromPeer;
    private ConnectionOperator fromConnection;
    private Communicable details = null;
    private boolean hasCommunicable = false;
    
    public ReceivedMessage(Message msg, UUID fromPeer, ConnectionOperator fromConnection)
    {
        init(msg, fromPeer, fromConnection);
    }
    
    public ReceivedMessage( Message msg,
                            UUID fromPeer,
                            ConnectionOperator fromConnection,
                            Communicable details)
    {
        init(msg, fromPeer, fromConnection);
        this.details = details;
        hasCommunicable = true;
    }
    
    private void init(Message msg, UUID fromPeer, ConnectionOperator fromConnection)
    {
        this.type = msg;
        this.fromPeer = fromPeer;
        this.fromConnection = fromConnection;
    }
    
    public boolean hasCommunicable()
    {
        return hasCommunicable;
    }
    
    public UUID getUUID()
    {
        return fromPeer;
    }
    
    public Communicable getCommunicable()
    {
        return details;
    }
    
    public Message getMessage()
    {
        return type;
    }
    
    public Message getType()
    {
        return type;
    }
    
    public ConnectionOperator getConnection()
    {
        return fromConnection;
    }
}
