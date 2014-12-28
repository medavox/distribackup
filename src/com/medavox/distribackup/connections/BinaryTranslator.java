package com.medavox.distribackup.connections;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Path;
import java.io.DataOutputStream;
import java.io.DataInputStream;

import com.medavox.distribackup.peers.PeerInfo;

/**Try to keep logic in this class just pertaining to conversion to and from binary*/
public class BinaryTranslator
{
    DataOutputStream dos;
    DataInputStream dis;
    ByteArrayOutputStream ba = new ByteArrayOutputStream();
	public BinaryTranslator()
	{
		dos = new DataOutputStream(ba);
	}
	

    public static byte[] shortToBytes(short l)
    {
        ba.reset();//reset byte output buffer
        dos.writeShort(l);
        return ba.toByteArray();
    }
    
    public static byte[] intToBytes(int l)
    {
        ba.reset();//reset byte output buffer
        dos.writeInt(l);
        return ba.toByteArray();
    }
    
    public static byte[] longToBytes(long l)
    {
        ba.reset();//reset byte output buffer
        dos.writeLong(l);
        return ba.toByteArray();
    }
    
    public static long bytesToLong(byte[] b) throws IOException, EOFException
    {
        
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
