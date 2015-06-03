package com.medavox.distribackup.filesystem;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.medavox.distribackup.connections.*;

/**Mutable extension of FileInfoBunch. Used for
maintaining changeable info about local archive state, global archive state, Files to download*/
public class ArchiveInfo extends FileInfoBunch implements Iterable<FileInfo>, Iterator<FileInfo>
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
    
    public ArchiveInfo(FileInfo[] fileInfos)
    {
    	for(FileInfo fi : fileInfos)
    	{
    		files.putIfAbsent(fi.toString(), fi);
    	}
    }
    
    public ArchiveInfo(FileInfo fi)
    {
    	files.putIfAbsent(fi.toString(), fi);
    }
    
    public ArchiveInfo()
    {
    	//we don't need the GRN, and the files collection can start empty
    }
    
    /**Add or update FileInfo(s) in the ArchiveInfo object. If this path/name is 
     *already in use, then the previous value is replaced with the supplied one.
     * If the archive already contains a corresponding FileInfo whose revision number 
     * is the same or greater than the update, then the new FileInfo is not added.
     *@return the number of files which were added*/
    public int update(long newGRN, FileInfo[] newFiles)
    {//replace any obsolete FileInfos, such as lower revision numbers than these new entries
    	globalRevNum = newGRN;
    	return update(newFiles);
    }
    
    public void setGRN(long newGRN)
    {
    	globalRevNum = newGRN;
    }
    
    public boolean update(FileInfo fi)
    {
    	if(files.containsKey(fi.toString()))
		{
			FileInfo oldEntry = files.get(fi.toString());
			if(oldEntry.getRevisionNumber() < fi.getRevisionNumber())
			{
				files.replace(fi.toString(), fi);
				return true;
			}
		}
		else
		{
			files.putIfAbsent(fi.toString(), fi);
			return true;
		}
    	return false;
    }
    
    public int update(FileInfo[] newFiles)
    {//replace any obsolete FileInfos, such as lower revision numbers than these new entries
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
    	Path q = p.getParent();
    	String path = (p == null ? "" : q.toString());
    	
    	String sep = FileSystems.getDefault().getSeparator();
    	
    	return files.get(path+sep+name);
    }
    
    public boolean contains(String file)
    {
    	return files.containsKey(file);
    }
    
    public boolean contains(FileInfo fi)
    {
    	return files.containsKey(fi.toString());
    }
    
    public String printAllFiles()
    {
    	String s = "";
    	for(FileInfo fi : files.values())
    	{
    		s+="\n"+fi.toString();
    	}
    	return s+"\n";
    }
    
    public FileInfo getFileInfo(String s)
    {
    	return files.get(s);
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
    
    public boolean remove(String filePath)
    {
    	return (files.remove(filePath) != null);
    }
    
    public boolean remove(FileInfo fi)
    {
    	return (files.remove(fi.toString()) != null);
    }
    
}
