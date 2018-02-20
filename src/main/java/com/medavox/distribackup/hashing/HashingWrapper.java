package com.medavox.distribackup.hashing;

import okio.BufferedSource;
import okio.Okio;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**Abstracts away implementations of our hashing function,
 * so we can easily test and swap out different hashing algorithms*/
public abstract class HashingWrapper {
    public static final int FILE_HASHING_BUFFER_SIZE = 1048576;//1MiB

    public abstract void addBytes(byte[] data);
    public abstract byte[] computeHash();
    public abstract void reset();
    private static HashingWrapper instance;
    public static HashingWrapper get(){
        if(instance==null) {
            instance = new JavaHashing(JavaHashing.Algorithm.SHA256);
        }
        return instance;
    }

    public static byte[] hashFile(File fileToAdd) throws IOException {
        byte[] buffer = new byte[FILE_HASHING_BUFFER_SIZE];
        HashingWrapper hasher = HashingWrapper.get();
        //FileInputStream doesn't cut it here, it only supports files of size <2GiB
        BufferedSource bufferedSource = Okio.buffer(Okio.source(fileToAdd));
        int bytesRead = bufferedSource.read(buffer);
        while(bytesRead == buffer.length) {
            hasher.addBytes(buffer);
            bytesRead = bufferedSource.read(buffer);
        }
        hasher.addBytes(Arrays.copyOfRange(buffer, 0, bytesRead));
        bufferedSource.close();
        return hasher.computeHash();
    }
}
