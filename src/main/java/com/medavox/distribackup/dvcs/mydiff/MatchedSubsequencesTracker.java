package com.medavox.distribackup.dvcs.mydiff;

import static com.sun.tools.javac.util.Assert.check;

/**Keeps track of which parts of a file have been accounted for;
 * that is, which subsequences of each file have been identified as identical
 * to a part in the other file.*/
public class MatchedSubsequencesTracker {
    private final long FILE_LENGTH;
    private LinkedSequence head;
    //private LinkedSequence tail;
    public MatchedSubsequencesTracker(long fileLength) {
        FILE_LENGTH = fileLength;
    }

    public Subsequence getLongestUnmatchedSequence() {
        if(head == null) {//if there are no matched sequences, then the whole file is unmatched
            return new Subsequence(0, FILE_LENGTH);
        }
        LinkedSequence entryBeforeLongestUnmatched = head;
        long lengthOfLongestUnmatched = 0;

        //find start and end values of longest unmatched sequence
        for(LinkedSequence current = head; current != null; current = current.getNext()) {
            long currentLength;
            if (current.getNext() == null ) {//this is the last entry
                //so the file must be unmatched to the end
                currentLength = (FILE_LENGTH - current.END)-1;
                //if this LinkedSequence goes to the end, then currentLength will be 0
            }else{
                currentLength = (current.getNext().START - current.END)-1;
            }
            if(currentLength > lengthOfLongestUnmatched) {
                entryBeforeLongestUnmatched = current;
                lengthOfLongestUnmatched = currentLength;
            }
        }
        long startOfLongestUnmatched = entryBeforeLongestUnmatched.END+1;
        LinkedSequence entryAfterLongestUnmatched = entryBeforeLongestUnmatched.getNext();
        long endOfLongestUnmatched = (entryAfterLongestUnmatched == null ? FILE_LENGTH-1
                : entryAfterLongestUnmatched.START-1);
        return new Subsequence(startOfLongestUnmatched, endOfLongestUnmatched);
    }

    public void setSequenceAsAccountedFor(long start, long end) throws IllegalArgumentException {
        check(end < FILE_LENGTH,
                new IllegalArgumentException("end must be < fileLength. Passed end value: "+end));
        //should we also throw an IllegalArgumentException if the passed sequence overlaps another?
        if(head == null ) {
            head = new LinkedSequence(start, end);
            //tail = head;
            return;
        }

        LinkedSequence newOne = new LinkedSequence(start, end);
        //case: we should go before the first one
        if(newOne.END < head.START) {
            newOne.setNext(head);
            //head.setPrev(newOne);
            head = newOne;
            return;
        }
        LinkedSequence previousOne = null;
        for(LinkedSequence currentOne = head;
            currentOne != null;
            currentOne = currentOne.getNext()) {

            if(newOne.START > currentOne.END) {
                LinkedSequence nextOne = currentOne.getNext();

                if(nextOne == null) {
                    if(currentOne.END == newOne.START-1) {//if the values are next to each other, no gap
                        //merge new one with the current
                        LinkedSequence mergedOne = new LinkedSequence(currentOne.START, newOne.END);
                        if(previousOne != null) {
                            previousOne.setNext(mergedOne);
                        }
                        mergedOne.setNext(currentOne.getNext());
                        currentOne = mergedOne;
                        newOne = mergedOne;
                    }
                    else
                    //if next one is null, that should mean currentOne is the last
                    //case: we should go after the last one
                    //if(currentOne != newOne) {
                    currentOne.setNext(newOne);
                    //}
                    return;
                }else if(newOne.END < nextOne.START) {
                    if(currentOne.END == newOne.START-1) {//if the values are next to each other, no gap
                        //merge new one with the current
                        LinkedSequence mergedOne = new LinkedSequence(currentOne.START, newOne.END);
                        if (previousOne != null) {
                            previousOne.setNext(mergedOne);
                        }
                        mergedOne.setNext(currentOne.getNext());
                        currentOne = mergedOne;
                        newOne = mergedOne;
                    }
                    if (nextOne.START == newOne.END + 1) {
                        //the next one begins straight after this one, so merge that
                        LinkedSequence mergedOne = new LinkedSequence(newOne.START, nextOne.END);
                        if(currentOne != newOne) {//if we haven't also merged current and new
                            currentOne.setNext(mergedOne);
                        }
                        mergedOne.setNext(nextOne.getNext());
                        nextOne = mergedOne;
                        newOne = mergedOne;
                    }
                    //case: we should go between two existing items
                    if(currentOne != newOne){
                        currentOne.setNext(newOne);
                    }
                    if(nextOne != newOne) {
                        newOne.setNext(nextOne);
                    }
                    return;
                }
                else {
                    //case: the new one overlaps with one or two existing items,
                    //and we don't fit anywhere. This is an overlap error
                    return;
                }
                //(newOne.END < currentOne.START && newOne.START > previousOne.END) ==
                //(newOne.START > currentOne.END && newOne.END < nextOne.START)
            }
            previousOne = currentOne;//set previous to this loop's current, ready for the next loop
        }
    }

}
