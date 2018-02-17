package com.medavox.distribackup.hashing;
/**Abstracts away implementations of our hashing function,
 * so we can easily test and swap out different hashing algorithms*/
public abstract class HashingWrapper {

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
}
