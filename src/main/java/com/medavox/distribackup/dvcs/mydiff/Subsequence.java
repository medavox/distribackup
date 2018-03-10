package com.medavox.distribackup.dvcs.mydiff;


import static com.medavox.util.validate.Validator.check;

/**Represents a start/end pair.*/
public class Subsequence {
    public final long START;
    public final long END;
    public Subsequence(long start, long end) throws IllegalArgumentException {

        check(start >= 0,
                new IllegalArgumentException("start must be >= 0. Passed start value: "+start));

        check(start < end,
                new IllegalArgumentException("start must be before end. Passed start: "+start
                        +"Passed end: "+end));

        START = start;
        END = end;
    }
    public long length() {
        return END - START;
    }
}
