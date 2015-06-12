package com.medavox.distribackup.filesystem;

import com.medavox.distribackup.connections.Communicable;

/**Common features between files and directories are implemented here.*/
public abstract class FileSystemObjectInfo implements Communicable
{
	protected String name;
	protected String path;
	
	public String getName()
	{
		return name;
	}
	
	public String getPath()
	{
		return path;
	}
}
