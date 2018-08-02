package pijus.java.exercises;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.FileOutputStream;

public class FileSplitter implements Runnable {

    public void run() {
        System.out.println(" File Splitter running ");
        try {


            Thread.sleep(5000);
            //Get database connection, delete unused data from DB
            //doDBProcessing();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void split(String threadName) throws IOException {
        File file = new File("input/100-0.txt");
        FileSplitter.splitFile(file);
    }

    public static void splitFile(File f) throws IOException {
        int partCounter = 1;//I like to name parts from 001, 002, 003, ...
        //you can change it to 0 if you want 000, 001, ...

        int sizeOfFiles = 1024 * 1024;// 1MB
        byte[] buffer = new byte[sizeOfFiles];

        String fileName = f.getName();

        //try-with-resources to ensure closing stream
        try (
                FileInputStream fis = new FileInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(fis)
        ) {

            int bytesAmount = 0;
            while ((bytesAmount = bis.read(buffer)) > 0) {
                System.out.println("bytesAmount " + Integer.toString(bytesAmount));
                //write each chunk of data into separate file with different number in name
                String filePartName = String.format("%s-%03d.txt", fileName.substring(0, fileName.lastIndexOf('.')), partCounter++);
                System.out.println("filePartName: "+filePartName + " >> Parent: "+ f.getParent());
                File newFile = new File(f.getParent(), filePartName);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, bytesAmount);
                }
            }
        }
    }
}
