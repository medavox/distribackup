package com.medavox.distribackup.filesystem;

import com.medavox.distribackup.connections.*;

public class UpdateAnnouncement implements Communicable
{
    protected long globalRevNum;
    private FileInfo[] changedFiles;
    public UpdateAnnouncement(long GRN, FileInfo[] files)
    {
        globalRevNum = GRN;
        this.changedFiles = files;
    }
    
    public long getGRN()
    {
        return globalRevNum;
    }
    
    public long getGlobalRevisionNumber()
    {
        return globalRevNum;
    }
    
    public FileInfo[] getFiles()
    {
        return changedFiles;
    }
    //avoid implementing chunks for now
    
    public String toString()
    {
    	String s = "";
    	s+="Update Announcement; GRN:"+globalRevNum;
    	s+="Number of files:"+changedFiles.length;
    	
    	return s;
    }
}
