package testing;

import com.medavox.distribackup.connections.*;
import com.medavox.distribackup.filesystem.FileDataChunk;
import com.medavox.distribackup.filesystem.FileInfo;
import com.medavox.distribackup.filesystem.FileUtils;
import com.medavox.distribackup.filesystem.FileInfoBunch;
import com.medavox.distribackup.peers.Peer;
import com.medavox.distribackup.peers.PeerInfo;

import java.io.*;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
/**Performs tests for conversions between binary and Distribackup Message types,
 to make sure they are encoded and decoded correctly*/
public class BinTest
{
	static PrintStream o = System.out;
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	//public static String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890 !\"$%^&*()_+-=\'@#~;:[{}]/\\|?<>,.";
	public static String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890 !,.";
/*  Tests:
	byte		N/A
	ubyte		PASSED
	short		PASSED
	ushort		PASSED
	int			PASSED
	uint		notimpl
	long		PASSED
	ulong		notimpl
	bitfield	PASSED
	String		PASSED
	PeerInfo	
	FileInfo	
	FileDataCh	
	Address		
	List		
	UpdAnnounce	*/
	
	private static Random r = new Random();
	
	private static Address randomAddress()
	{
		String[] urls = FileUtils.fileToString(new File("/home/scc/distribackup/res/popurls.txt")).split("\n");
		
		String hostName = urls[r.nextInt(urls.length)];
		boolean isOnline = r.nextBoolean();
		char port = (char)((r.nextInt() % 64512) +1024);
		long timeSinceOnline = r.nextLong();
		
		try
		{
			return new Address(hostName, port, isOnline, timeSinceOnline);
		}
		catch(UnknownHostException uhe)
		{
			uhe.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	private static FileInfo randomFileInfo()
	{
		/*String name, String path, long fileSize, long revisionNumber, byte[] checksum*/
		String name = genGib(r.nextInt(70));
		//o.println("name length:"+name.length());
		String path = genGib(r.nextInt(200));
		//o.println("path length:"+path.length());
		long fileSize = Math.abs(r.nextLong()) % (1048576l * 1048576l);//1TB
		long revNum = r.nextLong();
		byte[] bytes = new byte[20];
		r.nextBytes(bytes);
		return new FileInfo(name, path, fileSize, revNum, bytes);
	}
	
	private static PeerInfo randomPeerInfo()
	{
		/*UUID uuid, Address[] startingAddresses*/
		long topLong = r.nextLong();
		long botLong = r.nextLong();
		UUID uuid = new UUID(topLong, botLong);
		
		Address[] a = randomAddressList();
		
		return new PeerInfo(uuid, false, a);
	}
	
	private static Address[] randomAddressList()
	{
		int number = r.nextInt(50);
		Address[] a = new Address[number];
		for(int i = 0; i < number; i++)
		{
			a[i] = randomAddress();
		}
		return a;
	}
	
	private static FileInfo[] randomFileInfoList()
	{
		int number = r.nextInt(80);
		FileInfo[] f = new FileInfo[number];
		for(int i = 0; i < number; i++)
		{
			f[i] = randomFileInfo();
		}
		return f;
	}
	
	private static FileDataChunk randomFileDataChunk()
	{
		FileInfo fi = randomFileInfo();
		int chunkSize = r.nextInt(Peer.MAX_CHUNK_SIZE);
		byte[] garbage = new byte[chunkSize]; 
		r.nextBytes(garbage);
		long offset = Math.abs(r.nextLong()) % fi.getFileSize();//make the offset < fileSize
		//o.println("pre-encode offset:"+offset);
		return new FileDataChunk(fi, garbage, offset);
	}
	
	private static FileInfoBunch randomUpdateAnnouncement()
	{
		long grn = r.nextLong();
		FileInfo[] files = randomFileInfoList();
		return new FileInfoBunch(grn, files);
	}
	
	public static void main(String[] args)
	{
		//o.println((int)char);//cast chars to int to see them as a number
		//o.println(-53873 % 50 );//modulo'd minus numbers remain negative
		byte b2 = (byte)0xffff;//Result: 0xff aka -1
		/*for(int i = 0; i < 20; i++)
		{o.println(r.nextInt());}*/
		try
		{
			/*TODO:
			implement random-value testing for uint
			* test the following edge-cases for each numerical type:
			*  0
			* -1
			* MAX_VAL
			* MIN_VAL
			* 
			Tests for compound types as they are implemented*/
			
			int m = Integer.MAX_VALUE;
			
			//create random-value variables for testing
			
			//byte b = (byte)r.next(8);							//byte
			short ub = (short)(r.nextInt(256));					//ubyte
			short s  = (short)(r.nextInt(65536) - 32767);		//short
			char us  = (char)(r.nextInt(65536));				//ushort
			int	it	= r.nextInt();								//int
			//2^32 = 4,294,967,296
			//long uit = (long)(r.nextInt() - Integer.MIN_VALUE);	//uint
			//long uit = 0;
			//long uit = ((long)Integer.MAX_VALUE * 2);
			long uit = 4294901760L;	//0xFFFF0000
			long l = r.nextLong();								//long
			//me.love(ulong time);								//ulong (nope)
			boolean[] bf = new boolean[8];						//bitfield
			for(int i = 0; i < 8; i++)
			{
				bf[i] = r.nextBoolean();
			}
			String testString = genGib(r.nextInt(300));			//String
			
			//BEGIN THE TESTS!
			
			//unsigned byte test
			String ubytePre = new Short(ub).toString();
			byte ubyteConv = BinaryTranslator.ubyteToByte(ub);
			String ubytePost = new Short(BinaryTranslator.byteToUByte(ubyteConv)).toString();
			test(ubytePre, ubytePost, "ubyte");
			
			//signed short test
			String shortPre = new Short(s).toString();
			byte[] sconv = BinaryTranslator.shortToBytes(s);
			String shortPost = new Short(BinaryTranslator.bytesToShort(sconv)).toString();
			test(shortPre, shortPost, "short");
			
			//UNsigned short test
			String ushortPre = new Integer((int)s).toString();//hmmmm
			byte[] usconv = BinaryTranslator.shortToBytes(s);
			String ushortPost = 
			new Integer((int)BinaryTranslator.bytesToShort(usconv)).toString();
			test(ushortPre, ushortPost, "ushort");
			
			//integer test
			String intPre = new Integer(it).toString();
			byte[] iconv = BinaryTranslator.intToBytes(it);
			String intPost = new Integer(BinaryTranslator.bytesToInt(iconv)).toString();
			test(intPre, intPost, "int");
			
			//unsigned integer test
			String uintPre = new Long(uit).toString();
			byte[] uiconv = BinaryTranslator.uintToBytes(uit);
			String uintPost = new Long(BinaryTranslator.bytesToUInt(uiconv)).toString();
			test(uintPre, uintPost, "uint");
			
			//long integer test
			String lPre = new Long(l).toString();
			byte[] lconv = BinaryTranslator.longToBytes(l);
			String lPost = new Long(BinaryTranslator.bytesToLong(lconv)).toString();
			test(lPre, lPost, "long");
			
			//bitfield Test
			String bfPre = "";
			for(int i = 0; i < bf.length; i++)
			{
				bfPre += new Boolean(bf[i]).toString()+"\t";
			}
			byte bfconv = BinaryTranslator.bitfieldToByte(bf);
			boolean[] bfBack = BinaryTranslator.byteToBitfield(bfconv, 8);
			String bfPost = "";
			for(int i = 0; i < bfBack.length; i++)
			{
				bfPost += new Boolean(bfBack[i]).toString()+"\t";
			}
			test(bfPre, bfPost, "bitfield");
			
			//String Test
			String strPre = testString;
			byte[] strconv = BinaryTranslator.stringToBytes(testString);
			String strPost = BinaryTranslator.bytesToString(Arrays.copyOfRange(strconv, 4, strconv.length));
			test(strPre, strPost, "String");
			//o.println((int)byteToUShort(cb));
			
			//FileInfo Test
			FileInfo FI = randomFileInfo();
			String FIpre = FI.toString();
			byte[] FIconv = BinaryTranslator.fileInfoToBytes(FI);
			String FIpost = BinaryTranslator.bytesToFileInfo(Arrays.copyOfRange(FIconv, 4, FIconv.length)).toString();
			test(FIpre, FIpost, "FileInfo");
			byte[] failFiPre =  BinaryTranslator.stringToBytes(FIpre.toString());
			byte[] failFiPos =  BinaryTranslator.stringToBytes(FIpost.toString());
			
			//System.out.println("pre :"+space(bytesToHex(failFiPre)));
			//System.out.println("post:"+space(bytesToHex(failFiPos)));
			
			//PeerInfo Test
			PeerInfo PI = randomPeerInfo();
			String PIpre = PI.toString();
			byte[] PIconv = BinaryTranslator.peerInfoToBytes(PI, false);
			String PIpost = BinaryTranslator.bytesToPeerInfo(Arrays.copyOfRange(PIconv, 4, PIconv.length)).toString();
			test(PIpre, PIpost, "PeerInfo");
			
			//FileDataChunk Test
			FileDataChunk fdc = randomFileDataChunk();
			String fdcPre = fdc.toString();
			byte[] fdcConv = BinaryTranslator.fileDataChunkToBytes(fdc);
			String fdcPost = BinaryTranslator.bytesToFileDataChunk(Arrays.copyOfRange(fdcConv, 4, fdcConv.length)).toString();
			test(fdcPre, fdcPost, "FileDataChunk");
			
			//UpdateAnnouncement Test
			FileInfoBunch ua = randomUpdateAnnouncement();
			String uaPre = ua.toString();
			byte[] uaConv = BinaryTranslator.fileInfoBunchToBytes(ua);
			String uaPost = BinaryTranslator.bytesToFileInfoBunch(Arrays.copyOfRange(uaConv, 4, uaConv.length)).toString();
			test(uaPre, uaPost, "UpdateAnnouncement");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void test(String pre, String post, String name)
	{
		if(pre.equals(post))
		{
			o.println(name+" PASSED!");
			/*if(name.equals("FileInfo"))
			{
				o.println("pre  value: "+pre+"\npost value: "+post);
			}*/
		}
		else
		{
			o.println(name+" FAILED!\npre  value: "+
			pre+"\npost value: "+post);
			
			//o.println("pre  bytes: "+)
		}
	}

	public static String space(String pre)
	{
		String s = "";
		int i = 0;
		while(i < pre.length())
		{
			s += pre.substring(i, i+2)+" ";
			i+=2;
		}
		return s;
	}
	
	public static String bytesToHex(byte[] bytes)
	{
		char[] hexChars = new char[bytes.length * 2];
		for(int j = 0; j < bytes.length; j++)
		{
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	
	public static String genGib(int length)
    {
        String gib = "";
        Random rand = new Random();
        for(int i = 0; i < length; i++)
        {
            gib += chars.charAt(rand.nextInt(chars.length()));
        }
        return gib;
    }

}
