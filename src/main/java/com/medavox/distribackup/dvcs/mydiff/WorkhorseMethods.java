package com.medavox.distribackup.dvcs.mydiff;

import okio.BufferedSource;
import okio.Okio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

class WorkhorseMethods {

    private static final int BUFFER_SIZE = 16 * 1024 * 1024;//16MiB

    /**Returns the absolute-in-file index of the first byte to differ between the two provided byte[]s,
     * starting from {@code start}, and offseting the start.
     * That is,
     * an offset of 3 will have A start at array index 3;
     * an offset of -3 will have B start at array index 3*/
    public static long indexOfNextDifferingByte(File a, File b, Subsequence toCompare, long offsetOfBFromA)
        throws NoOverlapForOffsetException, IOException {
        long index = toCompare.START;
        RandomAccessFile randomA = new RandomAccessFile(a, "r");
        RandomAccessFile randomB = new RandomAccessFile(b, "r");

        //if offset > 0:
            //start in A at subsequence.START
            //start in B at subsequence.START + offset
            //overlapLength = min(a.length-offset, b.length)
        //elif offset < 0:
            //start in A at subsequence.START + offset
            //start in B at subsequence.START
            //overlapLength = min(a.length, b.length-offset)

        //find out the ending index, which is the highest absolute file index we can compare to
        //this is min(subsequence.START+overlapLength, subsequence.END)

        //with the index as values from subsequence.START to highestIndexToCheck,
        //break if we find any dissiimlar byte
        //etc

        if(offsetOfBFromA > 0 ) {
            //offset is positive; compare a[n+offset] with b[n]

            long overlapLength = Math.min(a.length() - offsetOfBFromA, b.length());
            if(overlapLength <= 0 || offsetOfBFromA > a.length() || offsetOfBFromA > b.length()) {
                //there is no overlap with this offset
                throw new NoOverlapForOffsetException(a.length(), b.length(), offsetOfBFromA);
            }

            long highestIndexToCheck = Math.min(toCompare.START + overlapLength, toCompare.END);

            randomA.seek(index);
            randomB.seek(index + offsetOfBFromA);
            //while(randomA.readUnsignedByte() == randomB.readUnsignedByte()) {
            while(index <= highestIndexToCheck) {
                if(randomA.readUnsignedByte() == randomB.readUnsignedByte()) {
                    index++;
                    randomA.seek(index + toCompare.START);
                    randomB.seek(index + toCompare.START + offsetOfBFromA);
                }else {
                    break;
                }
            }
            return index;
        } else if (offsetOfBFromA < 0) {
            //offset is negative; compare a[n] with b[n+offset]
            //simply reverse the positions, and the polarity
            return indexOfNextDifferingByte(b, a, toCompare, Math.abs(offsetOfBFromA));
        }else {
            /*offset is 0;
            * compare both Files from their starting positions*/
            long shorterLength = Math.min(a.length()-toCompare.START, b.length()-toCompare.START);
            long highestIndexToCheck = Math.min(toCompare.START + shorterLength, toCompare.END);
            randomA.seek(index + toCompare.START);
            randomB.seek(index + toCompare.START);
            while(index <= highestIndexToCheck) {
                if(randomA.readUnsignedByte() == randomB.readUnsignedByte()) {
                    index++;
                    randomA.seek(index+toCompare.START);
                    randomB.seek(index+toCompare.START);
                }else {
                    break;
                }
            }
            return index;
        }
    }

    /*Starting from the beginning of both files,
    * check how far into both files they are identical
    *
    * for a subset of each file,
    *
    *
    *     AA[AA]AAAA
    *        ||
    * <--  B[BB]BBBBB -->
    *
    * create a byte array with the same length as the two subsets,
    * check if the pair of bytes in each position is the same, storing true or false accordingly
    *
    * until we find the offset with the fewest differences.
    *
    * we record the longest contiguous matching sequences.
    *
    * For every offset, compare the two files.
    * we record all long continuous stretches of identical bytes.
    * the goal is to find the offset which matches the most bytes (preferably continuously)
    * between the two files.
    *
    * we record each match sequence as its starting offset in each file, its length,
    * and the offset at which it occurs.
    * then we mark that off and look for the next offset which produces the next longest continuous run
    * which excludes the bytes from the previous runs  (the previous runs are discounted).
    *
    * Once we can no longer find any runs, or all bytes are accounted for,
    * then we generate the edit script using the record of longest runs,
    * and include any remaining literal differences
    * (insertions/deletions: sequences unique to either file).
    *
    * then, starting from the end of both files going backwards,
    * check how far from the end both files are identical*/

