package com.medavox.distribackup.connections;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.EOFException;


import java.nio.file.Path;

import com.medavox.distribackup.peers.PeerInfo;

/**Try to keep logic in this class just pertaining to conversion to and from binary*/
public class BinaryTranslator
{
	/*BITFIELD		((byte)0x00,  2),
	STRING			((byte)0x01, -1),
	UBYTENUM		((byte)0x02,  2),
	USHORT			((byte)0x03,  3),
	UINTEGER		((byte)0x04,  5),
	ULONG			((byte)0x05,  9),
	BYTENUM			((byte)0x06,  2),
	SHORT			((byte)0x07,  3),
	INTEGER			((byte)0x08,  5),
	LONG			((byte)0x09,  9),*/
	
	/**TODO:
	ubyte  -> byte
	byte   -> byte
	ushort -> bytes
	short  -> bytes    DONE
	uint   -> bytes
	int    -> bytes    DONE
	ulong  -> bytes
	long   -> bytes    DONE
	 
	byte  -> ubyte
	byte  -> byte
	bytes -> ushort
	bytes -> shortm
	bytes -> uint
	bytes -> int
	bytes -> ulong
	bytes -> long      DONE
	
	
	bitfield		-> byte
	byte			-> bitfield
	
	String			-> bytes
	bytes			-> String

	FileInfo	    -> bytes
	bytes       	-> FileInfo
	 
	DirectoryInfo   -> bytes
	bytes           -> DirectoryInfo
	
	PeerInfo    	-> bytes
	byte			-> PeerInfo
	
	 */
	static DataOutputStream dos;
	//DataInputStream dis;
	static ByteArrayOutputStream ba = new ByteArrayOutputStream();
	//ByteArrayInputStream bais = new ByteArrayInputStream();
	public BinaryTranslator()
	{
		dos = new DataOutputStream(ba);
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
	
	public static long bytesToLong(byte[] b) throws IOException, EOFException
	{
		
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b) );
		return dis.readLong();
	}
	
	public static byte bitfieldToByte(boolean... bools) throws Exception
	{
		byte out = (byte)0;
		if(bools.length > 8)
		{
			throw new Exception("Too many booleans to fit inside a byte!"); 
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
	
	public static byte[] generateFileInfoBytes(Path p)
	{
		/*
		0. (ID Byte)        | a byte
		0. (Length)         | UInteger
		1. Name             | String
		2. Path             | String
		3. file size        | ULong
		4. revision number  | ULong
		5. checksum         | SHA1*/
		
		byte byteID = Message.FILE_INFO.IDByte;
		long length 
	}
	
	public static boolean[] byteToBitfield(byte in, byte numFields)
	{
		if(numFields > 8)
		{
			System.err.println("WARNING: number of requested fields from a byte was >8!\nassuming 8 fields.");
			numFields = 8;
		}
		boolean[] fields = new boolean[numFields];
		
		for(int i = 0; i < numFields; i++)
		{
			byte mask = (byte)(0x1 << i);
			byte test = in & mask;
			fields[i] = (test > 0);
		}
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
