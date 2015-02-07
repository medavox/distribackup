package com.medavox.distribackup.filesystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.medavox.distribackup.connections.*;

/**Mutable extension of ArchiveInfo. Used for
maintaining info about local archive state, global archive state, Files to download*/
public class FileSet extends ArchiveInfo implements Communicable
{
    ConcurrentMap<String, FileInfo> files = new ConcurrentHashMap<String, FileInfo>();
    public FileSet(long GRN, FileInfo[] fileInfos)
    {
    	super(GRN, fileInfos);
    	for(FileInfo fi : fileInfos)
    	{
    		files.putIfAbsent(fi.toString(), fi);
    	}
    }
    
    public void update(long newGRN, FileInfo[] newFiles)
    {//replace any obsolete FileInfos, such as lower revision numbers than these new entries
    	globalRevNum = newGRN;
    	for(FileInfo fi : newFiles)
    	{
    		if(files.containsKey(fi.toString()))
    		{
    			files.replace(fi.toString(), fi);
    		}
    		else
    		{
    			files.putIfAbsent(fi.toString(), fi);
    		}
    	}
    }
    
    public int getSize()
    {
    	return files.size();
    }
    
    public long getTotalFileSize()
    {
    	long sum = 0;
    	for(FileInfo fi : files.values())
    	{
    		sum += fi.getFileSize();
    	}
    	return sum;
    }
    
    public void remove(String filePath)
    {
        files.remove(filePath);
    }
    
}
