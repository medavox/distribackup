package com.medavox.distribackup.connections;

import java.util.Arrays;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.UnsupportedEncodingException;

import java.nio.file.Path;

import com.medavox.distribackup.peers.PeerInfo;
import com.medavox.distribackup.filesystem.FileUtils;
import com.medavox.distribackup.filesystem.FileInfo;
import com.medavox.distribackup.filesystem.DirectoryInfo;

/**Logic in this class just pertains to conversion of primitive types to and from binary.
 * PeerInfo converts to and from binary with methods in its own class. Note that
 * byte arrays constructed by this class do not contain an IDByte, as not all uses
 * of every given field requires it. Instances which need one, should add it manually.
 * 
 * Byte arrays passsed to methods in this class should correspondingly have 
 * their IDBytes removed, and except for complex or compound types, also their length.
 * Simple variable length types such as String do not need this field. */
public abstract class BinaryTranslator
{
	/**TODO:
	byte   -> byte		N/A
	ubyte  -> byte		DONE
	short  -> bytes		DONE
	ushort -> bytes		DONE
	int    -> bytes		DONE
	uint   -> bytes		DONE
	long   -> bytes		DONE
	ulong  -> bytes
	 
	byte  -> byte		N/A
	byte  -> ubyte		DONE
	bytes -> short		DONE
	bytes -> ushort		DONE
	bytes -> int		DONE
	bytes -> uint		DONE
	bytes -> long		DONE
	bytes -> ulong
	
	
	bitfield	-> byte			DONE
	byte		-> bitfield		DONE
	
	String		-> bytes		DONE
	bytes		-> String		DONE

	PeerInfo	-> bytes
	byte		-> PeerInfo
	
	FileInfo	-> bytes		DONE
	bytes		-> FileInfo		DONE
	
DirectoryInfo	-> bytes
bytes			-> DirectoryInfo
	
	FileData	-> bytes
	bytes 		-> FileData
	
	Address		-> bytes
	bytes		-> Address
	
	List		-> bytes
	bytes		-> List
	
	HList		-> bytes
	bytes		-> HList*/
	
	/**Primitive types omit a header by default; 
	add one manually or write wrapper methods to add one*/
	static ByteArrayOutputStream ba = new ByteArrayOutputStream();
	static DataOutputStream dos = new DataOutputStream(ba);
	
	public static byte ubyteToByte(short l) throws IOException, NumberFormatException
	{
		if(l > 255
		|| l < 0)
		{
			throw new NumberFormatException("ubyte value must be between 0 and 255!");
		}
		//+128
		return (byte)(l & 0xff); 
	}

	public static byte[] shortToBytes(short l) throws IOException
	{
		ba.reset();//reset byte output buffer
		dos.writeShort(l);
		return ba.toByteArray();
	}
	
	public static byte[] uShortToBytes(char l)
	{
		byte[] out = new byte[2];
		out[0] = (byte)((l >> 8) & (byte)0xff);
		out[1] = (byte)(l & (byte)0xff);
		return out;
	}
	
	public static byte[] intToBytes(int l) throws IOException
	{
		ba.reset();//reset byte output buffer
		dos.writeInt(l);
		return ba.toByteArray();
	}
	
	public static byte[] uintToBytes(long l)throws NumberFormatException//TODO: fix broken method(s)
	{//pronblem: our "uint" is stored in a long:8bytes wide
	 //the uint has to be 4bytes wide, so we may lose precision 
	 //based on what number is converted
		if(l < 0
		|| l > Math.pow(2, 32) - 1)
		{
			throw new NumberFormatException
			("ERROR: Specified long falls outside uint range!\nvalue: "+l);
		}
		byte[] out = new byte[4];
		out[3] = (byte)((l >> 24) & (byte)0xff);
		out[2] = (byte)((l >> 16) & (byte)0xff);
		out[1] = (byte)((l >>  8) & (byte)0xff);
		out[0] = (byte)(l & (byte)0xff);
		return out;
	}
	
	public static byte[] longToBytes(long l) throws IOException
	{
		ba.reset();//reset byte output buffer
		dos.writeLong(l);
		return ba.toByteArray();
	}
	
