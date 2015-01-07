package com.medavox.distribackup.filesystem;

/**Does not have to refer to an extant file. Consider referencing File AMAP*/
public class FileInfo extends FileSystemObjectInfo
{/*
3. file size        | ULong
4. revision number  | ULong
5. checksum         | SHA1*/

	private long revisionNumber;//currently the spec calls for ULongs here,
	private long fileSize;//but this is extremely inconvenient in Java
	private byte[] checksum;
	
	public FileInfo(String name, String path, )
	{
		
	}
}
