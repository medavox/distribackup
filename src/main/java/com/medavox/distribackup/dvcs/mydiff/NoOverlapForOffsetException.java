package com.medavox.distribackup.dvcs.mydiff;

/**Occurs when two arrays have no overlapping bytes for comparison,
 * given the provided offset and their lengths*/
public class NoOverlapForOffsetException extends Exception {
    public NoOverlapForOffsetException(long lengthOfA, long lengthOfB, long offset) {
        super("array A of length "+lengthOfA+" and array B of length "+lengthOfB+
                " do not overlap, when offset is "+offset);
    }
}