	public static byte bitfieldToByte(boolean... bools) throws Exception
	{
		byte out = (byte)0;
		if(bools.length > 8)
		{
			throw new NumberFormatException("Too many booleans to fit inside a byte!"); 
		}
		else
		{
			for(int i = 0; i < bools.length; i++)
			{//fill fields up from lsb first
				if(bools[i])
				{
					byte set = (byte)(0x1 << i);
					out |= set;
				}
			}
		}
		return out;
	}
		
	public static byte[] stringToBytes(String s) throws UnsupportedEncodingException
	{
		return s.getBytes("UTF-16");
	}
	
	/**This path should be relative to the repo root*/
	public static byte[] fileInfoToBytes(File f) throws IOException//TODO
	{/*
		0. (ID Byte)        | a byte
		0. (Length)         | Integer
		1. Name             | String
		2. Path             | String
		3. file size        | ULong
		4. revision number  | ULong
		5. checksum         | SHA1*/
		
		//make sure File object is an actual file (not a directory) before starting
		if(!f.isFile())
		{
			throw new IOException("ERROR: supplied File Object is not actually a file!");
		}
		
		//ERK: all conversions to binary have so far omitted the IDByte
		//and have not needed a length field. Are we going to keep compound types
		//consistent with primitive types? YES
		//byte[] byteID = {Message.FILE_INFO.IDByte};
		
		//i tried sticking to the new Path interface,
		//but it has no methods for filesize checking!
		
		byte[] name = stringToBytes(f.getName());
		byte[] path = stringToBytes(f.getPath());
		byte[] fileSize = longToBytes(f.length());//TODO: implement ulongToBytes
		byte[] revNum = longToBytes((long)0);//TODO: NOT YET IMPLEMENTED
		byte[] checksum = FileUtils.checksum(f);
		
		byte[] messageLength = 
		intToBytes(name.length + path.length + fileSize.length + revNum.length +
											checksum.length);
		
		return concat(messageLength, name, path, fileSize, revNum, checksum);
	}
	
	/**DirInfo objects are going to massive, due to their tree nature.
	 Converting a directory into byte-form will invoLve converting all objects
	 * inside it, which also contain their own files. WHOA*/
	/*public static byte[] dirInfoToBytes(File f) // TODO
	{
		//make sure File object is a directory (not a file) before starting
		if(f.isFile())
		{
			throw new IOException("ERROR: supplied File Object is not a Directory!");
		}
		
		
	}*/
	
	public static short byteToUByte(byte b) throws IOException, EOFException
	{//crazy, crazy manual bit manipulation to get an unsigned value from a byte
		short accumulator = 1;
		short converted = 0;
		for(int i = 0; i < 8; i++)
		{
			byte mask = 0x1;
			byte test = (byte)((b >> i) & mask);
			if(test == 1)
			{
				converted += accumulator;
			}
			accumulator *= 2;
		}
		return converted;
	}

