package pijus.java.exercises;

import java.io.*;
import java.util.Scanner;
import java.util.HashSet;

public class LineProcessor implements Runnable {

    private static FileOutputStream foutps = null;

    static protected HashSet<String> words = new HashSet<String>();
    private String filePath = "";
    private String filePrefix = "";

    LineProcessor(String filePath, String filePrefix) {
        this.filePath = filePath;
        this.filePrefix = filePrefix;
    }
    public void run() {
        System.out.println(" Thread Line Processor running ");
        try {

            //this.process(this.filePath, this.filePrefix);
            Thread.sleep(1000);
            //Get database connection, delete unused data from DB
            //doDBProcessing();
            try {
                process(this.filePath, this.filePrefix);
            }
            catch(IOException e) {
                e.printStackTrace();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void process(String filePath, String filePrefix) throws IOException {
        System.out.println("FilePath: "+ filePath + "  Prefix: "+filePrefix);
        File file = new File(filePath);
        FileInputStream inputStream = new FileInputStream(file);
        Scanner sc = new Scanner(inputStream, "UTF-8");
        FileOutputStream out = new FileOutputStream(new File("input/"+filePrefix + "_test.txt"));
        String alphFileName = "input/"+filePrefix + "_test_###.txt";
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
    }

    public void writeToAlphabetFile(String word, String filePath) throws FileNotFoundException, IOException {
        try {
            if(word == "" || word.length() == 0 ) {
                return;
            }

            System.out.println("Word: " + word + " Len: "+ Integer.toString(word.length()));
            String fname = filePath.replace("###", word.substring(0, 1));
            File file = new File(fname);
            FileOutputStream out = new FileOutputStream(file, true);

            if(file.exists()) {
                System.out.println(" File >> " + fname + " << exists");
            } else {

            }

            out.write(word.getBytes());
            out.write("\n".getBytes());
            out.close();
            //String alphFileName = "input/"+filePrefix + "_test_###.txt";
        } catch (FileNotFoundException ex) {
            System.out.print(ex);
        } finally {
            System.out.println(" >> Added Word : "+word );

        }

    }
}
