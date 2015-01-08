import com.medavox.distribackup.connections.*;
import java.io.*;
import java.util.Random;
/**Performs tests for the type conversions to binary,
 to make sure they are encoded and decoded correctly*/
public class BinTest
{
	static PrintStream o = System.out;
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890 !\"$%^&*()_+-=\'@#~;:[{}]/\\|?<>,.";
/*  Tests:
	byte		N/A
	ubyte		PASSED
	short		PASSED
	ushort		PASSED
	int			PASSED
	uint		notimpl
	long		PASSED
	ulong		notimpl
	bitfield	FAILED
	String*/
	public static void main(String[] args)
	{
		//o.println((int)char);//cast chars to int to see them as a number
		//o.println(-53873 % 50 );//modulo'd minus numbers remain negative
		byte b2 = (byte)0xffff;//Result: 0xff aka -1
		Random r = new Random();
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
			
			//bitfield test
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
				bfPost += new Boolean(bfBack[i]).toString()+"\t"		;
			}
			test(bfPre, bfPost, "bitfield");
			
			//String test
			String strPre = testString;
			byte[] strconv = BinaryTranslator.stringToBytes(testString);
			String strPost = BinaryTranslator.bytesToString(strconv);
			test(strPre, strPost, "String");
			//o.println((int)byteToUShort(cb));
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
			if(name.equals("uint"))
			{
				o.println("pre  value: "+pre+"\npost value: "+post);
			}
		}
		else
		{
			o.println(name+" FAILED!\npre  value: "+
			pre+"\npost value: "+post);
		}
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
