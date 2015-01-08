package com.medavox.distribackup.filesystem;

/**An immutable object whose purpose is to 
 * 1) uniquely identify a file, and
 * 2) provide Distribackup-specific information about it (checksum, revision number). 
 * Does not have to refer to an extant file. Consider referencing File Object AMAP*/
public class FileInfo extends FileSystemObjectInfo
{/*
3. file size        | ULong
4. revision number  | ULong
5. checksum         | SHA1*/

	private long revisionNumber;//currently the spec calls for ULongs here,
	private long fileSize;//but this is extremely inconvenient in Java
	private byte[] checksum;
	
	public FileInfo(String name, String path, long fileSize, long revisionNumber, byte[] checksum)
	{
		this.name = name;
		this.path = path;
		this.revisionNumber = revisionNumber;
		this.fileSize = fileSize;
		this.checksum = checksum;
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
}
