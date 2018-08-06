/**
 * Problem Statement:
 * Given a very large text file that may not fit in available memory,
 * create a file that contains the distinct words from the original file sorted in the ascending order.
 *
 * Solution Approach:
 * Step 1: Slice the big file into smaller files.
 * Step 2: Create one thread for each file to process. Use a threadpool with fixed number of threads.
 * Step 2.1: Each thread will create new files for the words starting with same letter.( like t1-a.txt, t1-b.txt ..)
 * Step 3: Then merge each files with same letters ( t1-a.txt, t2-a.txt, ...) and create a new file (a.txt) with all the words starting with same letter. And sort it.
 * Step 4: Merge all the files,26,(a.txt, b.txt ... z.txt)
 *
 * @author  Pijus Kumar Sarker
 * @version 1.0
 * @since   2018-07-31
 * @link https://github.com/pk-sarker/sorting-large-file
 **/

package pijus.java.exercises;

import java.io.IOException;
import java.util.*;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class FileProcessorMain {

    private static Hashtable<String, HashSet<String>> alphabeticFiles = new Hashtable<String, HashSet<String>>();
    private static Hashtable<String, String> mergedAlphabeticFiles = new Hashtable<String, String>();
    private static FreeDiskMemory freeDiskMemory = new FreeDiskMemory();

    public static void main(String args[]) throws IOException {
        System.out.println("Memory Use: " + (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024);

        // Split sample.txt file in smaller files
        // F -> { F1, F2, F3, .... Fn }
        // Sample.txt -> { Sample-000.txt, Sample-001.txt ... }
        Set<String> splittedFiles = splitFile("io/sample.txt");

        // For each splitted file create a set of files for the words that starts with same Letters
        // F1 -> {
        //      JDK 10 Release Notes
        //      The complete Java SE 10 API Specification
        // }
        //
        // Word Files: {
        // T_x_sample_J, T_x_sample_1, T_x_sample_R, T_x_sample_N, T_x_sample_T,
        // T_x_sample_C, T_x_sample_S, T_x_sample_A
        // }
        // T_x_sample_J: { JDK, Java }
        // T_x_sample_1: { 10 }
        // T_x_sample_R: { Release }
        // T_x_sample_N: { Notes }
        // T_x_sample_T: { The },
        // T_x_sample_C: { complete },
        // T_x_sample_S: { Specification },
        // T_x_sample_A: { API },

        splitFileByWordsFirstLetter(splittedFiles);

        // Delete splited files: Sample-000.txt, Sample-001.txt
        Iterator<String> fileItr = splittedFiles.iterator();
        while(fileItr.hasNext()) {
            FileProcessorMain.freeDiskMemory.deleteFile("io/"+fileItr.next());
        }

        displayAlphabeticFiles();

        // Merge all the files that contains words starting with same letter to one file
        // Consider
        //      Thread 1 process a file split Sample-001.txt and produced T_1_sample_A, T_1_sample_T, T_1_sample_M files.
        //      Thread 2 process a file split Sample-005.txt and produced T_2_sample_Z, T_2_sample_C, T_2_sample_A files.
        //      Thread 3 process a file split Sample-003.txt and produced T_3_sample_M, T_3_sample_A, T_3_sample_L, T_3_sample_I, T_3_sample_T files.
        // Then,
        // File A -> T_1_sample_A + T_2_sample_A + T_3_sample_A
        // File T -> T_1_sample_T + T_3_sample_T
        // File M -> T_1_sample_M + T_3_sample_M
        // File Z -> T_2_sample_Z
        // File C -> T_2_sample_C
        // File L -> T_3_sample_L
        // File I -> T_3_sample_I

        mergeAlphabeticFiles();

        // Sort the words in each that contains the words starting with same letter/character and kept in a new file
        // A thread has been assigned to each file ( A, T, M, Z ...) to do the sorting in parallel.
        // File { A.txt -> sorted_A.txt, T.txt -> sorted_T.txt, M.txt -> sorted_M.txt, ...  }

        Hashtable<String, String> sortedFiles = sortAlphabeticFiles();

        // Delete unused files: T_1_sample_A, T_1_sample_T, T_1_sample_M ... T_3_sample_I, T_3_sample_T
        Set<String> keys = FileProcessorMain.alphabeticFiles.keySet();
        Iterator<String> itr = keys.iterator();

        while(itr.hasNext()){
            String fileKey = itr.next();
            HashSet<String> files = FileProcessorMain.alphabeticFiles.get(fileKey);
            Iterator<String> itr2 = files.iterator();
            while(itr2.hasNext()) {
                FileProcessorMain.freeDiskMemory.deleteFile(itr2.next());
            }
        }


        // Merge all the alphabetic files ( sorted_A, sorted_T, sorted_M, sorted_Z ...) in sequence.
        finalMerge(sortedFiles);

        System.out.println("Memory Use: " + (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024);
    }

    /**
     * Returns a set of file names which are splitted from large file.
     * Consider Sample.txt is the large file. In this step that Sample.txt
     * file is splitted in to smaller files: sample-001.txt, sample-002.txt ... sample-nnn.txt
     *
     * @param  inputFile  an relative path of the input file
     * @return  returns a set of strings where each element of the set is a path to an splitted file.
     */
    public static Set<String> splitFile(String inputFile) throws IOException {
        LineReader reader = new LineReader(32);
        reader.ScanByLine(inputFile);
        return reader.getSplitedFiles();
    }

    /**
     * Returns a set of file names which are splitted from large file.
     * Consider { sample-001.txt, sample-002.txt ... sample-nnn.txt } are the smaller
     * files obtained after spliting Sample.txt.
     * In this step, a thread is created/assigned to process each file. In the thread-pool
     * fixed number of threads are used.
     *
     * Each thread creates a number of new files where each file contains the words that started with same letter.
     * For example, Thread 1 is processing sample-001.txt file. On completion, it will create some files like:
     * T_1_sample_A, T_1_sample_T, T_1_sample_M, ....
     * 
     * @param  splittedFiles  A set of file names that are obtained from initial split
     * @return  void
     */
    public static void splitFileByWordsFirstLetter(Set<String> splittedFiles) {
        Integer numberOfThreads = 5;
        Iterator<String> splittedFilesItr = splittedFiles.iterator();

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads); //creating a pool of 5 threads
        int fileCount = 0;
        while(splittedFilesItr.hasNext()) {
            Callable worker = new LineProcessor("io/" + splittedFilesItr.next(), "t"+Integer.toString(fileCount++));
            Future<Hashtable<String, HashSet<String>>> future = executor.submit(worker);

            Hashtable<String, HashSet<String>> result = new Hashtable<String, HashSet<String>>();
            // { a: { abc, axr, amn}, b: { bbb, bcd, bgh } }
            // @TODO: add
            try {
                result = future.get();
                Set<String> keys = result.keySet();
                Iterator<String> itr = keys.iterator();

                while(itr.hasNext()){
                    String fileKey = itr.next();

                    if(FileProcessorMain.alphabeticFiles.containsKey(fileKey)) {
                        FileProcessorMain.alphabeticFiles.get(fileKey).addAll(result.get(fileKey));
                    } else {
                        FileProcessorMain.alphabeticFiles.put(fileKey,result.get(fileKey));
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        awaitTerminationAfterShutdown(executor);
    }

    public static void displayAlphabeticFiles() {
        Set<String> keys = FileProcessorMain.alphabeticFiles.keySet();
        Iterator<String> itr = keys.iterator();

        while(itr.hasNext()){
            String fileKey = itr.next();
            HashSet<String> files = FileProcessorMain.alphabeticFiles.get(fileKey);

            Iterator<String> itr2 = files.iterator();

            while(itr2.hasNext()) {
                System.out.println(" | File Key: " + fileKey + " >> " + itr2.next());
            }

        }
    }

    public static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(120, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // {
    //   a: {
    //     abc, axrio, a9, as
    //   },
    //   a: {
    //     bco, bbc
    //   },
    // }
    //
    // ->
    // {
    //  a, b, c, ... z
    // }
    public static void mergeAlphabeticFiles() {
        System.out.println("======================= mergeAlphabeticFiles =======================");
        Set<String> keys = FileProcessorMain.alphabeticFiles.keySet();
        Iterator<String> fileItr = keys.iterator();

        ExecutorService executor = Executors.newFixedThreadPool(5); //creating a pool of 5 threads

        int fileCount = 0;
        while(fileItr.hasNext()) {

            // create a base file for each distinct alphabet
            // pass base file(where to merge), and the file to merge

            String fileKey = fileItr.next();
            FileProcessorMain.mergedAlphabeticFiles.put(fileKey.toUpperCase(), "io/"+fileKey.toUpperCase()+".txt");
            Callable worker = new FileMerger("io/"+fileKey.toUpperCase()+".txt", FileProcessorMain.alphabeticFiles.get(fileKey));
            Future<String> future = executor.submit(worker);
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("======================= End mergeAlphabeticFiles =======================");
    }

    public static Hashtable<String, String> sortAlphabeticFiles() {
        System.out.println("======================= sortAlphabeticFiles =======================");
        Hashtable<String, String> sortedAlphabeticFile = new Hashtable<String, String>();
        Set<String> keys = FileProcessorMain.mergedAlphabeticFiles.keySet();
        Iterator<String> fileItr = keys.iterator();

        ExecutorService executor = Executors.newFixedThreadPool(5); //creating a pool of 5 threads
        // {
        //   a: {
        //     abc, axrio, a9, as
        //   },
        //   a: {
        //     bco, bbc
        //   },
        // }
        //
        // ->
        // {
        //  a, b, c, ... z
        // }
        int fileCount = 0;
        while(fileItr.hasNext()) {

            // create a base file for each distinct alphabet
            // pass base file(where to merge), and the file to merge
            String fileKey = fileItr.next();
            Callable worker = new MergeSort(fileKey, FileProcessorMain.mergedAlphabeticFiles.get(fileKey));

            Future<Hashtable<String, String>> future = executor.submit(worker);
            Hashtable<String, String> result = null;
            try {
                result = future.get();
                sortedAlphabeticFile.put(fileKey, result.get(fileKey));
                FileProcessorMain.freeDiskMemory.deleteFile(FileProcessorMain.mergedAlphabeticFiles.get(fileKey));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        }
        executor.shutdown();
        awaitTerminationAfterShutdown(executor);

        // Delete temporary files
//        keys = FileProcessorMain.mergedAlphabeticFiles.keySet();
//        fileItr = keys.iterator();
//
//        while(fileItr.hasNext()) {
//            FileProcessorMain.freeDiskMemory.deleteFile(FileProcessorMain.mergedAlphabeticFiles.get(fileItr.next()));
//        }

        System.out.println("======================= END sortAlphabeticFiles =======================");
        return sortedAlphabeticFile;
    }



    // merge {A, B, C, D .... Z } to SortedFile.

    public static void finalMerge(Hashtable<String, String> sortedFiles) {
        System.out.println("======================= finalMerge =======================");

        Set<String> keys = sortedFiles.keySet();
        List sortedKeys = new ArrayList(keys);
        Collections.sort(sortedKeys);

        // sort the keys;
        Iterator<String> letters = sortedKeys.iterator();
        FileMerger merger = new FileMerger("io/SORTED_FILE.txt", new HashSet<String>());
        while (letters.hasNext()) {
            String letter = letters.next();
            //System.out.println(">> " + letter + " " + sortedFiles.get(letter));
            merger.mergeFile(sortedFiles.get(letter), "io/SORTED_FILE.txt");
            FileProcessorMain.freeDiskMemory.deleteFile(sortedFiles.get(letter));

        }

    }
}
