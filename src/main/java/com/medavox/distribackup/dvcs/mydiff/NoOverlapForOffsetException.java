package com.medavox.distribackup.dvcs.mydiff;

public class NoOverlapForOffsetException extends Exception {
    public NoOverlapForOffsetException(long lengthOfA, long lengthOfB, long offset) {
        super("array A of length "+lengthOfA+" and array B of length "+lengthOfB+
                " do not overlap, when offset is "+offset);
    }
}
