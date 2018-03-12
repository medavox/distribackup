package com.medavox.distribackup.dvcs.mydiff;

/**Represents a sequence of identical bytes between two files,
 * given the specified offset of B from A.*/
public class OffsetSubsequence extends Subsequence {
    /*for each offset:long,
     * a list of every matching subsequence:
     *      the start in A,
     *      (the start in B is derived from the start in A + offset)
     *      and the length of the matching sequence*/
    /**The offset which which the start and end/length applies to*/
    public final long OFFSET;
    //public final Subsequence MATCHING_SEQUENCE;

    public OffsetSubsequence(long matchingRunLength,
                             long matchingRunIndex, long offset) {
        super(matchingRunIndex, matchingRunLength, Type.START_AND_LENGTH);
        OFFSET = offset;
    }
}