	public static short bytesToShort(byte[] b) throws IOException, EOFException, NumberFormatException
	{
		if(b.length != 2)
		{
			throw new NumberFormatException("wrong number of bytes to convert to int!");
		}
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b) );
		return dis.readShort();
	}
	
	public static char bytesToUShort(byte[] b) throws IOException, EOFException, NumberFormatException
	{//this is the only bytes-to-unsigned conversion that can use the built-in method
		if(b.length != 2)
		{
			throw new NumberFormatException("wrong number of bytes to convert to int!");
		}
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b) );
		return dis.readChar();
	}
	
	public static int bytesToInt(byte[] b) throws IOException, EOFException, NumberFormatException
	{
		if(b.length != 4)
		{
			throw new NumberFormatException("argument has wrong number of bytes to convert to int!");
		}
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b) );
		return dis.readInt();
	}
	
	public static long bytesToUInt(byte[] b)//TODO: fix broken method(s)
	{
		if(b.length != 4)
		{
			throw new NumberFormatException("argument has wrong number of bytes to convert to uint!");
		}
		long accumulator = 1;
		long converted = 0;
		for(int h = 0; h < 4; h++)//per-byte loop
		{
			for(int i = 0; i < 8; i++)//per-bit loop
			{
				byte mask = 0x1;
				byte test = (byte)((b[h] >> i) & mask);
				if(test == 1)
				{
					converted += accumulator;
				}
				accumulator *= 2;
			}
		}
		return converted;
	}
	
	public static long bytesToLong(byte[] b) throws IOException, EOFException
	{
		if(b.length != 8)
		{
			throw new NumberFormatException("wrong number of bytes to convert to long!");
		}
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b) );
		return dis.readLong();
	}
	
	public static boolean[] byteToBitfield(byte in, int numFields)
	{
		if(numFields > 8)
		{
			System.err.println("WARNING: number of requested fields from a byte was >8!\nassuming 8 fields.");
			numFields = 8;
		}
		else if(numFields < 1)
		{
			throw new NumberFormatException("byte must specify at least one field!");
		}
		
		boolean[] fields = new boolean[numFields];
		
		for(int i = 0; i < numFields; i++)
		{
			byte mask = (byte)(0x1 << i);
			byte test = (byte)(in & mask);
			fields[i] = (test != 0);
		}
		return fields;
	}
	
	public static String bytesToString(byte[] b) throws UnsupportedEncodingException
	{
		return new String(b, "UTF-16");
	}

	public static FileInfo bytesToFileInfo(byte[] b)
	{/*	1. Name             | String
		2. Path             | String
		3. file size        | Long
		4. revision number  | Long
		5. checksum         | SHA1*/
		try
		{
			int nameEndIndex = getNextMessageEndIndex(b, 0);
			String name = bytesToString(Arrays.copyOfRange(b, 4, nameEndIndex));
			int pathEndIndex = getNextMessageEndIndex(b, nameEndIndex);
			String path = bytesToString(Arrays.copyOfRange(b, nameEndIndex+4, pathEndIndex));
			
			long fileSize = bytesToLong(Arrays.copyOfRange(b, pathEndIndex+8, pathEndIndex+16));
			long revisionNumber = bytesToLong(Arrays.copyOfRange(b, pathEndIndex, pathEndIndex+8));
			
			//there should be 20 bytes left, and they should all be the checksum
			int checksumBegin = pathEndIndex+16;
			byte[] checksum = Arrays.copyOfRange(b, checksumBegin, b.length);
			
			assert checksum.length == 20;
			
			//finally, reconstruct the FileInfo object from the decoded data
			FileInfo rxFileInfo = new FileInfo(name, path, fileSize, revisionNumber, checksum);
			
			return rxFileInfo;
		}
		catch(UnsupportedEncodingException usee)
		{
			//do nothing, the encoding name never changes
		}
		catch(Exception e)//TODO proper exception handling, it's all bubbled here
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/*public static PeerInfo bytesToPeerInfo(byte[] b)//TODO
	{
		
	}*/
	
	/**Pass a bunch of bytes which represent several Messages, with the first 4
	 * bytes being the length of the first Message payload.
	 * Useful for decoding String objects inside compound Messages.*/
	/*public static byte[] getNextBytes(byte[] b)
	{
		
	}*/
	/**Returns the index of the first byte after the end of the first Message.
	 * So for a message that spans from 0 to 35, is 32 bytes long (plus length bytes),
	 * this method will return 36*/
	public static int getNextMessageEndIndex(byte[] b, int offset) throws IOException
	{
		int len = bytesToInt(Arrays.copyOfRange(b, offset, 4));
		return len;
	}
	
	/**Convenience wrapper to handle a leading non-array byte*/
	public static byte[] concat(byte b, byte[]... bytes)
	{
		byte[] bArray = {b};
		concat(bytes);
		
		byte[][] out = new byte[bytes.length+1][];
		out[0] = bArray;
		for(int i = 1; i < out.length; i++)
		{
			out[i] = bytes[i-1];
		}
		return concat(out);
	}
	
	public static byte[] concat(byte[]... bytes)
	{
		//count how many bytes in total we have
		int count = 0;
		for(int i = 0; i < bytes.length; i++)
		{
			for(int j = 0; j < bytes[i].length; j++)
			{
				count++;
			}
		}
		//write every byte in argument arrays into ret, in order
		byte[] ret = new byte[count];
		int index = 0;
		for(int i = 0; i < bytes.length; i++)
		{
			for(int j = 0; j < bytes[i].length; j++)
			{
				ret[index] = bytes[i][j];
				index++;
			}
		}
		return ret;
	}
}
