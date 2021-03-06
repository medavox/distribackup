package com.medavox.distribackup.filesystem;

import java.util.LinkedList;
import java.util.List;

import com.medavox.distribackup.connections.*;
/**A read-only collection of FileInfo objects, and a revision number.*/
public class FileInfoBunch implements Communicable
{
    protected long globalRevNum;
    private FileInfo[] files;
    
    /*public static FileInfoBunch merge(FileInfoBunch a, FileInfoBunch b)
    {
    	List<FileInfo> joined = new LinkedList<FileInfo>();
    	long mergedGRN = Math.max(a.globalRevNum, b.globalRevNum);
    	
    	
    }*/
    public FileInfoBunch(long GRN, FileInfo[] files)
    {
        globalRevNum = GRN;
        this.files = files;
    }
    
    public FileInfoBunch(FileInfo[] files)
    {
    	globalRevNum = -1;
    	this.files = files;
    }
    
    public FileInfoBunch(FileInfo fi)
    {
    	globalRevNum = -1;
    	FileInfo[] wrapper = {fi};
    	files = wrapper;
    }
    
    public FileInfoBunch()
    {
    	globalRevNum = -1;
    	files = new FileInfo[0];
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
        return files;
    }
    //avoid implementing chunks for now
    
    public ArchiveInfo toArchiveInfo()
    {
    	return new ArchiveInfo(globalRevNum, files);
    }
    
    public String toString()
    {
    	String s = "";
    	s+="Update Announcement; GRN:"+globalRevNum;
    	s+="Number of files:"+files.length;
    	
    	return s;
    }
}
