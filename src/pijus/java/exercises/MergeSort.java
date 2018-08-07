/**
 * MergeSort class used to sort the words in a file and write in a new sorted file.
 * In the constructor it accepts the file name and file path
 *
 * @author  Pijus Kumar Sarker
 * @version 1.0
 * @since   2018-08-04
 **/

package pijus.java.exercises;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.Callable;

public class MergeSort implements Callable<Hashtable<String, String>> {
    public String fileName;
    public String filePath;

    MergeSort(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
    }

    /**
     *  This method reads words form given file, sorts them and writes in a file.
     *
     *  @return hashtable, where the key is the Letter(A, B, C ..) and value(sorted_A.txt, sorted_B.txt, sorted_C.txt ...) is the file path.
     **/
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

    /**
     *  This method reads all the words in a file
     *
     *  @return a TreeSet with all the word in a given file.
     **/
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

    /**
     *  This method writes a given list of words in a file
     *
     *  @param sortedList sorted word list
     *  @return  HashTable, were the key is letter (A, B, C ..) and the value is the file path( io/sorted_A.txt, io/sorted_B.txt ...)
     **/
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
