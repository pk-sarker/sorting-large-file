package pijus.java.exercises;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.Callable;
//import java.lang.*;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class FileMerger implements Callable<String> {
    private String fileMergeTo;
    private HashSet<String> files = new HashSet<String>();

    FileMerger(String fileMergeTo, HashSet<String> files) {
        this.fileMergeTo = fileMergeTo;
        this.files = files;
    }
    public String call() throws Exception {

        this.mergeFiles(this.files, this.fileMergeTo);
        return this.fileMergeTo;
    }

    public void mergeFiles (HashSet<String> files, String fileMergeTo) {
        Iterator<String> fileItr = files.iterator();

        while (fileItr.hasNext()) {
            String file = fileItr.next();
            this.mergeFile(file, fileMergeTo);
        }
    }

    public void mergeFile(String file, String fileMergeTo) {

        Path outFile=Paths.get(fileMergeTo);

        System.out.println("TO "+fileMergeTo);
        try(FileChannel out=FileChannel.open(outFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE,  StandardOpenOption.APPEND)) {
            Path inFile=Paths.get(file);
            System.out.println(inFile+"...");
            try(FileChannel in=FileChannel.open(inFile, StandardOpenOption.READ)) {
                for(long p=0, l=in.size(); p<l; )  {
                    p+=in.transferTo(p, l-p, out);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(">> Completed merging " + file + " file to " + fileMergeTo + " file");
    }

}
