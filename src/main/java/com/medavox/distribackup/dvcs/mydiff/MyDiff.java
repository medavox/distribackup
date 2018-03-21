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

    /*REMEMBER: we're picking each offset that matches the most first,
    * but we ARE going to use multiple offsets.
    * We're not expecting any one offset to fully match.
    * Identical files should have been filtered out before we invoke this algorithm*/

    //todo: prioritise offsets which are likely to produce more matches
    /*Prioritised offsets we should check first:
    * 0 offset (file starts are aligned)
    * offset where file ends are aligned
    * ?possibly? all the offsets between these two*/
    public MatchingSequenceTracker findIdenticalSubsequencesGivenOffset(File a, File b, long offset) {
        long minOffset = 0-b.length()-SMALLEST_NUMBER_OF_BYTES_WORTH_CONSIDERING;
        long maxOffset = a.length()-SMALLEST_NUMBER_OF_BYTES_WORTH_CONSIDERING;
        //List<OffsetSubsequence> matchingSequences = new LinkedList<>();
        MatchingSequenceTracker matchedSequencesInB = MatchingSequenceTracker.getNew(b.length());

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
                    //find how many bytes after this one are identical,
                    //advance our index to that position,
                    index = WorkhorseMethods
                            .indexOfNextDifferingByte(randomA, randomB, index, offset);
                    //then record that as a new matching subsequence
                    //fixme: only one structure is necessary for tracking matched sequences
                    //matchingSequences.add(new OffsetSubsequence(lastIndex, index, offset));
                    matchedSequencesInB.addSequence(lastIndex+offset, index+offset);

                }else {
                    index = WorkhorseMethods
                            .indexOfNextIdenticalByte(randomA, randomB, index, offset);
                    //todo: maybe record this dissimilar sequence somehow?
                }
            }
            return matchedSequencesInB;
        }catch(NoOverlapForOffsetException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
    /*First, for every offset, we have to create a list of the parts of A and B that match.
    Then, we select ranges from those lists with the best* matches,
    then stitch those selections together into a list of ranges-with-offsets which describe
    identical and transposed parts of A into B; and then append the new-in-B parts.
    (sections of B which remain unfound in A)

    That is how to make the edit-script.
    edit-script entries are sorted by destination-in-B start.

    *best in this case is not completely pinned down yet, but it will probably be something like:
      the highest number of bytes matched in the fewest continuous sequences.

    end-conditions for the search:
    we've searched exhaustively when A has been compared against B for every valid offset.

    */

    /*Big-O of algorithm:
    ---------------------
    For a minimum overlap z, how many offsets are there for 2 files of lengths m and n?
    (offsets can be negative)

    answer:
    (m - (z-1) ) + ( n - z )

    or, simplified by aimee:
    m + n - 2z+1

    So this means our Order is roughly O(m+n), or linear. That's good!
    */

    /*Using the matching subsequences-with-offsets to make an edit script:
    ---------------------------------------------------------------------
    starting at the start of the file,
    find all offsets where this byte is identical.
    if there are none, then this is the beginning of a >=1 byte-long original sequence in B,
    so record it verbatim from B into the edit-script

    for all the offsets where this byte is identical, find the one with the longest continuous length
    of identical bytes, from this byte to the end of the continuous identical subsequence.

    (
    Will identical sequences from different offsets ever overlap?
    if so, we need to check for identical sequences which start in the middle of a longer section,
    and re-calculte its length starting from rghat oint
    )
    *
    * */

}