    //we don't record a full boolean[] of (a[i]==b[i]) for every byte comparison in the files;
    // that could take too much space in memory.
    //instead we record the start and end indexes of each run of identical bytes,
    //along with the offset at which it occurs.
    //to create these, we increment a long counter every time a[i] == b[i],
    //and reset the counter to 0 every time they are different
    //(before resetting, record the length and start position of that run of identical bytes)


    //every time we decide on which sequence of bytes at which offset is to be used,
    //record those bytes involved as 'solved', or used up, or whatever.
    //(this also goes for the pre-algorithm checking of the beginning and end of the files, count those out too)
    //for every run of checking sequences for a given offset,
    //make sure we're not diffing bytes which have been counted out already.
    //store this info for both files, as sequences in each file might get ticked off at different times


    //offset is positive; compare b[n] with a[n+offset]

            /*AAAAAAAAAAAA
                   BBBBBBBBBBBB
             = 7

             or
              AAAAAAAAAAAA
                   BBBB
             = 4

             or even
             AAAAA
                    BBBBB
             = 0

            * */

        /*offset is negative; move B to start before A
            compare b[n+offset] with a[n]

                   AAAAAAAAAAAA
            BBBBBBBBBBBBB


            or
                    AAAAA
            BBBBBBBBBBBBBB

            or
                    AAAAAAA
            BBBBBB


            equivalent to swapping A and B, and doing Math.abs() on the offset.

            eg a[i] == b[i+offset], where offset = -3
             is the same as
            a[i+offset] == b[i], where offset = 3
            * */
    //simply reverse the positions, and the polarity

    /*offset is 0;
            * compare both arrays from their starting positions*/

    /**The index of the first position in both files (their starts are aligned) where their contents differ.*/
    private long indexOfFirstDissimilarByteFromStart(File a, File b) throws IOException {
        byte[] bufferA = new byte[BUFFER_SIZE];
        byte[] bufferB = new byte[BUFFER_SIZE];
        long offset = 0;
        BufferedSource bufferedSourceA = Okio.buffer(Okio.source(a));
        BufferedSource bufferedSourceB = Okio.buffer(Okio.source(b));
        while(true) {
            int bytesReadA = bufferedSourceA.read(bufferA);
            int bytesReadB = bufferedSourceB.read(bufferB);
            int shorterFile = Math.min(bytesReadA, bytesReadB);
            int bufferOffset = 0;
            while(bufferOffset < bytesReadA && bufferOffset < bytesReadB
                    && bufferA[bufferOffset] == bufferB[bufferOffset]) {
                bufferOffset++;
            }
            offset += bufferOffset;
            if(bufferOffset < BUFFER_SIZE) {
                //if we found a dissimilar byte at a particular offset, we're done
                break;
            }
            if(shorterFile < BUFFER_SIZE) {
                //if we didn't fill one of the buffers on this cycle, there's no more to read
                break;
            }
        }
        //FileInputStream doesn't cut it here, it only supports files of size <2GiB
        bufferedSourceA.close();
        bufferedSourceB.close();
        return offset;
    }

    /**This method is negative-indexed: 0 is the last byte in both files; -1 the second-to-last, etc*/
    private long indexOfLastDissimilarByteFromEnd(File fileA, File fileB) throws IOException {
        byte[] bufferA = new byte[BUFFER_SIZE];
        byte[] bufferB = new byte[BUFFER_SIZE];
        long offset = 0;


        RandomAccessFile a = new RandomAccessFile(fileA, "r");
        RandomAccessFile b = new RandomAccessFile(fileB, "r");
        for(long i = 1; i > a.length() || i > b.length(); ) {
            long positionA = Math.max(0, a.length() - i - BUFFER_SIZE);
            long positionB = Math.max(0, b.length() - i - BUFFER_SIZE);
            a.seek(positionA);
            b.seek(positionB);
            int bytesReadA = a.read(bufferA);
            int bytesReadB = a.read(bufferB);


            int indexBackwardsA = bytesReadA-1;
            int indexBackwardsB = bytesReadB-1;
            while(indexBackwardsA >= 0 && indexBackwardsB >= 0) {
                if(bufferA[indexBackwardsA] != bufferB[indexBackwardsB]) {
                    break;
                }//else
                indexBackwardsA--;
                indexBackwardsB--;
                offset++;
            }
            int shorterRead = Math.min(bytesReadA, bytesReadB);
            i += shorterRead;
            if(bytesReadA != bytesReadB || shorterRead < BUFFER_SIZE) {
                break;
            }
        }
        return offset;
    }
}
