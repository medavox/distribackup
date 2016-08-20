import java.util.Scanner;
public class FileChunkSizer
{
    //constants for chunkit algo
    private static final int MIN_CHUNK_SIZE = 262144;
    private static final int MIN_CHUNK_POWER = 18;
    
    //constants for chunkit2 algo
    private static final int MIN_EXPONENT = 18;//256KB
    private static final int MAX_EXPONENT = 28;//256MB
    //private static final int MAX_EXPONENT = 30;//1GB; too large for RAM?
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
        int exponent = 18;//256KB
        
        long workingSize = size;
        long dealtWith = 0;
        boolean initial = true;
        int totalChunks = 0;
        while(true)
        {
            long chunkSize = 1L << exponent;//starts at 256KB
            long threshold = 1L << (exponent+8);//starts at 64MB
            long wholeChunks = workingSize / chunkSize;
            if(workingSize <= chunkSize) //we have a file of size <= MIN_CHUNK_SIZE
            {//aka <= 1 chunk's worth of data
                System.out.println("1 chunk of size "+prettify(workingSize));
                break;
            }
            int maxChunks = (initial ? 256 : 192);
            long actualChunksOfThisSize = Math.min(wholeChunks, maxChunks);
            if(exponent == MAX_EXPONENT)//have we reached max chunk size?
            {
                actualChunksOfThisSize = wholeChunks;
            }
            //size is > chunkSize
            System.out.println(actualChunksOfThisSize+" chunks of size "+prettify(chunkSize));
            totalChunks += actualChunksOfThisSize;
            
            if(wholeChunks == 1)
            {
                long remainder = workingSize % chunkSize;
                if(remainder != 0)
                {
                    System.out.println("1 chunk of size "+prettify(remainder));
                }
                break;
            }
            
            if(wholeChunks <= maxChunks)
            {
                if (exponent == MAX_EXPONENT)//if we've hit the max chunk size this round,
                {//print the size of the remainder from the many,many chunks
                    long sizeTotalOfTheseChunks = actualChunksOfThisSize * chunkSize;
                    workingSize -= sizeTotalOfTheseChunks;
                    System.out.println("1 chunk of size"+workingSize);
                }
                break;
            }
            
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
    
    
    public static void chunkit(long size)
    {//example: 132GB
//x is the highest even power of 2 < size -- aka our chunk size boundary

        int x = getHighestEvenPowerOf2(size);
        
        System.out.println("highest even power of 2 < input size: "+x);
        if(x < 26)//is the file size smaller than 64MB?
        {//if so, all chunks will be 256KB
            chunkUplowest64MB(size);
            
        }
        else
        {
            long largestChunkSizeBoundary = 1 << x;
            System.out.println("highestChunksize boundary: "+prettify(largestChunkSizeBoundary));
            
            //int remainingSmallerChunkSize = size - largestChunkSizeBoundary;
            //System.out.println("remaining smaller chunks : "+remainingSmallerChunkSize);
            
//-6 because that's the difference of powers between size boundary and chunk size at that boundary.
            long largestChunkSize = 1 << (x-6);
            //eg for chunks past the 2^26th byte (64MB), chunk size is 2^20 (1MB)
            
            System.out.println("largest chunk size : "+prettify(largestChunkSize));
            
            for(int i = 20; i < x; i += 2)
            {
                //System.out.println(
            }
            
            //the number of even powers of 2 between largestChunkSize and 18, aka min chunk size: 256KB
            int numberOfChunkSizes = ((x-6)/2)-8;
            System.out.println("number of chunk sizes : "+numberOfChunkSizes);
            
            long currentSize = size;
            long remainder = currentSize % largestChunkSize;
            currentSize -= remainder;
            System.out.println("last chunk's size: "+prettify(remainder, true));
            
            
            for (int workingChunkSizePower = x-6; workingChunkSizePower > 18; workingChunkSizePower -= 2)
            {
                long currentChunkSize = 1 << workingChunkSizePower;//2^workingChunkSizePower
                long chunksOfThisSize = (currentSize / currentChunkSize) - 64;
                System.out.println(chunksOfThisSize+" chunks of size "+prettify(currentChunkSize));
                currentSize -= (chunksOfThisSize * currentChunkSize);
            }
            
            chunkUplowest64MB(currentSize);
            
            //int numOfSmallestChunks = currentSize / MIN_CHUNK_SIZE;
            
            //System.out.println(numOfSmallestChunks+" chunks of size "+MIN_CHUNK_SIZE);
        }
    }
    
    /**Divide files of <=64MB into <= 256 chunks of size 256KB.*/
    public static void chunkUplowest64MB(long size)
    {
        if(size <= 64 * 1024 * 1024
        && size >= 0)
        {
            long numChunks = size / MIN_CHUNK_SIZE;
            long lastChunkSize = size % MIN_CHUNK_SIZE;
            
            System.out.println(numChunks+" chunks of size "+prettify(MIN_CHUNK_SIZE)
            +(lastChunkSize == 0 ? "" : "\nand 1 chunk of size "+prettify(lastChunkSize)));
        }
        else
        {
            throw new IllegalArgumentException("size argument must be a number between 0 and 2^26 inclusive, or 64MB");
        }
    }
    
    
    public static int getHighestEvenPowerOf2(long number)
    {
        long workingNum = number;
        int x = 0;
        while (workingNum != 1)
        {
            workingNum >>= 1;
            x++;
        }
        x /= 2;
        x *= 2;//round down to nearest even number
        return x;
    }
    
    public static String prettify(long size)
    {
        return prettify(size, true);
    }
    
    public static String prettify(long size, boolean twoUnits) {
        String units = " KMGTPEZY";
        int unitIndex = 0;
        long workingSize;
        for(workingSize = size; workingSize >= 1024; workingSize /= 1024)
        {
            unitIndex++;
        }
        
        String ret = ""+workingSize+units.charAt(unitIndex)+"B";
        
        if(twoUnits && unitIndex > 0)
        {
            long secondUnit;
            for(secondUnit = size; secondUnit >= 1024*1024; secondUnit /= 1024){}
            secondUnit -= (workingSize*1024);
            if(secondUnit > 0)
            {
                ret += ","+secondUnit+units.charAt(unitIndex-1)+"B";
            }
        }
        return ret;
    }
}
