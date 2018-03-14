package com.medavox.distribackup.dvcs.mydiff.matching_sequence_trackers;

/**Occurs when a subsequence does not fit in a list of other indices for a file,
 * because some or all of its marked indices (between its end and start) overlap
 * with existing subsequences*/
public class DoesNotFitException extends RuntimeException {
}
