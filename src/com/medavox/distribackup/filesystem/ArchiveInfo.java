package com.medavox.distribackup.filesystem;

import com.medavox.distribackup.connections.*;

public class ArchiveInfo implements Communicable
{
    protected long globalRevNum;
    private FileInfo[] changedFiles;
    public ArchiveInfo(long GRN, FileInfo[] files)
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
}
