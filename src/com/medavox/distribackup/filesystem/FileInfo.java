package com.medavox.distribackup.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import com.medavox.distribackup.peers.Peer;

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
	private long fileSize;		//but this is extremely inconvenient in Java
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
    /*
    public FileInfo(File f)
    {
    	
    }
    
    public FileInfo(File f, long revisionNumber)
    {
    	
    }*/
    
	public FileInfo(Path p, long revisionNumber)
	{
		Path relativePath = Peer.root.relativize(p);
		//System.out.println("passed path:   "+p);
		//System.out.println("relative path: "+relativePath);
    	this.name = relativePath.getFileName().toString();
    	Path relPath = relativePath.getParent();//use an empty string if the path is null
    	this.path = (relPath == null ? "" : relPath.toString());
    	
    	File asFile = p.toFile();
    	this.isDirectory = asFile.isDirectory();
		
    	if(!isDirectory)
    	{//is a file, so use the Filewise constructor for FileInfo
			try
			{
				this.fileSize = Files.size(p);
		
		    	this.checksum = FileUtils.checksum(asFile);
		    	this.revisionNumber = revisionNumber;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
    	}
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
