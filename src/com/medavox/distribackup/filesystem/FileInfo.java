package com.medavox.distribackup.filesystem;

import java.nio.file.FileSystems;

/**An immutable object whose purpose is to 
 * 1) uniquely identify a file, and
 * 2) provide Distribackup-specific information about it (checksum, revision number). 
 * Does not have to refer to an extant file. Consider referencing Java's File class AMAP*/
public class FileInfo extends FileSystemObjectInfo
{/*
3. file size        | ULong
4. revision number  | ULong
5. checksum         | SHA1*/
//TODO: we're going to have a problem when a FileInfo is passed from a windows to a unix system:
//the path separators will switch! 
	private long revisionNumber;//the spec called for ULongs here and here,
	private long fileSize;//but this is extremely inconvenient in Java
	private byte[] checksum;
    private boolean isDirectory;
	
	public FileInfo(String name, String path, long fileSize, long revisionNumber, byte[] checksum)
	{
        isDirectory = false;
		this.name = name;
		this.path = path;
		this.revisionNumber = revisionNumber;
		this.fileSize = fileSize;
		this.checksum = checksum;
	}
    /**Constructor for a FileInfo which represents a directory.*/
    public FileInfo(String name, String path)
    {
        isDirectory = true;
        this.name = name;
        this.path = path;
        
        revisionNumber = -1;
        fileSize = -1;
        checksum = new byte[0];
    }
    
    public boolean isDirectory()
    {
        return isDirectory;
    }
	
	public long getRevisionNumber()
	{
		return revisionNumber;
	}
	
	public long getFileSize()
	{
		return fileSize;
	}
	
	public byte[] getChecksum()
	{
		return checksum;
	}
	
	public String toString()
	{
		String sep = FileSystems.getDefault().getSeparator();
		return (path.length() == 0 ? "" : path+sep)+name;
	}
	
	/**debug version*/
	/*public String toString()//TODO : check up on correct separators, and that there is one between path and name
	{
		String s = "";
		s+= "FileInfo path: \""+path+
		"\" name: \""+name+
		"\" is Directory:"+isDirectory;
		s += (isDirectory ? "" : "\" revision number: "+revisionNumber+" file size: "+fileSize);
		//return path + name;
		return s;
	}*/
}
