package com.medavox.distribackup.connections;
public class ReceivedMessage
{
    private Message type;
    private UUID fromPeer;
    private ConnectionOperator fromConnection;
    Communicable details = null;
}
