package com.medavox.distribackup.dvcs.mydiff;

/**Represents a sequence of identical bytes between two files,
 * given the specified offset of B from A.*/
public class OffsetMatchingSequence {
    /**for each offset:long,
     * a list of every matching subsequence:
     *      the start in A,
     *      (the start in B is derived from the start in A + offset)
     *      and the length of the matching sequence*/
    public final long OFFSET_OF_B_FROM_A;
    public final Subsequence MATCHING_SEQUENCE;

    public OffsetMatchingSequence(long matchingRunLength,
                                  long matchingRunIndex, long shift) {
        OFFSET_OF_B_FROM_A = shift;
        MATCHING_SEQUENCE = Subsequence.fromStartAndLength(matchingRunIndex, matchingRunLength);
    }
}
