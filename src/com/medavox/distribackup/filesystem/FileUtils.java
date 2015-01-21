package com.medavox.distribackup.filesystem;

import java.security.*;
import java.io.*;
import java.io.IOException;
import java.nio.file.Path;

public abstract class FileUtils
{
	public static byte[] checksum(File f) throws FileNotFoundException, IOException
	{
		int bufferSize = 16*1024*1024;//read 16MB of the file into RAM at a time
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			FileInputStream fis = new FileInputStream(f);
		
			byte[] chunk = new byte[bufferSize];
			int bytesRead = 0;
			while ((bytesRead = fis.read(chunk)) != -1)
			{
				md.update(chunk, 0, bytesRead);
			}
			
			return md.digest();
		}
		catch(NoSuchAlgorithmException nsae)
		{
			//do nothing;
			//the String which could cause this exception is invariate
		}
		return new byte[0];
	}

	public static String fileToString(File f)
	{
		try
		{
			FileReader fr = new FileReader(f);
			char[] tmp = new char[(int)f.length()];
			char c;
			int j = 0;
			for(int i = fr.read(); i != -1; i = fr.read())
			{
				c = (char)i;
				tmp[j] = c;
				j++;
			}
			fr.close();
			String ret = new String(tmp);
			return ret;
		}
		catch(Exception e)
		{
			System.err.println("failed to read file: \""+f.getName()+"\"!");
			return "";
		}
	}
}