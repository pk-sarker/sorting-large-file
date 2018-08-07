/**
 * Problem Statement:
 * Given a very large text file that may not fit in available memory,
 * create a file that contains the distinct words from the original file sorted in the ascending order.
 *
 * Solution Approach:
 * Step 1: Slice the big file into smaller files.
 * Step 2: Create one thread for each file to process. Use a threadpool with fixed number of threads.
 * Step 2.1: Each thread will create new files for the words starting with same letter.( like t1-a.txt, t1-b.txt ..)
 * Step 3: Then merge each files with same letters ( t1-a.txt, t2-a.txt, ...) and create a new file (a.txt) with all the words starting with same letter.
 * Step 4: Sort the words in each file (a.txt, b.txt .....) and write to a new file (sorted_a.txt, sorted_b.txt, ...)
 * Step 4: Merge all the files,(sorted_a.txt, sorted_b.txt ... )
 *
 * @author  Pijus Kumar Sarker
 * @version 1.0
 * @since   2018-08-04
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
        //      Thread 1 process a file split Sample-001.txt and produced T_1_sample_A.txt, T_1_sample_T.txt, T_1_sample_M.txt files.
        //      Thread 2 process a file split Sample-005.txt and produced T_2_sample_Z.txt, T_2_sample_C.txt, T_2_sample_A.txt files.
        //      Thread 3 process a file split Sample-003.txt and produced T_3_sample_M.txt, T_3_sample_A.txt, T_3_sample_L.txt, T_3_sample_I.txt, T_3_sample_T.txt files.
        // Then,
        // File A.txt -> T_1_sample_A.txt + T_2_sample_A.txt + T_3_sample_A.txt
        // File T.txt -> T_1_sample_T.txt + T_3_sample_T.txt
        // File M.txt -> T_1_sample_M.txt + T_3_sample_M.txt
        // File Z.txt -> T_2_sample_Z.txt
        // File C.txt -> T_2_sample_C.txt
        // File L.txt -> T_3_sample_L.txt
        // File I.txt -> T_3_sample_I.txt

        mergeAlphabeticFiles();

        // Sort the words in each that contains the words starting with same letter/character and kept in a new file
        // A thread has been assigned to each file ( A.txt, T.txt, M.txt, Z.txt ...) to do the sorting in parallel.
        // File { A.txt -> sorted_A.txt, T.txt -> sorted_T.txt, M.txt -> sorted_M.txt, ...  }

        Hashtable<String, String> sortedFiles = sortAlphabeticFiles();

        // Delete unused files: T_1_sample_A.txt, T_1_sample_T.txt, T_1_sample_M.txt ... T_3_sample_I.txt, T_3_sample_T.txt
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


        // Merge all the alphabetic files ( sorted_A.txt, sorted_T.txt, sorted_M.txt, sorted_Z.txt ...) in sequence.
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
        FileSplitter reader = new FileSplitter(32);
        reader.ScanByLine(inputFile);
        return reader.getSplitedFiles();
    }

    /**
     * Create a set of files for each file, obtained by spliting the large file.
     * Each new file contains the words starting with same letter.
     * Consider { sample-001.txt, sample-002.txt ... sample-nnn.txt } are the smaller
     * files obtained after spliting Sample.txt.
     * In this step, a thread is created/assigned to process each file. In the thread-pool
     * fixed number of threads are used.
     *
     * Each thread creates a number of new files where each file contains the words that started with same letter.
     * For example, Thread 1 is processing sample-001.txt file. On completion, it will create some files like:
     * T_1_sample_A.txt, T_1_sample_T.txt, T_1_sample_M.txt, ....
     *
     * @param  splittedFiles  A set of file names that are obtained from initial split
     * @return  void
     */
    public static void splitFileByWordsFirstLetter(Set<String> splittedFiles) {
        Integer numberOfThreads = 5;
        Iterator<String> splittedFilesItr = splittedFiles.iterator();

        // creating a pool of 5 threads, will change based on memory allocation to JVM,
        // or number of distributed sub-systems
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        int fileCount = 0;
        while(splittedFilesItr.hasNext()) {
            Callable worker = new LineProcessor("io/" + splittedFilesItr.next(), "t"+Integer.toString(fileCount++));
            Future<Hashtable<String, HashSet<String>>> future = executor.submit(worker);

            Hashtable<String, HashSet<String>> result = new Hashtable<String, HashSet<String>>();
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

    /**
     * This method displays Alphabetic files(for A = {T_1_sample_A.txt, T_3_sample_A.txt .. })
     *
     * @return  void
     */
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

    /**
     * Wait for a shutdown pool to fully terminate, or until the timeout has expired.
     *
     * @param threadPool
     * @return void
     */
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

    /**
     * This method merges all the alphabetic files associated with the words starting with same letter.
     * Consider, T_1_sample_A.txt, T_1_sample_T.txt, T_2_sample_M.txt, T_2_sample_T.txt are the alphabetic files.
     * It will create the following files, excluding duplicate words,
     * A.txt = T_1_sample_A.txt
     * T.txt = T_1_sample_T.txt + T_2_sample_T.txt
     * M.txt = T_2_sample_M.txt
     *
     * @return void
     **/
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

        awaitTerminationAfterShutdown(executor);

        // some time to complete the writing process
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("======================= End mergeAlphabeticFiles =======================");
    }

    /**
     * This method sort the words in each alphabetic files(A.txt, B.txt)  and creates a new file(sorted_A.txt, sorted_B.txt ...) with sorted words.
     * A thread pool is used to read, sort and write. To obtain parallel process each file processed by one single thread.
     * When all thread completes their processing it returns a Hashtable with newly created file names(sorted_A.txt, sorted_B.txt ...)
     *
     * @return void Hashtable
     **/
    public static Hashtable<String, String> sortAlphabeticFiles() {
        System.out.println("======================= sortAlphabeticFiles =======================");
        Hashtable<String, String> sortedAlphabeticFile = new Hashtable<String, String>();
        Set<String> keys = FileProcessorMain.mergedAlphabeticFiles.keySet();
        Iterator<String> fileItr = keys.iterator();

        // creating a pool of 5 threads, will change based on memory allocation to JVM,
        // or number of distributed sub-systems
        ExecutorService executor = Executors.newFixedThreadPool(5);

        int fileCount = 0;
        while(fileItr.hasNext()) {

            String fileKey = fileItr.next();
            Callable worker = new MergeSort(fileKey, FileProcessorMain.mergedAlphabeticFiles.get(fileKey));

            Future<Hashtable<String, String>> future = executor.submit(worker);
            Hashtable<String, String> result = null;
            try {
                result = future.get();
                sortedAlphabeticFile.put(fileKey, result.get(fileKey));
                // Delete temporary files
                FileProcessorMain.freeDiskMemory.deleteFile(FileProcessorMain.mergedAlphabeticFiles.get(fileKey));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        }
        executor.shutdown();
        awaitTerminationAfterShutdown(executor);

        System.out.println("======================= END sortAlphabeticFiles =======================");
        return sortedAlphabeticFile;
    }



    //
    /**
     * This method, finalMerge, merged all the sorted files in ascending order to output file.
     * Merges { sorted_A.txt, sorted_B.txt, sorted_C.txt .... } files to output file, SORTED_FILE.txt
     *
     * @param sortedFiles
     * @return void
     **/
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
            merger.mergeFile(sortedFiles.get(letter), "io/SORTED_FILE.txt");
            FileProcessorMain.freeDiskMemory.deleteFile(sortedFiles.get(letter));

        }

    }
}
