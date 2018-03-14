package com.medavox.distribackup.dvcs.mydiff.matching_sequence_trackers;

import com.medavox.distribackup.dvcs.mydiff.Subsequence;

public abstract class MatchingSequenceTracker {
    public abstract Subsequence getLongestUnmatchedSequence();
    public abstract void setSequenceAsAccountedFor(long start, long end) throws IllegalArgumentException, DoesNotFitException;

    public static MatchingSequenceTracker getNew(long fileLength) {
        return new MatchingSequenceTrackerSecondAttempt(fileLength);
    }
}
