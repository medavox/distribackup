package com.medavox.distribackup.dvcs;

import com.medavox.distribackup.hashing.HashingWrapper;
import com.medavox.util.io.Bytes;

import java.io.File;
import java.io.IOException;

public class CommandApi {
    public static final int FILE_HASHING_BUFFER_SIZE = 1048576;//1MiB

    /**
    1. Write the output of diff /dev/null <file> to a file named with the hash of the file's contents,
    stored in ./.distribackup

    2. Write a copy of the file itself to ./.distribackup/latest-edition/<path>/<file>

    3. Write the list of hashes that changed in this edition, along
    */
    public static void addCommand(File fileToAdd) {
        /*todo:
        perform centralised validation on the file object before it reaches this method:
        file exists
        is a file
        if an absolute path, path referenced is inside the repository directory*/
        Thread t = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    byte[] hash = HashingWrapper.hashFile(fileToAdd);
                    String hashAsHex = Bytes.bytesToHex(hash);
                }catch(IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }, "hashing file \""+fileToAdd.getName()+"\"");
        t.start();
    }


    /*Update <file>:
    *
    * */

    /*Remove <file>:
    * */

    /*If we automatically publish editions on every file change,
    then we may want to give users the ability to simplify down the edition history;
    to remove redundant intermediate editions before sending across the network*/
}
