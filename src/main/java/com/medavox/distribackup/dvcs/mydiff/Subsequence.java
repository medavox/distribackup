package com.medavox.distribackup.dvcs.mydiff;


import static com.medavox.util.validate.Validator.check;

/**Represents a start/end pair.*/
public class Subsequence {
    public final long START;
    public final long END;
    public final long LENGTH;
    public enum Type {
        START_AND_LENGTH,
        START_AND_END,
        ;
    }

    public Subsequence(long start, long endOrLength, Type type) {
        check(start >= 0,
                new IllegalArgumentException("start must be >= 0. Passed start value: "+start));
        START = start;
        switch(type) {
            case START_AND_END:
                check(start < endOrLength,
                        new IllegalArgumentException("start must be before end. Passed start: "+start
                                +"Passed end: "+endOrLength));
                END = endOrLength;
                LENGTH = END - START;
                break;
            case START_AND_LENGTH:
            default:
                LENGTH = endOrLength;
                END = START + LENGTH;
        }


    }

    public static Subsequence fromStartAndEnd(long start, long end) throws IllegalArgumentException {
        return new Subsequence(start, end, Type.START_AND_END);

    }
    public static Subsequence fromStartAndLength(long start, long length) throws IllegalArgumentException {
        return new Subsequence(start, length, Type.START_AND_LENGTH);
    }
}
