package com.medavox.distribackup.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class JavaHashing extends HashingWrapper {
    private MessageDigest javaHasher;
    public JavaHashing(Algorithm algo) {
        try {
            javaHasher = MessageDigest.getInstance(algo.name);
        }catch(NoSuchAlgorithmException nsae) {
            javaHasher = null;
            //this is a static string. we just KNOW it's right.
            //so no exception handling is necessary
        }
    }
    @Override
    public void addBytes(byte[] data) {
        javaHasher.update(data);
    }

    @Override
    public byte[] computeHash() {
        return javaHasher.digest();
    }

    @Override
    public void reset() {
        javaHasher.reset();
    }

    public enum Algorithm {
        SHA1 ("SHA-1"),
        SHA256 ("SHA-256"),
        ;
        public final String name;
        Algorithm(String algo) {
            name = algo;
        }
    }
}
