package com.medavox.distribackup.dvcs.mydiff;

import com.medavox.distribackup.dvcs.mydiff.matching_sequence_trackers.MatchingSequenceTracker;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

public class MyDiff {
    /**Any smaller, and it's not worth running the algorithm on*/
    static final long SMALLEST_NUMBER_OF_BYTES_WORTH_CONSIDERING = 16;

    /*Prioritised offsets we should check first:
    * 0 offset (file starts are aligned)
    * offset where file ends are aligned
    * ?possibly? all the offsets between these two*/
    public void calcDiffGivenOffset(File a, File b, long offset) {
        long minOffset = 0-b.length()-SMALLEST_NUMBER_OF_BYTES_WORTH_CONSIDERING;
        long maxOffset = a.length()-SMALLEST_NUMBER_OF_BYTES_WORTH_CONSIDERING;
        List<OffsetSubsequence> matchingSequences = new LinkedList<>();
        MatchingSequenceTracker matchedSequencesInB = MatchingSequenceTracker.getNew(b.length());

        //todo: prioritise offsets which are likely to produce more matches
        try {
            RandomAccessFile randomA = new RandomAccessFile(a, "r");
            RandomAccessFile randomB = new RandomAccessFile(b, "r");

            long highestIndexToCheck = WorkhorseMethods
                    .getHighestIndexToCheck(a.length(),b.length(), offset);
            for(long index = 0; index < highestIndexToCheck;) {
                randomA.seek(index);
                randomB.seek(index+offset);
                boolean currentIndexIsIdentical =
                        (randomA.readUnsignedByte() == randomB.readUnsignedByte());
                long lastIndex = index;
                if(currentIndexIsIdentical) {
                    index = WorkhorseMethods
                            .indexOfNextDifferingByte(randomA, randomB, index, offset);
                    matchingSequences.add(new OffsetSubsequence(lastIndex, index, offset));
                    matchedSequencesInB.setSequenceAsAccountedFor(lastIndex+offset, index+offset);

                }else {
                    index = WorkhorseMethods
                            .indexOfNextIdenticalByte(randomA, randomB, index, offset);
                    //todo: maybe record this dissimilar sequence somehow?
                }

            }
            /*long lastSimilarByteIndex = -1;
            long nextDifferingByteIndex = WorkhorseMethods.indexOfNextDifferingByte(a, b, 0, offset);
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
            }*/

        }catch(NoOverlapForOffsetException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
