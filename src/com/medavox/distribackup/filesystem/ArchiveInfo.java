package com.medavox.distribackup.filesystem;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.medavox.distribackup.connections.*;

/**Mutable extension of ArchiveInfo. Used for
maintaining info about local archive state, global archive state, Files to download*/
public class ArchiveInfo extends UpdateAnnouncement implements Communicable
{
    ConcurrentMap<String, FileInfo> files = new ConcurrentHashMap<String, FileInfo>();
    public ArchiveInfo(long GRN, FileInfo[] fileInfos)
    {
    	super(GRN, fileInfos);
    	for(FileInfo fi : fileInfos)
    	{
    		files.putIfAbsent(fi.toString(), fi);
    	}
    }
    /**Add or update a FileInfo in the ArchiveInfo object. If this path/name is 
     *already in use, then the previous value is replaced with the supplied one.*/
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
    /**Retrieves the FileInfo with the same path/name as 
     * the supplied Path object. If there isn't any such FileInfo in this
     * ArchiveInfo object, then return null*/
    public FileInfo getFileInfoFromPath(Path p)
    {//we're reverse-engineering a toString() of our desired FileInfo 
    	String name = p.getFileName().toString();
    	String path = p.getParent().toString();
    	
    	return files.get(name+path);
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
