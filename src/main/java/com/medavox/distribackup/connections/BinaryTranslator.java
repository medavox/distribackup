package com.medavox.distribackup.connections;

import java.util.Arrays;
import java.util.UUID;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import com.medavox.distribackup.peers.PeerInfo;
import com.medavox.distribackup.filesystem.FileUtils;
import com.medavox.distribackup.filesystem.FileInfo;
import com.medavox.distribackup.filesystem.FileDataChunk;
import com.medavox.distribackup.filesystem.FileInfoBunch;

/**Logic in this class only pertains to conversion of primitive types to and from binary.
 * Byte arrays constructed by this class do not contain an IDByte, as not all uses
 * of every given field requires it. Instances which need one should add it manually.
 * <p>
 * Methods which decode bytes expect no IDByte or length field; this header is
 * only useful for parsing the Message payload from the network stream.
 * The Message length can be determined from the byte[].length anyway.
 * <p>
 * However, methods which encode data INTO bytes prepend the length field for convenience.
 * This means that bytes encoded with one method cannot be immediately passed
 * back to the corresponding decoding method without some processing first. */
public abstract class BinaryTranslator
{/**TODO:
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

	PeerInfo	-> bytes        DONE?
	bytes		-> PeerInfo     DONE?
	
	FileInfo	-> bytes		DONE?
	bytes		-> FileInfo		DONE?
	
	FileDataChunk-> bytes       DONE
	bytes 		-> FileDataChunkDONE
	
	Address		-> bytes        DONE
	bytes		-> Address      DONE
	
	List		-> bytes		DONE
	bytes		-> List			DONE
    
    UpdAnnounce -> bytes        DONE
    bytes       -> UpdAnnounce  DONE*/
	
	/**Primitive types omit a header by default; 
	add one manually or write wrapper methods to add one*/
	static ByteArrayOutputStream ba = new ByteArrayOutputStream();
	static DataOutputStream dos = new DataOutputStream(ba);
	private static PrintStream o = System.out;
	
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
	
	public static byte[] ushortToBytes(char l)
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
	
	public static byte[] uintToBytes(long l)throws NumberFormatException
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
	
	public static byte bitfieldToByte(boolean... bools) throws NumberFormatException
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
		
	public static byte[] stringToBytes(String s) throws UnsupportedEncodingException, IOException
	{
		byte[] stringBytes = s.getBytes("UTF-16");
		byte[] len = intToBytes(stringBytes.length); 
		return concat(len, stringBytes);
	}
	
	/**This path should be relative to the repo root*/
	/*public static byte[] fileInfoToBytes(File f) throws IOException
	{*//* 0. (ID Byte)        | a byte
		0. (Length)         | Integer
		1. Name             | String
		2. Path             | String
		3. file size        | Long
		4. revision number  | Long
		5. checksum         | SHA1*//*
		
		//make sure File object is an actual file (not a directory) before starting
		if(!f.isFile())
		{
			throw new IOException("ERROR: supplied File Object is not actually a file!");
		}

		//consider whether to include a length field in each compound type builder
		//byte[] byteID = {Message.FILE_INFO.IDByte};
		
		//i tried sticking to the new Path interface,
		//but it has no methods for filesize checking!
		
		//IDByte is omitted deliberately, for consistency with primitive types
		byte[] name = stringToBytes(f.getName());
		byte[] path = stringToBytes(f.getPath());
		byte[] fileSize = longToBytes(f.length());
		byte[] revNum = longToBytes((long)0);//TODO: REVNUMS NOT YET IMPLEMENTED
		byte[] checksum = FileUtils.checksum(f);
		
		byte[] messageLength = 
		intToBytes(getTotalLength(name, path, fileSize, revNum,	checksum));
		
		return concat(messageLength, name, path, fileSize, revNum, checksum);
	}*/
    
