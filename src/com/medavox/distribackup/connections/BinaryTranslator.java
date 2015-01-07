package com.medavox.distribackup.connections;

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

/**Logic in this class just pertains to conversion of primitive types to and from binary.
 * PeerInfo converts to and from binary with methods in its own class
 * */
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
	static DataOutputStream dos;
	//DataInputStream dis;
	static ByteArrayOutputStream ba = new ByteArrayOutputStream();
	//ByteArrayInputStream bais = new ByteArrayInputStream();
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
			throw new NumberFormatException("wrong number of bytes to convert to int!");
		}
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b) );
		return dis.readInt();
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
	/*
	public static byte[] generateFileInfoBytes(Path p)
	{
		/*
		0. (ID Byte)        | a byte
		0. (Length)         | UInteger
		1. Name             | String
		2. Path             | String
		3. file size        | ULong
		4. revision number  | ULong
		5. checksum         | SHA1*
		
		byte byteID = Message.FILE_INFO.IDByte;
		long length;
		
	}*/
    
    public static String bytesToString(byte[] b) throws UnsupportedEncodingException
    {
        return new String(b, "UTF-16");
    }

    /*public static PeerInfo bytesToPeerInfo(byte[] b)
    {
        
    }*/
	
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
