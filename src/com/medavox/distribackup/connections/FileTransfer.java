package com.medavox.distribackup.connections;

import java.io.Serializable;
public class FileTransfer implements Serializable
{
    private String fileName;
    private String location;
    private byte[] data;
    
    public FileTransfer(String fileName, String location, byte[] data) 
    {
        this.fileName = fileName;
        this.location = location;
        this.data = data;
    }
}
