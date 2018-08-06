package pijus.java.exercises;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Callable;

public class MergeSort implements Callable<Hashtable<String, String>> {
    public String fileName;
    public String filePath;

    MergeSort(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public Hashtable<String, String> call() throws Exception {
        Hashtable<String, String> sortedAlphabeticFile = new Hashtable<String, String>();
        TreeSet<String> words = new TreeSet<String>();
        //sortedAlphabeticFile.put(this.fileName, "io/sorted_"+this.fileName+".txt");

        // get words in memory,
        words = readWordsFromFile();

        // sort words
        List sortedList = new ArrayList(words);
        Collections.sort(sortedList);

        // print in a new file, sorted
        sortedAlphabeticFile = writeSortedWordsInFile(sortedList);

        return sortedAlphabeticFile;
    }

    public TreeSet<String> readWordsFromFile() throws IOException {
        TreeSet<String> words = new TreeSet<String>();

        // get words in memory,
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            File file = new File(this.filePath);
            inputStream = new FileInputStream(file);
            sc = new Scanner(inputStream, "UTF-8");

            while (sc.hasNextLine()) {
                words.add(sc.nextLine().trim());
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }
        return words;
    }

    public Hashtable<String, String> writeSortedWordsInFile(List sortedList) throws IOException {
        Hashtable<String, String> sortedAlphabeticFile = new Hashtable<String, String>();
        sortedAlphabeticFile.put(this.fileName, "io/sorted_"+this.fileName+".txt");
        Iterator<String> itr = sortedList.iterator();
        String test = "";

        FileOutputStream outStream = null;
        try {
            File outFile = new File(sortedAlphabeticFile.get(this.fileName));
            outStream = new FileOutputStream(outFile, true);

            while(itr.hasNext()) {
                String word = itr.next();
                test += word + ", ";
                outStream.write(word.getBytes());
                outStream.write("\n".getBytes());
            }

        } catch (FileNotFoundException exception) {
            // Output expected FileNotFoundExceptions.
            System.out.println(exception);
        } catch (IOException exception) {
            // Output unexpected Exceptions.
            System.out.println(exception);
        } catch (Exception exception) {
            // Output unexpected Exceptions.
            System.out.println(exception);
        } finally {
            if (outStream != null) {
                outStream.close();
            }
        }

        return sortedAlphabeticFile;
    }
}
