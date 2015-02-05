package com.medavox.distribackup.filesystem;

import com.medavox.distribackup.connections.Communicable;

public class FileDataChunk implements Communicable
{
    private FileInfo fileInfo;
    private long offset;
    private byte[] payload;
    
    public FileDataChunk(FileInfo fi, byte[] payload, long offset)
        throws NumberFormatException
    {
        if(offset < 0)
        {
            throw new NumberFormatException("Offset must not be negative!");
        }
        this.offset = offset;
        this.payload = payload;
        fileInfo = fi;
    }
    
    public boolean isWholeFile()
    {//if the file is >2GB, then this piece can't be the whole file anyway
        //because a byte[] could't hold all the data
        //or if there's any offset, then it's not the whole file
        if (fileInfo.getFileSize() > Integer.MAX_VALUE || offset > 0)
        {
            return false;
        }
        else if(offset == 0 && payload.length == fileInfo.getFileSize())
        {
            return true;
        }
        return false;
    }
    
    public byte[] getPayload()
    {
        return payload;
    }
    
    public FileInfo getFileInfo()
    {
        return fileInfo;
    }
    
    public long getOffset()
    {
        return offset;
    }
}