    public static byte[] fileInfoToBytes(FileInfo fi) throws UnsupportedEncodingException, IOException
    {/* 1. Name             | String
        2. Path             | String
        2. isDirectory      | bitfield<0>HAHA FOUND YOU
        3. file size        | Long
        4. revision number  | Long
        5. checksum         | SHA1*/
        byte[] name = stringToBytes(fi.getName());
        byte[] path = stringToBytes(fi.getPath());
        
        byte isDirectoryByte = bitfieldToByte(fi.isDirectory());
        byte[] isDirectory = {isDirectoryByte};
        if(fi.isDirectory())//if the FileInfo represents a directory, don't bother with the file-specific fields
        {
            byte[] length = intToBytes(getTotalLength(name, path)+1);//+1 for bitfield
            return concat(length, name, path, isDirectory);
        }
        byte[] fileSize = longToBytes(fi.getFileSize());
        byte[] revNum = longToBytes(fi.getRevisionNumber());
        byte[] checksum = fi.getChecksum();//checksums are already byte[]s
        byte[] length = intToBytes(getTotalLength(name, path, isDirectory, fileSize, revNum, checksum));
        return concat(length, name, path, isDirectory, fileSize, revNum, checksum);
    }
    
    public static byte[] fileDataChunkToBytes(FileDataChunk fd) throws UnsupportedEncodingException, IOException
    {/* 1. (Length)             | Integer
	    2. FileInfo             | FileInfo
	    3. isWholeFile          | bitfield<0>
	    4. offset               | Long
	    5. payload              | ByteArray*/
        byte[] fileInfoBin = fileInfoToBytes(fd.getFileInfo());
        //o.println("pre  fileInfo:"+BinTest.bytesToHex(Arrays.copyOfRange(fileInfoBin, 4, 16)));
        byte isWholeFileByte = bitfieldToByte(fd.isWholeFile());
        byte[] isWholeFile = {isWholeFileByte};
        byte[] offset = longToBytes(fd.getOffset());
        //o.println("pre  offset start:"+BinTest.bytesToHex(offset));
        //payload object is a Distribackup 'Byte Array', a TLV object which hasn't got conversion methods here
        byte[] payloadLength = intToBytes(fd.getPayload().length);
        
        byte[] data = fd.getPayload();
        
        byte[] payload = concat(payloadLength, data);//WARNING:
        
        //payload length could be long enough to make enclosing FileData length overrun!
        byte[] messageLength = intToBytes(getTotalLength(fileInfoBin, isWholeFile, offset, payload));
        
        //o.println("FDC raw:"+BinTest.bytesToHex(concat(messageLength, fileInfoBin, isWholeFile, offset)));
        return concat(messageLength, fileInfoBin, isWholeFile, offset, payload);
    }
	
	public static byte[] peerInfoToBytes(PeerInfo p, boolean isPublisher) throws IOException, NumberFormatException
	{/* 0. (ID Byte)           | a byte
		0. (Length)            | Integer
		1. UUID1               | Long
		2. UUID2               | Long
		4. isPublisherOrPeer   | bitfield<0>
		5. Addresses           | List:Address*/

		byte[] UUID1 = longToBytes(p.getUUID().getMostSignificantBits());
		byte[] UUID2 = longToBytes(p.getUUID().getLeastSignificantBits());
		//byte[] globalRevNum = longToBytes(p.getGRN());
		
		boolean[] bitfield = {isPublisher};
		//boolean[] bitfield = {true};// I'm Spartacus!
		byte[] isPubByte = {bitfieldToByte(bitfield)};
		
		Address[] adds = p.getAddresses();
		byte[][] addsBytes = new byte[adds.length][];
		for(int i = 0; i < adds.length; i++)
		{
			addsBytes[i] = addressToBytes(adds[i]);
		}
		byte[] addresses = listToBytes(addsBytes, Message.ADDRESS.IDByte);
		
		byte[] msgLength = 
		intToBytes(getTotalLength(UUID1, UUID2, /*globalRevNum,*/ isPubByte, addresses));
		
		return concat(msgLength, UUID1, UUID2, /*globalRevNum,*/ isPubByte, addresses);
	}
	/**Takes a holding array of pre-converted byte[]s and turns them into a 
	distribackup:List.
	The Message byte[] elements must be pre-sorted, contain no IDByte, 
	and only their length fields if the list is made of variable-length elements.*/
	public static byte[] listToBytes(byte[][] items, byte elIDByte) throws IOException
	{/* 1. (Length)             | Int
		2. ID byte of elements  | a byte
		3. number of elements   | int
		4. &lt;elements&gt;     | ?*/
		//o.println("pre  numElements:"+items.length);
		byte[] elIDByteWrapper = {elIDByte};
		byte[] numElements = intToBytes(items.length);
		//o.println("as bytes:"+BinTest.bytesToHex(numElements));
		byte[] elements = concat(items);
		byte[] messageLength = intToBytes(getTotalLength(numElements, elements)+1);//+1 for elements' IDByte
		//o.println("list elements pre-encode:"+getTotalLength(numElements, elements)+1);
		//o.println("as bytes:"+BinTest.bytesToHex(messageLength));
		return concat(messageLength, elIDByteWrapper, numElements, elements);
	}
	
