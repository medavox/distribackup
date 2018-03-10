package com.medavox.distribackup.dvcs.mydiff;

class LinkedSequence extends Subsequence {
    private LinkedSequence next = null;
    //private LinkedSequence prev = null;

    public LinkedSequence(long start, long end) throws IllegalArgumentException {
        super(start, end, Type.START_AND_END);
    }

    public LinkedSequence getNext() {
        return next;
    }

    /*public LinkedSequence getPrev() {
        return prev;
    }//*/

    public void setNext(LinkedSequence next) {
        this.next = next;
    }

    /*public void setPrev(LinkedSequence prev) {
        this.prev = prev;
    }//*/
}
