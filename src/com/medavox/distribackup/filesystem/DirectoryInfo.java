package com.medavox.distribackup.filesystem;

/**Primarily used as a reply to a File Tree Status Request,
with a serialised DirectoryInfo as root dir*/
public class DirectoryInfo extends FileSystemObjectInfo
{
	FileSystemObjectInfo[] contents;

	public DirectoryInfo(String name, String path, FileSystemObjectInfo[] contents)
	{
		this.name = name;
		this.path = path;
		this.contents = contents;
	}
	
	public FileSystemObjectInfo[] getContents()
	{
		return contents;
	}
}
