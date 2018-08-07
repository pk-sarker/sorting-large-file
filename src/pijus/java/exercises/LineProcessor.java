/**
 * LineProcessor class used to parse a file and create files with words starting with same letter
 *
 * @author  Pijus Kumar Sarker
 * @version 1.0
 * @since   2018-08-04
 **/

package pijus.java.exercises;

import java.io.*;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.Callable;

public class LineProcessor implements Callable<Hashtable<String, HashSet<String>>> {

    private static Hashtable<String, HashSet<String>> alphabeticFileSet = new Hashtable<String, HashSet<String>>();

    static protected HashSet<String> words = new HashSet<String>();
    private String filePath = "";
    private String filePrefix = "";

    LineProcessor(String filePath, String filePrefix) {
        this.filePath = filePath;
        this.filePrefix = filePrefix;
    }

    /**
     *  This method parses process input file and creates a set of files where the words starts with same letter
     *
     *  @return returns a Hashtable
     **/
    public Hashtable<String, HashSet<String>> call() throws Exception {
        System.out.println(Thread.currentThread().getName()+ " " + this.filePath + " (Start) ");
        try {

            Thread.sleep(1000);
            try {
                process(this.filePath, this.filePrefix);
            }
            catch(IOException e) {
                e.printStackTrace();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+" (End)");//prints thread name
        return LineProcessor.alphabeticFileSet;

    }

    /**
     *  This method parses a file and creates a set of files where each file contains words starts with same letter
     *  After parsing it deletes the input file.
     *
     *  @param filePath input file name
     *  @param filePrefix the file key for each thread, like t0, t1 ...
     *
     *  @return void
     **/
    public void process(String filePath, String filePrefix) throws IOException {
        System.out.println("FilePath: "+ filePath + "  Prefix: "+filePrefix);
        File file = new File(filePath);
        FileInputStream inputStream = new FileInputStream(file);
        Scanner sc = new Scanner(inputStream, "UTF-8");
        FileOutputStream out = new FileOutputStream(new File("io/"+filePrefix + "_test.txt"));
        String alphFileName = "io/"+filePrefix + "_test_###.txt";
        try {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String wordsInLine[] = line.split(" ");
                for(String word: wordsInLine) {

                    if(word != "" && !words.contains(word)) {
                        words.add(word);
                        writeToAlphabetFile(word, alphFileName);
                    }
                }
            }

            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } catch (OutOfMemoryError excp) {
            System.out.println("========== Error : Out Of Memory Error. ==========");

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }

        // delete test file
        FreeDiskMemory freeDiskMemory = new FreeDiskMemory();
        freeDiskMemory.deleteFile("io/"+filePrefix + "_test.txt");
    }

    /**
     *  This method writes a word to a given file
     *
     *  @param word input word
     *  @param filePath the file path where the word will be written.
     *  @return void
     **/
    public void writeToAlphabetFile(String word, String filePath) throws FileNotFoundException, IOException {
        try {
            if(word == "" || word.length() == 0 ) {
                return;
            }
            word = word.replaceAll("[-+.^:,()]","");
            String firstLetter = word.substring(0, 1).toUpperCase();
            String fname = filePath.replace("###", firstLetter);
            File file = new File(fname);

            if(!file.exists()) {
                if(LineProcessor.alphabeticFileSet.containsKey(firstLetter)) {
                    LineProcessor.alphabeticFileSet.get(firstLetter).add(fname);
                } else {
                    HashSet<String> temp = new HashSet<String>();
                    temp.add(fname);
                    LineProcessor.alphabeticFileSet.put(firstLetter, temp);
                }
            }
            FileOutputStream out = new FileOutputStream(file, true);
            out.write(word.getBytes());
            out.write("\n".getBytes());
            out.close();
        } catch (FileNotFoundException ex) {
            System.out.print(ex);
        } finally {

        }

    }
}
