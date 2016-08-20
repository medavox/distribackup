import java.util.Scanner;
public class FileChunkSizer
{
    private static final int MIN_CHUNK_SIZE = 262144;
    private static final int MIN_CHUNK_POWER = 18;
    public static void main(String[] args)
    {
        if(args.length != 1)
        {
            System.out.println("please supply one argument: a file size");
        }
        else
        {
            //todo:input sanitation
            int input = Integer.parseInt(args[0]);
            chunkit(input);
            
        }
    }
    
    public static void chunkit(int size)
    {//example: 132GB
        int workingNum = size;
        int x = 0;
        
        while (workingNum != 1)
        {
            workingNum >>= 1;
            x++;
        }
        x /= 2;
        x *= 2;//round down to nearest even number
        
        System.out.println("x is: "+x);
        if(x < 26)//is the file size smaller than 64MB?
        {
            int numChunks = size / MIN_CHUNK_SIZE;
            int lastChunkSize = size % MIN_CHUNK_SIZE;
            
            System.out.println(numChunks+" chunks of size "+MIN_CHUNK_SIZE);
            System.out.println("and 1 chunk of size "+lastChunkSize);
        }
        else
        {
            
            int largestChunkSizeBoundary = 1 << x;
            System.out.println("highestChunksize boundary: "+largestChunkSizeBoundary);
            //x is now the highest even power of 2 in our size -- aka our chunk size boundary
            //int remainingSmallerChunkSize = size - largestChunkSizeBoundary;
            //System.out.println("remaining smaller chunks : "+remainingSmallerChunkSize);
            
            int largestChunkSize = 1 << (x-6);//-6 because that's the difference of powers between size boundary and chunk size at that boundary.
            //eg for chunks past the 2^26th byte (64MB), chunk size is 2^20 (1MB)
            System.out.println("largest chunk size : "+largestChunkSize);
            
            for(int i = 20; i < x-; i += 2)
            {
                System.out.println(
            }
            
            //the number of even powers of 2 between largestChunkSize and 18, aka min chunk size: 256KB
            int numberOfChunkSizes = ((x-6)/2)-8;
            System.out.println("number of chunk sizes : "+numberOfChunkSizes);
            
            int workingChunkSizePower = x-6;//eg 20
            System.out.println("workig chunk size power:"+workingChunkSizePower);
            int currentSize = size;
            int remainder = currentSize % (1 << largestChunkSize);
            System.out.println("last chunk's size: "+remainder);
            
            
            while (workingChunkSizePower != 18)
            {
                int currentChunkSize = 1 << workingChunkSizePower;
                int chunksOfThisSize = (currentSize / currentChunkSize) - 64;
                System.out.println(chunksOfThisSize+" chunks of size "+currentChunkSize);
                workingChunkSizePower -= 2;
            }
            
            int numOfSmallestChunks = currentSize / MIN_CHUNK_SIZE;
            
            System.out.println(numOfSmallestChunks+" chunks of size "+MIN_CHUNK_SIZE);
        }
    }
}
