package com.medavox.distribackup.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**Allows us to use an existing diff engine (GNU diff, or otherwise on other systems)
 * rather than write our own, or use someone else's (old/poorly-maintained/buggy/rarely-used) java implementation.
 *
 * This method probably shouldn't directly store the output of the diff command into a String,
 * because that would mean reading file-size amounts of data (gigabytes, potentially) into memory.*/
public abstract class DiffWrapper {
    public static void diff(File a, File b) {
        ProcessBuilder pb = new ProcessBuilder("/usr/bin/diff", a.getAbsolutePath(), b.getAbsolutePath());
        /*ProcessBuilder pb = new ProcessBuilder( "/usr/local/bin/bash", "-c", "/bin/sleep 5 && /usr/bin/diff "
                +a.getAbsolutePath()+" "+b.getAbsolutePath());*/
        //pb.redirectErrorStream(true);//print error messages on standard output
        //ProcessBuilder pb = new ProcessBuilder("/bin/sleep", "5");
        try {

            /*Process p = Runtime.getRuntime().exec(new String[]{"/usr/local/bin/bash", "-c", "/bin/sleep 5 ; /usr/bin/diff "
                    +a.getAbsolutePath()+" "+b.getAbsolutePath()});//*/
            Process p = pb.start();
            int returnCode = p.waitFor();
            //System.out.println("waitFor(): "+p.waitFor());
            //Process p = Runtime.getRuntime().exec(new String[]{"/usr/bin/diff", a.getAbsolutePath(), b.getAbsolutePath()});

            int charRead = p.getInputStream().read();
            char[] buffer = new char[4096];
            int i = 0;
            while(charRead != -1) {
                if(i >= buffer.length) {
                    //buffer is full. print it out, reset the buffer and keep going
                    System.out.println(buffer);
                    i = 0;
                }
                buffer[i] = (char)charRead;
                charRead = p.getInputStream().read();
                i++;
            }
            String remainingUnprintedOutput = new String(Arrays.copyOfRange(buffer, 0, i));
            System.out.println(remainingUnprintedOutput);
            System.out.println("buffer size:"+(i+1));
            //pb.start();
        }catch(Exception ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //System.out.println("flagons of ale!");
        diff(new File("/Users/adamh/justy.txt"), new File("/Users/adamh/netsy.txt"));
    }
}
