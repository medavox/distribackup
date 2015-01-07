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
public class BinaryTranslator
{
	/**TODO:
	byte   -> byte		N/A
	ubyte  -> byte		DONE
	short  -> bytes		DONE
	ushort -> bytes
	int    -> bytes		DONE
	uint   -> bytes
	long   -> bytes		DONE
	ulong  -> bytes
	 
	byte  -> byte		N/A
	byte  -> ubyte		DONE
	bytes -> short		DONE
	bytes -> ushort		DONE
	bytes -> int		DONE
	bytes -> uint
	bytes -> long		DONE
	bytes -> ulong
	
	
	bitfield	-> byte			DONE
	byte		-> bitfield		DONE
	
	String		-> bytes        DONE
	bytes		-> String       DONE

	PeerInfo    -> bytes
	byte		-> PeerInfo
	
	FileInfo	-> bytes
	bytes       -> FileInfo
	
	DirectoryInfo   -> bytes
	bytes           -> DirectoryInfo
	*/
	
	/**Primitive types omit a header by default; 
	add one manually or write wrapper methods to add one*/
	static DataOutputStream dos;
	static ByteArrayOutputStream ba = new ByteArrayOutputStream();
	public BinaryTranslator()
	{
		dos = new DataOutputStream(ba);
	}
	
	public static byte UByteToByte(short l) throws IOException, NumberFormatException
	{
		if(l > 255
		|| l < 0)
		{
			throw new NumberFormatException("number must be between 0 and 255!");
		}
		//+128
		return (byte)((l & 0xff)+128); 
	}

	public static byte[] shortToBytes(short l) throws IOException
	{
		ba.reset();//reset byte output buffer
		dos.writeShort(l);
		return ba.toByteArray();
	}
	
	public static byte[] intToBytes(int l) throws IOException
	{
		ba.reset();//reset byte output buffer
		dos.writeInt(l);
		return ba.toByteArray();
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
					byte mask = (byte)(0x1 << i);
					out &= mask;
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
		0. (Length)         | UInteger
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
											checksum.length);//TODO: uintToBytes
		
		return concat(messageLength, name, path, fileSize, revNum, checksum);
	}
	
	/**DirInfo objects are going to massive, due to their tree nature.
	 Converting a directory into byte-form will invovle converting all objects inside it,
	 * which also contain their own files. WHOA*/
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
	
	public static int bytesToUShort(byte[] b) throws IOException, EOFException, NumberFormatException
	{//this is the only bytes-to-unsigned conversion that can use the built-in method
		if(b.length != 2)
		{
			throw new NumberFormatException("wrong number of bytes to convert to int!");
		}
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b) );
		return dis.readUnsignedShort();
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
	
	public static long bytesToUInt(byte[] b) throws IOException, EOFException, NumberFormatException
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
	
	public static boolean[] byteToBitfield(byte in, byte numFields)
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
		3. file size        | ULong
		4. revision number  | ULong
		5. checksum         | SHA1*/
		long  = 
		String name = bytesToString(Arrays.copyOfRange(b, 0, 
	}
	
	/*public static PeerInfo bytesToPeerInfo(byte[] b)
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
	public static long getEndOfNextMessage(byte[] b)
	{
		long len = bytesToUInt(Arrays.copyOfRange(b, 0, 4));
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
