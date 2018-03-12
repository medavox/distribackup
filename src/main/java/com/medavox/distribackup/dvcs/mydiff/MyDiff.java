package com.medavox.distribackup.dvcs.mydiff;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MyDiff {
    /**Any smaller, and it's not worth running the algorithm on*/
    static final long SMALLEST_NUMBER_OF_BYTES_WORTH_CONSIDERING = 16;

    public void topSmeg(File a, File b) {
        List<OffsetSubsequence> matchingSequences = new LinkedList<>();
        long minOffset = 0-(b.length()-SMALLEST_NUMBER_OF_BYTES_WORTH_CONSIDERING);
        long maxOffset = a.length()-SMALLEST_NUMBER_OF_BYTES_WORTH_CONSIDERING;

        //todo: prioritise offsets which are likely to produce more matches
        for(long offset = 0; offset < maxOffset; offset++) {
            long highestIndexToCheck = Math.min(a.length()-1, b.length()-1);
            //we don't need a super-precise highest-index value here;
            //we've already calculated that in indexOfNextDifferingByte()
            //however, it would look better to do that...
            Subsequence toCompare = Subsequence.fromStartAndEnd(0, highestIndexToCheck);
            try {
                long lastSimilarByteIndex = -1;
                long nextDifferingByteIndex = WorkhorseMethods.indexOfNextDifferingByte(a, b, toCompare, offset);
                if(nextDifferingByteIndex == (lastSimilarByteIndex+1)) {
                    //no bytes were similar :(
                    //so find the index of the next byte which IS the same,
                    //then advance lastSimilarByteIndex to that,
                    //and use that as a starting point to find the next DISSIMILAR byte
                }else {
                    //some bytes were similar
                    //create an OffsetSubsequence to record how many
                    OffsetSubsequence identicalBytes = new OffsetSubsequence
                            (lastSimilarByteIndex, nextDifferingByteIndex-1, offset);
                    matchingSequences.add(identicalBytes);
                    //continue to next index position; find the next similar bytes
                }

            }catch(NoOverlapForOffsetException | IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

        }
    }

}
