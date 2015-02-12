package com.medavox.distribackup.filesystem;

import com.medavox.distribackup.connections.*;

public class FileInfoBunch implements Communicable
{
    protected long globalRevNum;
    private FileInfo[] changedFiles;
    public FileInfoBunch(long GRN, FileInfo[] files)
    {
        globalRevNum = GRN;
        this.changedFiles = files;
    }
    
    public FileInfoBunch(FileInfo[] files)
    {
    	globalRevNum = -1;
    	this.changedFiles = files;
    }
    
    public FileInfoBunch(FileInfo fi)
    {
    	globalRevNum = -1;
    	FileInfo[] wrapper = {fi};
    	changedFiles = wrapper;
    }
    
    public FileInfoBunch()
    {
    	globalRevNum = -1;
    	changedFiles = new FileInfo[0];
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
    
    public ArchiveInfo toArchiveInfo()
    {
    	return new ArchiveInfo(globalRevNum, changedFiles);
    }
    
    public String toString()
    {
    	String s = "";
    	s+="Update Announcement; GRN:"+globalRevNum;
    	s+="Number of files:"+changedFiles.length;
    	
    	return s;
    }
}
