package com.medavox.distribackup.filesystem;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.medavox.distribackup.connections.*;

/**Mutable extension of FileInfoBunch. Used for
maintaining changeable info about local archive state, global archive state,
or lists of needed files to download*/
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
    
    public void setGRN(long newGRN)
    {
    	globalRevNum = newGRN;
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
    
    public boolean update(FileInfo fi)
    {
    	if(files.containsKey(fi.toString()))
		{
			FileInfo oldEntry = files.get(fi.toString());
			if(oldEntry.getRevisionNumber() < fi.getRevisionNumber())
			{
				files.replace(fi.toString(), fi);
				globalRevNum++;
				return true;
			}
		}
		else
		{
			files.putIfAbsent(fi.toString(), fi);
			globalRevNum++;
			return true;
		}
    	return false;
    }
    /**Adds (or updates) file/directory entries. This method signature has proven to be kind of useless,
     * because a FileInfo object requires a valid revNum to be compared against the ArchiveInfo,
     * which can only be retrieved from checking this ArchiveInfo first.*/
    public int update(FileInfo[] newFiles)
    {//replace any obsolete FileInfos, such as lower revision numbers than these new entries
    	int numberOfUpdatesAdded = 0;
    	for(FileInfo fi : newFiles)
    	{
    		boolean added = update(fi);
    		if(added)
    		{
    			numberOfUpdatesAdded++;
    		}
    	}
    	if(numberOfUpdatesAdded > 0)
    	{//if we added any files, update the ArchiveInfo revision number
    		globalRevNum++;
    	}
    	return numberOfUpdatesAdded;
    }
    /**Adds (or updates) file/directory entries.
     * @return true if a file was added, false if not.*/
    public boolean update(Path p)
    {
    	FileInfo fi = getFileInfoWithPath(p);
    	if(fi == null)//a FileInfo of Path p doesn't exist in this ArchiveInfo
    	{
    		//System.out.println("file "+p+" is new");
    		FileInfo newFileInfo = new FileInfo(p, (long)0);
    		files.put(newFileInfo.toString(), newFileInfo);
    		globalRevNum++;
    		return true;
    	}
    	else//file already exists
    	{//TODO: checking whether extant FileInfo is newer than supplied Path Object
    		//System.out.println("file "+p+" already exists");
    		System.out.println(fi);
    		return false;
    	}
    }
    //TODO: there must a be a problem here
    /**Retrieves the FileInfo with the same path/name as 
     * the supplied Path object. If there isn't any such FileInfo in this
     * ArchiveInfo object, then return null*/
    public FileInfo getFileInfoWithPath(Path p)
    {//we're reverse-engineering a toString() of our desired FileInfo 
    	String name = p.getFileName().toString();
    	Path q = p.getParent();
    	String path = (q == null ? "" : q.toString());
    	
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
    
    public String toString()
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
    public int getNumEntries()
    {
    	return files.size();
    }
    public int getNumFiles()
    {
    	int numFiles = 0;
    	for(FileInfo fi : files.values())
    	{
    		if(!fi.isDirectory())
    		{
    			numFiles++;
    		}
    	}
    	return numFiles;
    }
    public int getNumDirectories()
    {
    	int numDirs = 0;
    	for(FileInfo fi : files.values())
    	{
			if(fi.isDirectory())
			{
				numDirs++;
			}
    	}
		return numDirs;
    }
}
