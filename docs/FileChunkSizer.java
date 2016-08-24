import java.util.Scanner;
public class FileChunkSizer
{
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
    
    //todo:finish this
    public static long getNumberOfChunks(long size)
    {
        boolean initial = true;
        int chunkBoundary = MIN_EXPONENT+8;//64MB
        long chunks = 0;
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
                long amountInThisSize = (initial ? size : size - (1L << (chunkBoundary - 2)));
                int chunkSize = 1 << (chunkBoundary - 8);
                long wholeChunks = amountInThisSize / chunkSize;
                
                if (amountInThisSize % chunkSize != 0)
                {//add remainder as last chunk, if it's longer than 0
                    chunks++;
                }
                
                //quick sanity check on calculated result
                int chunkLimit = (initial ? 256 : 192);
                if (wholeChunks > chunkLimit)
                {
                    throw new IllegalStateException("file should not produce > "+chunkLimit
                    +" chunks of size "+chunkSize+"!");
                }
                chunks += wholeChunks;
            }
            initial = false;
            chunkBoundary += 2;
        }
        return chunks;
    }
    
    /**This doesn't need an actual file argument,
     * because the address for every chunk is the same for every files.
     *It's up to the caller to check if the returned address falls within the file's length*/
    public long getAddressOfChunk(int chunkNum)
    {
        long address;
        if(chunkNum <= 255)
        {
            return chunkNum * (1 << MIN_EXPONENT);
        }
        else
        {
            int chunkNumAfterInitial = chunkNum - 255;//number of chunks in first size
            int completeChunkSizes = chunkNumAfterInitial / 192;//number of chunks in every size after
            int chunksInLastSize = chunkNumAfterInitial % 192;
            
            address = 256 * (1 << MIN_EXPONENT);
            for(int i = 1; i <= completeChunkSizes; i++)
            {
                int exponent = MIN_EXPONENT + (i * 2);
                address += (1L << exponent);
            }
            
            //add length of chunks in this last, incomplete chunk size set and return
            return address + (chunksInLastSize * (1L << (MIN_EXPONENT+1+completeChunkSizes)));
            
        }
        /*if(chunkNum > lastChunk)
        {
            throw new IndexOutOfBoundsException("File of length "+fileSize
                +" does not have a chunk number "+chunkNum);
        }
        else
        {*/
            return address;
        //}
    }
    
    public int getLengthOfChunk(int chunkNum)
    {
        if(chunkNum < 256)
        {
            return 1 << MIN_EXPONENT;
        }
        else
        {
            int chunkNumLessInitial = chunkNum - 255;
            int exponentAdditions = chunkNumLessInitial / 192;
            
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
