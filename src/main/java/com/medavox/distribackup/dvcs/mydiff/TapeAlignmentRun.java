package com.medavox.distribackup.dvcs.mydiff;

/**Represents a byte-by-byte diff of a subportion of two files A and B,
 * where the contents of B start at a given offset (positive or negative) from A.*/
public class TapeAlignmentRun {
    public final boolean[] matches;
    public final long globalShiftOfBfromA;
    public TapeAlignmentRun(boolean[] data, long matchingRunLength,
                            long matchingRunIndex, long shift) {
        matches = data;
        globalShiftOfBfromA = shift;

    }

    public static class MatchingSequence {
        public final long startingIndexInA;
        public final long length;

        public MatchingSequence(long length, long indexInA) {
            this.length = length;
            startingIndexInA = indexInA;
        }
    }
}
