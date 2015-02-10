package com.medavox.distribackup.filesystem;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.medavox.distribackup.connections.*;

/**Mutable extension of ArchiveInfo. Used for
maintaining info about local archive state, global archive state, Files to download*/
public class ArchiveInfo extends UpdateAnnouncement implements Communicable, Iterable<FileInfo>, Iterator<FileInfo>
{
    ConcurrentMap<String, FileInfo> files = new ConcurrentHashMap<String, FileInfo>();
    private int currentIndex = 0;
    private String[] iters;
    public ArchiveInfo(long GRN, FileInfo[] fileInfos)
    {
    	super(GRN, fileInfos);
    	for(FileInfo fi : fileInfos)
    	{
    		files.putIfAbsent(fi.toString(), fi);
    	}
    }
    /**Add or update a FileInfo in the ArchiveInfo object. If this path/name is 
     *already in use, then the previous value is replaced with the supplied one.
     * If the archive already contains a corresponding FileInfo whose revision number 
     * is the same or greater than the update, then the new FileInfo is not added.
     *@return the number of files which were added*/
    public int update(long newGRN, FileInfo[] newFiles)
    {//replace any obsolete FileInfos, such as lower revision numbers than these new entries
    	globalRevNum = newGRN;
    	int numberOfUpdatesAdded = 0;
    	for(FileInfo fi : newFiles)
    	{
    		if(files.containsKey(fi.toString()))
    		{
    			FileInfo oldEntry = files.get(fi.toString());
    			if(oldEntry.getRevisionNumber() < fi.getRevisionNumber())
    			{
    				files.replace(fi.toString(), fi);
    				numberOfUpdatesAdded++;
    			}
    		}
    		else
    		{
    			files.putIfAbsent(fi.toString(), fi);
    			numberOfUpdatesAdded++;
    		}
    	}
    	return numberOfUpdatesAdded;
    }
    /**Retrieves the FileInfo with the same path/name as 
     * the supplied Path object. If there isn't any such FileInfo in this
     * ArchiveInfo object, then return null*/
    public FileInfo getFileInfoWithPath(Path p)
    {//we're reverse-engineering a toString() of our desired FileInfo 
    	String name = p.getFileName().toString();
    	String path = p.getParent().toString();
    	
    	return files.get(name+path);
    }
    
    public FileInfo getFileInfoWithPath(String s)
    {
    	return files.get(s);
    }
    
    //public FileInfo getFileInfo()
    
    public Iterator<FileInfo> iterator()
    {
    	currentIndex = 0;
    	Set<String> keys = files.keySet();
    	iters = new String[keys.size()];
    	keys.toArray(iters);
    	return this;
    }
    
    public boolean hasNext()
    {
    	return (currentIndex < iters.length);
    }
    
    public FileInfo next()
    {
    	currentIndex++;
    	return files.get(iters[currentIndex-1]);
    }
    
    public boolean containsKey(String s)
    {
    	return files.containsKey(s);
    }
    
    public void remove()//part of Iterator interface
    {
    	//not used/implemented
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
