package com.medavox.distribackup.datamodels;

public class Hash {
    public final byte[] bytes;
    public final int HASH_BYTES = 20;
    public Hash(byte[] data) throws IllegalArgumentException {
        if(data.length != HASH_BYTES) {
            throw new IllegalArgumentException("incorrect size byte array passed to constructor. Hashes must be "+
            HASH_BYTES+" bytes long");
        }
        bytes = data;
    }
}