	public static byte[] addressToBytes(Address addr) throws IOException, UnsupportedEncodingException
	{/* 0. (ID Byte)           | a byte
		1. (Length)            | Integer
		2. isUp                | bitfield<0>
		3. usingHostname(NotIP)| bitfield<1>
		4. USing IPv6          | bitfield<2>
		5. IP/hostname         | ByteArray.4/String
		6. listenPort          | UShort
		7. lastKnownTimeOnline | Long (ms since epoch)*/
		byte bitfield = bitfieldToByte(addr.isOnline(), addr.usingHostName(), addr.isIPv6());
		byte[] addrField;
		if(addr.usingHostName())
		{
			addrField = stringToBytes(addr.getHostName());
		}
		else
		{//whether it's 4 bytes or 16 ie IPv4 or IPv6, doesn't matter here. 
			addrField = addr.getRawIPAddress();//We know from the bitfield
		}
		byte[] port = ushortToBytes(addr.getPort());
		byte[] lastSpotted = longToBytes(addr.getLastKnownTimeOnline().getTime());
		
		byte[] bfWrapper = {bitfield};
		byte[] len = intToBytes(getTotalLength(bfWrapper, addrField, port, lastSpotted));
		return concat(len, bfWrapper, addrField, port, lastSpotted);
	}
    
