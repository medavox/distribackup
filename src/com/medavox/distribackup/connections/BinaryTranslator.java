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
import java.io.UnsupportedEncodingException;

import com.medavox.distribackup.peers.PeerInfo;
import com.medavox.distribackup.filesystem.FileUtils;
import com.medavox.distribackup.filesystem.FileInfo;
import com.medavox.distribackup.filesystem.FileDataChunk;
import com.medavox.distribackup.filesystem.DirectoryInfo;
import com.medavox.distribackup.filesystem.ArchiveInfo;

/**Logic in this class only pertains to conversion of primitive types to and from binary.
 * Byte arrays constructed by this class do not contain an IDByte, as not all uses
 * of every given field requires it. Instances which need one should add it manually.
 * 
 * Methods which decode bytes expect no IDByte or length field; this header is
 * only useful for parsing the Message payload from the network stream.
 * The Message length can be determined from the byte[].length anyway.
 * 
 * However, methods which encode data INTO bytes prepend the length field for convenience.
 * This means that bytes encoded with one method cannot be immediately passed
 * back to the corresponding decoding method without some processing first. 
 * 
 * If this encoding is more hassle than help, then the behaviour can be changed.*/
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
	
	FileData	-> bytes        DONE
	bytes 		-> FileData     DONE
	
	Address		-> bytes        DONE
	bytes		-> Address      DONE
	
	List		-> bytes		DONE
	bytes		-> List			DONE
    
    UpdAnnounce -> bytes        
    bytes       -> UpdAnnounce  */
	
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
		
	public static byte[] stringToBytes(String s) throws UnsupportedEncodingException
	{
		return s.getBytes("UTF-16");
	}
	
	/**This path should be relative to the repo root*/
	public static byte[] fileInfoToBytes(File f) throws IOException
	{/* 0. (ID Byte)        | a byte
		0. (Length)         | Integer
		1. Name             | String
		2. Path             | String
		3. file size        | Long
		4. revision number  | Long
		5. checksum         | SHA1*/
		
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
	}
    
    public static byte[] fileInfoToBytes(FileInfo fi) throws UnsupportedEncodingException, IOException
    {/* 1. Name             | String
        2. Path             | String
        2. isDirectory      | bitfield<0>
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
        byte[] length = intToBytes(getTotalLength(name, path, fileSize, revNum, checksum));
        return concat(length, name, path, isDirectory, fileSize, revNum, checksum);
    }
    
    public static byte[] fileDataChunkToBytes(FileDataChunk fd) throws UnsupportedEncodingException, IOException//TODO
    {/*0. (ID Byte)         | a byte
    1. (Length)             | Integer
    2. FileInfo             | FileInfo
    3. isWholeFile          | bitfield<0>
    4. offset               | Long
    5. payload              | ByteArray*/
        byte[] fileInfoBin = fileInfoToBytes(fd.getFileInfo());
        byte isWholeFileByte = bitfieldToByte(fd.isWholeFile());
        byte[] isWholeFile = {isWholeFileByte};
        byte[] offset = longToBytes(fd.getOffset());
        
        
        //payload bojecty is a Distribackup 'Byte Array', a TLV object which hasn't got conversion methods here
        
        byte[] payloadLength = intToBytes(fd.getPayload().length);
        byte[] data = fd.getPayload();
        
        byte[] payload = concat(payloadLength, data);
        
        byte[] messageLength = intToBytes(getTotalLength(fileInfoBin, isWholeFile, offset, payload));
        return concat(messageLength, fileInfoBin, isWholeFile, offset, payload);//WARNING:
        //payload length could be long enough to make enclosing FileData length overrun!
    }
	
	/**DirInfo objects are going to massive, due to their tree nature.
	 Converting a directory into byte-form will involve converting all files and
	 directories inside it, which can also contain their own files. WHOA*/
	public static byte[] directoryInfoToBytes(File f) throws IOException// TODO
	{
		//make sure File object is a directory (not a file) before starting
		if(f.isFile())
		{
			throw new IOException("ERROR: supplied File Object is not a Directory!");
		}
		return new byte[0];//TODO
	}
    
    public static byte[] directoryInfoToBytes(DirectoryInfo di)//TODO
    {/* 0. (ID Byte)        | a byte
        0. (Length)         | Integer
        2. Name             | String
        3. Path             | String
        4. Contents         | HList:FileInfo,DirectoryID*/
        return new byte[0];//TODO
    }
	
	public static byte[] peerInfoToBytes(PeerInfo p, boolean isPublisher) throws IOException, NumberFormatException
	{/* 0. (ID Byte)           | a byte
		0. (Length)            | Integer
		1. UUID1               | Long
		2. UUID2               | Long
		3. GlobalRevisionNumber| Long
		4. isPublisherOrPeer   | bitfield<0>
		5. Addresses           | List:Address*/

		byte[] UUID1 = longToBytes(p.getUUID().getMostSignificantBits());
		byte[] UUID2 = longToBytes(p.getUUID().getLeastSignificantBits());
		byte[] globalRevNum = longToBytes(p.getGRN());
		
		boolean[] bitfield = {true};
		byte[] isPubByte = {bitfieldToByte(bitfield)};
		byte[] addresses = {(byte)0x00};//TODO: implement lists and Addresses!
		
		byte[] msgLength = 
		intToBytes(getTotalLength(UUID1, UUID2, globalRevNum, isPubByte, addresses));
		
		return concat(msgLength, UUID1, UUID2, globalRevNum, isPubByte, addresses);
	}
	/**Takes a holding array of pre-converted byte[]s and turns them into a 
	distribackup:List.
	The Message byte[] elements must be pre-sorted, contain no IDByte, 
	and only their length fields if the list is made of variable-length elements.*/
	public static byte[] listToBytes(byte[][] items, byte elIDByte) throws IOException
	{/* 1. (Length)             | Long
		2. ID byte of elements  | a byte
		3. number of elements   | int
		4. &lt;elements&gt;     | ?*/
		
		byte[] elIDByteWrapper = {elIDByte};
		byte[] numElements = intToBytes(items.length);
		
		byte[] elements = concat(items);
		byte[] messageLength = intToBytes(getTotalLength(numElements, elements)+1);//+1 for elements' IDByte
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
    
    public static byte[] archiveInfoToBytes(ArchiveInfo ua) throws IOException
    {
        byte[] GRN = longToBytes(ua.getGlobalRevisionNumber());
        
        byte[] fileInfoList = fileInfoListToBytes(ua.getFiles());
        byte[] length = intToBytes(GRN.length + fileInfoList.length);
        return concat(length, GRN, fileInfoList);
    }
    
    public static byte[] fileInfoListToBytes(FileInfo[] cf) throws UnsupportedEncodingException, IOException
    {
        byte[][] fileInfos = new byte[cf.length][];
        for(int i = 0; i < cf.length; i++)
        {
            fileInfos[i] = fileInfoToBytes(cf[i]);
        }
        return listToBytes(fileInfos, Message.FILE_INFO.IDByte);
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
			int nameEndIndex = getNextMessageEndIndex(b, 0);
			String name = bytesToString(Arrays.copyOfRange(b, 4, nameEndIndex));
			int pathEndIndex = getNextMessageEndIndex(b, nameEndIndex);
			String path = bytesToString(Arrays.copyOfRange(b, nameEndIndex+4, pathEndIndex));
			
            pathEndIndex++;//move along one so as not to disrupt later addresses
            
            boolean[] isDirectory = byteToBitfield(b[pathEndIndex], 1);
            
            FileInfo rxFileInfo;
            
            if(isDirectory[0])//the file-specific fields are omitted if this is a directory
            {
                rxFileInfo = new FileInfo(name, path);
            }
            else
            {
                long fileSize = bytesToLong(Arrays.copyOfRange(b, pathEndIndex, pathEndIndex+8));
                long revisionNumber = bytesToLong(Arrays.copyOfRange(b, pathEndIndex+8, pathEndIndex+16));
                
                //there should be 20 bytes left, and they should all be the checksum
                int checksumBegin = pathEndIndex+16;
                byte[] checksum = Arrays.copyOfRange(b, checksumBegin, b.length);
                
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
		return null;
	}
    
    public static FileDataChunk bytesToFileDataChunk(byte[] b) throws IOException
    {/*2. FileInfo             | FileInfo
    3. isWholeFile          | bitfield<0>
    4. offset               | Long
    5. payload              | ByteArray*/
        /*The FileDataChunk length field has been removed,
         * so the first bit of data is the enclosed FileInfo length field*/
        int fileInfoLength = bytesToInt(Arrays.copyOfRange(b, 0, 4));
        FileInfo fi = bytesToFileInfo(Arrays.copyOfRange(b, 4, 4+fileInfoLength));
        boolean isWholeFile = byteToBitfield(b[4+fileInfoLength], 1)[0];
        long offset = bytesToLong(Arrays.copyOfRange(b, 5+fileInfoLength, 13+fileInfoLength));
        byte[] payload = Arrays.copyOfRange(b, 13+fileInfoLength, b.length);
        
        //WARNING: do we even need the isWholeFile bitfield, if we're not passing it to the constructor
        
        return new FileDataChunk(fi, payload, offset);
    }
	
	/**Whether the length field needs to be left on for this method,
	 * is up for debate*/
	public static PeerInfo bytesToPeerInfo(byte[] b)
	{/*	1. UUID1               | Long
		2. UUID2               | Long
		3. GlobalRevisionNumber| Long
		4. isPublisherOrPeer   | bitfield<0>
		5. Addresses           | List:Address*/
		try
		{
			long UUID1msb = bytesToLong(Arrays.copyOfRange(b, 0, 8));
			long UUID2lsb = bytesToLong(Arrays.copyOfRange(b, 8, 16));
			long globalRevNum = bytesToLong(Arrays.copyOfRange(b, 16, 24));
			boolean[] isPublisher = byteToBitfield(b[24], 1);
			
			UUID uuid = new UUID(UUID1msb, UUID2lsb);
			
            //get elements as byte[]s from initial byte[]
            byte[][] addressesBinary = bytesToList(Arrays.copyOfRange(b, 25, b.length));
            Address[] addresses = new Address[addressesBinary.length-1];
            for(int i = 0; i < addressesBinary.length; i++)
            {
                addresses[i] = bytesToAddress(addressesBinary[i+1]);
            }
            
            
			//TODO: do something with the isPublisher info we've received
            
			//finally, reconstruct the PeerInfo object from the decoded data
			PeerInfo rxPeerInfo = new PeerInfo(uuid, globalRevNum, addresses);
			
			return rxPeerInfo;
		}
		catch(UnsupportedEncodingException uee)
		{
			//do nothing, the encoding name is static, so this should never execute
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
		Message type = Message.getMessageTypeFromID(b[0]);
		int numElements = bytesToInt(Arrays.copyOfRange(b, 1, 5));
		byte[][] output = new byte[numElements+1][];//save the first byte for the IDByte
		byte[] IDByteWrapper = {b[0]};
		output[0] = IDByteWrapper;
		if (type.length >= 0)//the elements are fixed- or zero-length
        {
			int length = type.length;
			for(int i = 0; i < numElements; i++)//split up the input byte[] into elements
			{//I really hope Arrays.copyOfRange isn't expensive!
				output[i+1] = Arrays.copyOfRange(b, (i*length)+5, (i*length)+5+length);
			}
		}
        else//the elements are variable-length or compound
		{
			int lengthLength;
			if(type == Message.LIST)
			{//the length field of lists is a long: 8, not 4, bytes wide
				lengthLength = 8;
			}
			else
			{
				lengthLength = 4;
			}
			
			int i = 5;
			int elementCount = 1;
			while(i < numElements)
			{
				int currentElementLength = 
					bytesToInt(Arrays.copyOfRange(b, i, i+lengthLength));
				int elementEnd = i+currentElementLength+lengthLength;
				output[elementCount] = Arrays.copyOfRange(b, i+lengthLength, elementEnd);
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
		try
		{
			boolean[] bools = byteToBitfield(b[0], 3);
			boolean isOnline = bools[0];
			boolean usingHostName = bools[1];
			boolean isIPv6 = bools[2];
			String hostName = "";//initialise to empty values to prevent java complaining about the unused one
			byte[] rawIP;
			int addressEnd;
			if(usingHostName)
			{
				addressEnd = getNextMessageEndIndex(b, 1);
				hostName = bytesToString(Arrays.copyOfRange(b, 1, addressEnd));
				
			}
			else if(isIPv6)
			{
				addressEnd = 17;
			}
			else
			{//plain old IPv4
				addressEnd = 5;
			}
			rawIP = Arrays.copyOfRange(b, 1, addressEnd);
			
			char port = bytesToUShort(Arrays.copyOfRange(b, addressEnd, addressEnd+2));
			
			long lastSpotted = bytesToLong(Arrays.copyOfRange(b, addressEnd+2, addressEnd+10));
			
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
    
    public static ArchiveInfo bytesToArchiveInfo(byte[] b) throws UnsupportedEncodingException, IOException
    {/* 2. Global RevNum    | Long
        3. Files            | List:FileInfo*/
        long GRN = bytesToLong(Arrays.copyOfRange(b, 0, 8));
        FileInfo[] fileInfos = bytesToFileInfoList(Arrays.copyOfRange(b, 8, b.length));
        ArchiveInfo ai = new ArchiveInfo(GRN, fileInfos);
        return ai;
    }
    
    public static FileInfo[] bytesToFileInfoList(byte[] b) throws UnsupportedEncodingException, IOException
    {
        byte[][] lb = bytesToList(Arrays.copyOfRange(b, 0, b.length));
        FileInfo[] fileInfos = new FileInfo[lb.length];
        for(int i = 0; i < lb.length; i++)
        {
            fileInfos[i] = bytesToFileInfo(lb[i]);
        }
        return fileInfos;
    }

    /**invokes the appropriate conversion method on a byteArray to convert any 
     * binary message into a Communicable, the interface which all Java class 
     * versions of Distribackup message objects implement.*/
    public static Communicable bytesToCommunicable(byte[] b, Message type) throws UnsupportedEncodingException, IOException//TODO
    {/* PeerInfo
        Archive Status
        File Data Chunk
        File Request
        Update Announcement
        "no haz" FileReq Reply
        "haz nao" announcement*/
        Communicable vagueReturnType;

        switch(type)
        {
            case PEER_INFO:
                return bytesToPeerInfo(b);
            
            case ARCHIVE_STATUS:
            case UPDATE_ANNOUNCE:
                return bytesToArchiveInfo(b);
            
            case FILE_DATA_CHUNK:
                return bytesToFileDataChunk(b);
            
            case NO_HAZ:
            case HAZ_NAO:
            //there is no encapsulating class, java or distribackup, for these
            //just send the FileInfo[]?
                byte[] wneud = Arrays.copyOfRange(b, 8, b.length);
                FileInfo[] fi = bytesToFileInfoList(wneud);
                return new FileInfoListWrapper(fi);
        }
        throw new UnsupportedEncodingException("supplied bytes did not form a Message that needs converting!");
    }
	/**Returns the index of the first byte after the end of the first Message.
	 * So for a message that spans from 0 to 35, is 32 bytes long (plus length bytes),
	 * this method will return 36*/
	public static int getNextMessageEndIndex(byte[] b, int offset) throws IOException
	{
		int len = bytesToInt(Arrays.copyOfRange(b, offset, offset+4));
		return len;
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
