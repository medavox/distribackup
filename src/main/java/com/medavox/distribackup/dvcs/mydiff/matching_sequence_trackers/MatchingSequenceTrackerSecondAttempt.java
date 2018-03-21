package com.medavox.distribackup.dvcs.mydiff.matching_sequence_trackers;

import com.medavox.distribackup.dvcs.mydiff.Subsequence;

import java.util.LinkedList;
import java.util.List;

class MatchingSequenceTrackerSecondAttempt extends MatchingSequenceTracker {
    private final long FILE_LENGTH;
    private List<Subsequence> matchedSequences = new LinkedList<>();
    public MatchingSequenceTrackerSecondAttempt(long fileLength) {
        FILE_LENGTH = fileLength;
    }
    @Override
    public Subsequence getLongestUnmatchedSequence() {
        //todo
        return null;
    }

    @Override
    public void addSequence(long start, long end) throws IllegalArgumentException, DoesNotFitException {
        Subsequence newOne = Subsequence.fromStartAndEnd(start, end);
        if(matchedSequences.size() == 0) {
            matchedSequences.add(newOne);
            return;
        }
        boolean added = false;
        for(int i = 0; i < matchedSequences.size()-1; i++) {
            if(matchedSequences.get(i).END < newOne.START &&
                    matchedSequences.get(i+1).START > newOne.END) {
                matchedSequences.add(i+1, newOne);
                added = true;
                break;
                //return;
            }
        }
        if(!added) {
            if(matchedSequences.get(matchedSequences.size()-1).END < newOne.START) {
                //add the new one to the end
                matchedSequences.add(newOne);
                return;
            }
            //else, there was no gap for this one
            throw new DoesNotFitException();
        }
    }
}
