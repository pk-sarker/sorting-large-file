/**
 * FileMerger class used to merge files. In the constructor it accepts two parameters,
 * the file path to merged file(fileMergeTo) and the set of files(files) to merge.
 *
 * @author  Pijus Kumar Sarker
 * @version 1.0
 * @since   2018-08-04
 **/

package pijus.java.exercises;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Callable;
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

    /**
     * This method merges a set of files to a desired file
     *
     * @param files set of file paths that is going to merge
     * @param fileMergeTo the file path where the file is going to merge
     * @return void
     **/
    public void mergeFiles (HashSet<String> files, String fileMergeTo) {
        Iterator<String> fileItr = files.iterator();

        while (fileItr.hasNext()) {
            String file = fileItr.next();
            this.mergeFile(file, fileMergeTo);
        }
    }

    /**
     * This method merges one file to a desired file
     *
     * @param file the file path that is going to merge
     * @param fileMergeTo the file path where the file is going to merge
     * @return void
     **/
    public void mergeFile(String file, String fileMergeTo) {
        Path outFile=Paths.get(fileMergeTo);
        try(FileChannel out=FileChannel.open(outFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE,  StandardOpenOption.APPEND)) {
            Path inFile=Paths.get(file);
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
