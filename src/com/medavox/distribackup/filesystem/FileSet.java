package com.medavox.distribackup.filesystem;

import com.medavox.distribackup.connections.*;

/**Mutable extension of ArchiveInfo. Used for
 * maintaining info about local archive state, global archive state, Files to downkload*/
public class FileSet extends ArchiveInfo implements Communicable
{
    private FileInfo[] changedFiles;
    
    ConcurrentMap<String, FileInfo> files = new ConcurrentHashMap<String, FileInfo>();
    public ArchiveInfo(long GRN, FileInfo[] files)
    {
        globalRevNum = GRN;
        this.changedFiles = changedFiles;
    }
    
    public void update(long newGRN, FileInfo[] newFiles)
    {//replace any obsolete FileInfos, such as lower revision numbers than these new entries
        
    }
    
    public void remove(String filePath)
    {
        
    }
    
}
