package com.medavox.distribackup.dvcs;

import java.io.*;
import java.util.Arrays;

/**Allows us to use an existing diff engine (GNU diff, or otherwise on other systems)
 * rather than write our own, or use someone else's (old/poorly-maintained/buggy/rarely-used) java implementation.
 *
 * This method shouldn't directly store the output of the diff command into a String,
 * because that would mean reading file-size amounts of data (gigabytes, potentially) into memory.*/
public abstract class DiffWrapper {

    public static final int IN_MEMORY_BUFFER_SIZE = 1048576;//1MiB

    public interface DiffResult {
        void onDiffResult(int exitCode);
        void onError(Throwable t);
    }

    public static void asyncDiff(OutputStream o, File a, File b, DiffResult callback) {
        ProcessBuilder pb = new ProcessBuilder("/usr/bin/diff", a.getAbsolutePath(), b.getAbsolutePath());
        try {
            Process p = pb.start();
            int exitCode = p.waitFor();
            if(exitCode == 1) {
                //int charRead = p.getInputStream().read();
                byte[] buffer = new byte[IN_MEMORY_BUFFER_SIZE];
                InputStream stdout = p.getInputStream();

                int bytesRead = stdout.read(buffer);
                while(bytesRead == buffer.length) {
                    o.write(buffer);
                    bytesRead = stdout.read(buffer);
                }
                //write remainder
                o.write(buffer,0, bytesRead);
            }//if the exit code was 0 (files are identical) or otherwise,
            //then there's no need to write the diff output
            callback.onDiffResult(exitCode);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError(e);
        }
    }

    public static void diff(PrintStream o, File a, File b) {
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
            if(returnCode == 0) {
                return;
            }
            //System.out.println("waitFor(): "+p.waitFor());
            //Process p = Runtime.getRuntime().exec(new String[]{"/usr/bin/diff", a.getAbsolutePath(), b.getAbsolutePath()});

            int charRead = p.getInputStream().read();
            char[] buffer = new char[IN_MEMORY_BUFFER_SIZE];
            int i = 0;
            while(charRead != -1) {
                if(i >= buffer.length) {
                    //buffer is full. print it out, reset the buffer and keep going
                    o.print(buffer);
                    i = 0;
                }
                buffer[i] = (char)charRead;
                charRead = p.getInputStream().read();
                i++;
            }
            String remainingUnprintedOutput = new String(Arrays.copyOfRange(buffer, 0, i));
            o.println(remainingUnprintedOutput);
            System.out.println("buffer size:"+(i+1));
            //pb.start();
        }catch(Exception ioe) {
            ioe.printStackTrace();
        }
    }



    public static void main(String[] args) {
        //System.out.println("flagons of ale!");
        diff(System.out, new File("/Users/adamh/justy.txt"), new File("/Users/adamh/netsy.txt"));
    }
}