    public static byte[] fileInfoBunchToBytes(FileInfoBunch ua) throws IOException
    {/* GRN		| Long
    	files	| fileInfolist*/
        byte[] GRN = longToBytes(ua.getGRN());
        
        byte[][] fileInfoBytes = new byte[ua.getFiles().length][];
        for(int i = 0; i < fileInfoBytes.length; i++)
        {
        	fileInfoBytes[i] = fileInfoToBytes(ua.getFiles()[i]);
        }
        byte[] fileInfoList = listToBytes(fileInfoBytes, Message.FILE_INFO.IDByte);
        
        byte[] length = intToBytes(GRN.length + fileInfoList.length);
        return concat(length, GRN, fileInfoList);
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
	
	public static char bytesToUShort(byte[] b) throws IOException, EOFException, NumberFormatException
	{//this is the only bytes-to-unsigned conversion that can use the built-in method
		if(b.length != 2)
		{
			throw new NumberFormatException("wrong number of bytes to convert to int!");
		}
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
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
	
	public static long bytesToUInt(byte[] b)
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
	{/* 1. Name             | String
        2. Path             | String
        2. isDirectory      | bitfield<0>
        3. file size        | Long
        4. revision number  | Long
        5. checksum         | SHA1*/
		try
		{
			int ptr = 0;
			//o.println("b length:"+b.length);
			int nameLength = bytesToInt(Arrays.copyOfRange(b, ptr, ptr+4));
			//System.out.println("post name length:"+nameLength);
			ptr += 4;
			String name = bytesToString(Arrays.copyOfRange(b, ptr, ptr+nameLength));
			ptr += nameLength;
			
			int pathLength = bytesToInt(Arrays.copyOfRange(b, ptr, ptr+4));
			//System.out.println("post path length:"+nameLength);
			ptr += 4;
			String path = bytesToString(Arrays.copyOfRange(b, ptr, ptr+pathLength));
			ptr += pathLength;
			
            boolean[] isDirectory = byteToBitfield(b[ptr], 1);
            ptr++;
            
            FileInfo rxFileInfo;
            
            if(isDirectory[0])//the file-specific fields are omitted if this is a directory
            {
                rxFileInfo = new FileInfo(name, path);
            }
            else
            {
                long fileSize = bytesToLong(Arrays.copyOfRange(b, ptr, ptr+8));
                ptr += 8;
                long revisionNumber = bytesToLong(Arrays.copyOfRange(b, ptr, ptr+8));
                ptr += 8;
                
                //there should be 20 bytes left, and they should all be the checksum
                byte[] checksum = Arrays.copyOfRange(b, ptr, b.length);
                
                //System.out.println("checksum length: "+checksum.length);
                assert checksum.length == 20;
                
                //finally, reconstruct the FileInfo object from the decoded data
                rxFileInfo = new FileInfo(name, path, fileSize, revisionNumber, checksum);
            }
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
		return null;//stupid compiler
	}
    
    public static FileDataChunk bytesToFileDataChunk(byte[] b) throws IOException
    {/*2. FileInfo             | FileInfo
    3. isWholeFile          | bitfield<0>
    4. offset               | Long
    5. payload              | ByteArray*/
        /*The FileDataChunk length field has been removed,
         * so the first bit of data is the enclosed FileInfo length field*/
    	//o.println("message post length: "+b.length);
    	int ptr = 0;
        int fileInfoLength = bytesToInt(Arrays.copyOfRange(b, ptr, ptr+4));
        ptr += 4;
        //o.println("fileInfo post length: "+fileInfoLength);
        
        FileInfo fi = bytesToFileInfo(Arrays.copyOfRange(b, ptr, ptr+fileInfoLength));
        //o.println("post fileInfo:"+BinTest.bytesToHex(Arrays.copyOfRange(b, ptr, ptr+12)));
        ptr += fileInfoLength;
        boolean isWholeFile = byteToBitfield(b[ptr], 1)[0];
        
        ptr++;
        long offset = bytesToLong(Arrays.copyOfRange(b, ptr, ptr+8));
        //o.println("post offset start:"+BinTest.bytesToHex(Arrays.copyOfRange(b, ptr, ptr+8)));
        ptr += 8;
        //o.println("post payload start:"+BinTest.bytesToHex(Arrays.copyOfRange(b, ptr+4, ptr+16)));
        byte[] payload = Arrays.copyOfRange(b, ptr+4, b.length);//ignore byteArray's length field, for now
        
        //WARNING: do we even need the isWholeFile bitfield, if we're not passing it to the constructor
        
        return new FileDataChunk(fi, payload, offset);
    }
	
	/**Whether the length field needs to be left on for this method,
	 * is up for debate*/
	public static PeerInfo bytesToPeerInfo(byte[] b)
	{/*	1. UUID1               | Long
		2. UUID2               | Long
		4. isPublisherOrPeer   | bitfield<0>
		5. Addresses           | List:Address*/
		try
		{
			int ptr = 0;
			long UUID1msb = bytesToLong(Arrays.copyOfRange(b, ptr, ptr+8));
			ptr += 8;
			long UUID2lsb = bytesToLong(Arrays.copyOfRange(b, ptr, ptr+8));
			ptr += 8;
			//long globalRevNum = bytesToLong(Arrays.copyOfRange(b, 16, 24));
			boolean isPublisher = byteToBitfield(b[ptr], 1)[0];
			ptr++;//TODO: do something with the isPublisher info we've received
			
			UUID uuid = new UUID(UUID1msb, UUID2lsb);
			
			byte[] addsPre = Arrays.copyOfRange(b, ptr+4, b.length);
			//o.println("addsPre length:"+addsPre.length);
            byte[][] addsBin = bytesToList(addsPre);
            //o.println("addressesBinary length:"+addsBin.length);
            /*for(int i = 0; i < addsBin.length; i++)
            {
            	String len = (addsBin[i] == null ? "nope" : ""+addsBin[i].length);
            	o.println("addressesBinary["+i+"]:"+addsBin[i]+"\nlength:"+len);
            	
            }*/
            Address[] addresses = new Address[addsBin.length-1];
            
            assert addsBin[0][0] == Message.ADDRESS.IDByte;
            for(int i = 0; i+1 < addsBin.length; i++)
            {//why is [2] == null?
            	//o.println("addressesBinary["+(i+1)+"] length:"+addsBin[i+1].length);
                addresses[i] = bytesToAddress(addsBin[i+1]);
            }
            
			//finally, reconstruct the PeerInfo object from the decoded data
			PeerInfo rxPeerInfo = new PeerInfo(uuid, isPublisher, addresses);
			
			return rxPeerInfo;
		}
		catch(UnsupportedEncodingException uee)
		{
			//do nothing, the encoding name is static, this should never execute
		}
		catch(Exception e)//TODO proper exception handling, it's all bubbled here
		{
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	public static byte[][] bytesToList(byte[] b) throws IOException
	{/* 2. ID byte of elements  | a byte
		3. number of elements   | int
		4. &lt;elements&gt;     | ?*/
		int ptr = 0;
		Message type = Message.getMessageTypeFromID(b[0]);
		ptr++;
		
		int numElements = bytesToInt(Arrays.copyOfRange(b, ptr, ptr+4));
		
		//o.println("post numElements:"+numElements);
		//o.println("as bytes:"+BinTest.bytesToHex(Arrays.copyOfRange(b, ptr, ptr+4)));
		ptr += 4;
		
		byte[][] output = new byte[numElements+1][];//save the first byte for the IDByte
		byte[] IDByteWrapper = {b[0]};
		output[0] = IDByteWrapper;
		//System.out.println("output length: "+output.length);
		
		if (type.length >= 0)//the elements are fixed- or zero-length
        {
			int length = type.length;//get length from corresponding Message enum
			//o.println("element length: "+length);
			for(int i = 0; i+1 < numElements; i++)//split up the input byte[] into elements
			{
				//o.println("i:"+i);
				int offset = (i*length)+5;
				//o.println("offset:"+offset);
				try
				{
					output[i+1] = Arrays.copyOfRange(b, offset, offset+length);
				}
				catch(ArrayIndexOutOfBoundsException aioobe)
				{
					o.println("i:"+i);
					o.println("offset:"+offset);
					o.println("numElements:"+numElements);
					o.println("element length:"+length);
					o.println("b.length:"+b.length);
					aioobe.printStackTrace();
					System.exit(1);
				}
			}
		}
        else//the elements are variable-length or compound
		{//don't forget to remove length header for every element
			int i = ptr;
			int elementCount = 1;
			
			while(elementCount <= numElements)
			{
				//o.println("elementCount:"+elementCount);
				int currentElementLength = 
					bytesToInt(Arrays.copyOfRange(b, i, i+4));
				int elementEnd = i+currentElementLength+4;
				output[elementCount] = Arrays.copyOfRange(b, i+4, elementEnd);
				elementCount++;
				i = elementEnd;
			}
		}
        return output;
	}
	
	public static Address bytesToAddress(byte[] b)
	{/*	2. isUp                | bitfield<0>
		3. usingHostname(NotIP)| bitfield<1>
		4. Using IPv6          | bitfield<2>
		5. IP/hostname         | ByteArray.4/String
		6. listenPort          | UShort
		7. lastKnownTimeOnline | Long (ms since epoch)*/
        Address rxAddress = null;
        
        //o.println("b length:"+b.length);//if this causes a NullPointerException,
        //then that means we're being passed null...
        
		try
		{
			int ptr = 0;
			boolean[] bools = byteToBitfield(b[ptr], 3);
			boolean isOnline = bools[0];
			boolean usingHostName = bools[1];
			boolean isIPv6 = bools[2];
			String hostName = "";//initialise to empty values to prevent java complaining about the unused one
			byte[] rawIP;
			ptr++;
			int hostLength = bytesToInt(Arrays.copyOfRange(b, ptr, ptr+4));
			ptr += 4;

			//initialise both forms of address to prevent constructors complaining
			hostName = bytesToString(Arrays.copyOfRange(b, ptr, ptr+hostLength));
			rawIP = Arrays.copyOfRange(b, ptr, ptr+hostLength);
			
			ptr += hostLength;
			char port = bytesToUShort(Arrays.copyOfRange(b, ptr, ptr+2));
			ptr += 2;
			long lastSpotted = bytesToLong(Arrays.copyOfRange(b, ptr, ptr+8));
			
			if(usingHostName)
			{
				rxAddress = new Address(hostName, port, isOnline, lastSpotted);
			}
			else
			{
				rxAddress = new Address(rawIP, isIPv6, port, isOnline, lastSpotted);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
        return rxAddress;
	}
    
    public static FileInfoBunch bytesToFileInfoBunch(byte[] b) throws UnsupportedEncodingException, IOException
    {/* 2. Global RevNum    | Long
        3. Files            | List:FileInfo*/
    	int ptr = 0;
        long GRN = bytesToLong(Arrays.copyOfRange(b, ptr, ptr+8));
        ptr += 8;
        
      //int numElements = bytesToInt(Arrays.copyOfRange(b, 0, 4));
        byte[][] lb = bytesToList(Arrays.copyOfRange(b, ptr+4, b.length));
        FileInfo[] fileInfos = new FileInfo[lb.length-1];
        for(int i = 1; i < lb.length; i++)
        {
            fileInfos[i-1] = bytesToFileInfo(lb[i]);
        }
        
        FileInfoBunch ai = new FileInfoBunch(GRN, fileInfos);
        return ai;
    }
    
    /*public static FileInfo[] bytesToFileInfoList(byte[] b) throws UnsupportedEncodingException, IOException
    {
    	//int numElements = bytesToInt(Arrays.copyOfRange(b, 0, 4));
        byte[][] lb = bytesToList(Arrays.copyOfRange(b, 0, b.length));
        FileInfo[] fileInfos = new FileInfo[lb.length-1];
        for(int i = 1; i < lb.length; i++)
        {
            fileInfos[i-1] = bytesToFileInfo(lb[i]);
        }
        return fileInfos;
    }*/

    /**invokes the appropriate conversion method on a byteArray to convert any 
     * binary message into a Communicable, the interface which all Java class 
     * versions of Distribackup message objects implement.
     * Only works on Communicables with a non-zero payload.*/
    public static Communicable bytesToCommunicable(byte[] b, Message type) throws UnsupportedEncodingException, IOException
    {
        switch(type)
        {/* PeerInfo				DONE
			Archive Status			DONE
			Update Announcement		DONE
			Request For Peers		
			File Data Chunk			DONE
			File Request
			"no haz" FileReq Reply	DONE
			"haz nao" announcement	DONE
			More Peers				*/
            case PEER_INFO:
                return bytesToPeerInfo(b);
            
            case ARCHIVE_STATUS:
            case UPDATE_ANNOUNCE:
            case NO_HAZ:
            case GOT_ANNOUNCE:
            case FILE_REQUEST:
                return bytesToFileInfoBunch(b);
            
            case FILE_DATA_CHUNK:
                return bytesToFileDataChunk(b);
            
            default:
            	System.err.println("ERROR: bytesToCommunicable was given a Message Type it can't use!");
            	System.err.println("Message type:"+type);
        }
        throw new UnsupportedEncodingException("supplied bytes did not form a Message that needs converting!");
    }
		
	public static int getTotalLength(byte[]... fields)
	{
		int sum = 0;
		for(int i = 0; i < fields.length; i++)
		{
			sum += fields[i].length;
		}
		return sum;
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
