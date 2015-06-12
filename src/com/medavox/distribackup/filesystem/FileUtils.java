package com.medavox.distribackup.filesystem;

import java.security.*;
import java.util.Random;
import java.util.UUID;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.DirectoryStream;
/**Contains various helper methods to perform file-related functions.*/
public abstract class FileUtils
{
	/**Produces an SHA-1 checksum of a given file.
	 * Reads the file into memory in 16MB chunks.*/
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
			//the String which could cause this exception is invariant
		}
		return new byte[0];
	}
	/**Hands out randomly chosen names*/
	public static String getRandomName()
	{
		String[] boyNames = fileToString(new File("/home/scc/distribackup/res/boysnames.txt")).split("\n");
		String[] girlNames = fileToString(new File("/home/scc/distribackup/res/girlsnames.txt")).split("\n");
		String[] allNames = new String[boyNames.length + girlNames.length];
		for(int i = 0; i < boyNames.length; i++)
		{
			allNames[i] = boyNames[i];
		}
		for(int i = boyNames.length; i < boyNames.length+girlNames.length; i++)
		{
			allNames[i] = girlNames[i-boyNames.length];
		}
		
		Random r = new Random();
		int randomIndex = r.nextInt(allNames.length);
		return allNames[randomIndex];
	}
	/**Chooses a code name based on the supplied UUID. idempotent;
	 * gives the same name for the same uuid every time.*/
	public static String getCodeName(UUID uuid)
	{
		String[] boyNames = fileToString(new File("/home/scc/distribackup/res/boysnames.txt")).split("\n");
		String[] girlNames = fileToString(new File("/home/scc/distribackup/res/girlsnames.txt")).split("\n");
		String[] allNames = new String[boyNames.length + girlNames.length];
		
		for(int i = 0; i < boyNames.length; i++)
		{
			allNames[i] = boyNames[i];
		}
		for(int i = boyNames.length; i < boyNames.length+girlNames.length; i++)
		{
			allNames[i] = girlNames[i-boyNames.length];
		}
		
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		
		long xored = msb ^ lsb;
		int index = (int)(Math.abs(xored) % allNames.length);
		//System.out.println("index chosen:"+index);
		
		return allNames[index];
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
	
	public static void recursiveFileListing(Path p, ArchiveInfo ai)
	{
		if(Files.isDirectory(p))
		{//list dir contents
			try
			{
				DirectoryStream<Path> contents = Files.newDirectoryStream(p);
				for(Path child : contents)
				{
					recursiveFileListing(child, ai);
				}
			}
			catch(IOException ioe)//TODO: make error catching blocks better
			{
				ioe.printStackTrace();
			}
		}
		//whether p is a file or the directory, add it to the archiveInfo object
		ai.update(p);
	}
}
