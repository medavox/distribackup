import java.util.Scanner;
public class FileChunkSizer
{
    //constants for chunkit algo
    private static final int MIN_CHUNK_SIZE = 262144;
    private static final int MIN_CHUNK_POWER = 18;
    
    //constants for chunkit2 algo
    private static final int MIN_EXPONENT = 18;//256KB
    //private static final int MAX_EXPONENT = 28;//256MB
    private static final int MAX_EXPONENT = 30;//1GB; too large for RAM?
    //private static final int MAX_EXPONENT = 62;//max LONG's highest even power of 2
    
    public static void main(String[] args)
    {
        if(args.length != 1)
        {
            System.out.println("please supply one argument: a file size");
        }
        else
        {
            //todo:input sanitation & handling K,M,G,T
            long input = Long.parseLong(args[0]);
            System.out.println("input size given: "+prettify(input, true));
            chunkit2(input);
            
        }
    }
    
    public static void chunkit2(long size)
    {
        int exponent = MIN_EXPONENT;//256KB
        
        long workingSize = size;
        long dealtWith = 0;
        boolean initial = true;
        int totalChunks = 0;
        while(true)
        {
            long chunkSize = 1L << exponent;//starts at 256KB
            long threshold = 1L << (exponent+8);//starts at 64MB
            long wholeChunks = workingSize / chunkSize;//number of wholeChunks, unbounded
            if(workingSize <= chunkSize) //we have < a whole chunk's worth of file left
            {
                totalChunks++;
                System.out.println("1 chunk of size "+prettify(workingSize));
                break;
            }
            int maxChunks = (initial ? 256 : 192);
            //long actualChunksOfThisSize = Math.min(wholeChunks, maxChunks);
            long actualChunksOfThisSize = (exponent == MAX_EXPONENT ? wholeChunks : Math.min(wholeChunks, maxChunks));
            System.out.println(actualChunksOfThisSize+" chunks of size "+prettify(chunkSize));
            totalChunks += actualChunksOfThisSize;
            
            long sizeTotalOfTheseChunks = actualChunksOfThisSize * chunkSize;
            workingSize -= sizeTotalOfTheseChunks;
            dealtWith += sizeTotalOfTheseChunks;
            //System.out.println("remaining file size left to deal with: "+prettify(workingSize));
            //System.out.println("dealt with: "+prettify(dealtWith));
            exponent += 2;
            initial = false;
            //System.out.println("going again...");
        }
        System.out.println("total chunks: "+totalChunks);
    }
    
    public static int getNumberOfChunks(long size)
    {
        boolean initial = true;
        int chunkBoundary = 26;//64MB
        int chunks = 0;
        while(true)
        {
            if(size > (1L << chunkBoundary))//file is larger than this many chunks
            {
                chunks += (initial ? 256 : 192);
                chunkBoundary <<= 2;
                continue;
            }
            else//file contains no larger chunks than this size
            {
                long amountInThisSize = size - (chunkBoundary >> 2);
                long wholeChunks = 
            }
        }
        
    }
    
    public static String prettify(long size)
    {
        return prettify(size, true);
    }
    
    public static String prettify(long size, boolean twoUnits) {
        final String units = " KMGTPEZY";
        int unitIndex = 0;
        long workingSize;
        for(workingSize = size; workingSize >= 1024*1024; workingSize /= 1024)
        {
            unitIndex++;
        }
        long secondUnit = workingSize;
        workingSize /= 1024;
        unitIndex++;
        String ret = ""+workingSize+units.charAt(unitIndex)+"B";
        if(twoUnits && unitIndex > 0)
        {
            secondUnit -= (workingSize*1024);
            if(secondUnit > 0)
            {
                ret += ","+secondUnit+units.charAt(unitIndex-1)+"B";
            }
        }
        
        return ret;
    }
}
