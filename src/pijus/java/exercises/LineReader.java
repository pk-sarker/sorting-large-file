package pijus.java.exercises;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LineReader {
    private int maxByte = 50;
    private Set<String> splitedFiles = new HashSet<String>();

    LineReader(int maxByte) {
        this.setMaxByte(maxByte);
    }

    public int getMaxByte() {
        return this.maxByte;
    }

    public void setMaxByte(int maxByte) {
        this.maxByte = maxByte;
    }

    public Set<String> getSplitedFiles() {
        return this.splitedFiles;
    }

    public void ScanByLine(String inputFile) throws IOException {
        Long mb = 1024L * 1024L;
        System.out.println(">> Max Memory: " + Long.toString(getMaxMemory()/mb));
        System.out.println(">> Used Memory: " + Long.toString(getUsedMemory()/mb));
        System.out.println(">> Total Memory: " + Long.toString(getTotalMemory()/mb));
        System.out.println(">> Free Memory: " + Long.toString(getFreeMemory()/mb));
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            File file = new File(inputFile);
            int maxFileSizeInByte = getMaxByte(); // 1MB
            int bytesAmount = 0;
            int partCounter = 0;

            inputStream = new FileInputStream(file);
            sc = new Scanner(inputStream, "UTF-8");

            String fileName = file.getName();
            File newFile = new File("temp.txt");
            FileOutputStream out = new FileOutputStream(newFile);
            String filePartName = "";
            while (sc.hasNextLine()) {
                System.out.println("------------------------------------------------------------");
                String line = sc.nextLine();

                // Check if buffer is full or not
                // if buffer is not full then write the line to buffer
                // BufferedInputStream bis = new BufferedInputStream(inputStream);

                int bytesInline = line.getBytes().length;

                if (bytesInline > maxFileSizeInByte) {
                    int wordsByteCount = 0;
                    String[] words = line.split(" ");
                    for (String word : words) {
                        int bytesInWord = word.getBytes().length;
                        if(wordsByteCount == 0 || (wordsByteCount + bytesInWord) > maxFileSizeInByte) {
                            filePartName = createNewFile(fileName, partCounter++);
                            out = new FileOutputStream(new File("input", filePartName));
                            splitedFiles.add(filePartName);
                        }
                        out.write(word.getBytes());
                        out.write("\n".getBytes());

                        if((wordsByteCount + bytesInWord) > maxFileSizeInByte) {
                            wordsByteCount = 0;
                        }
                        wordsByteCount += bytesInWord;
                    }
                    continue;
                }

                if(bytesAmount == 0 || (bytesAmount + bytesInline) > maxFileSizeInByte) {
                    filePartName = createNewFile(fileName, partCounter++);
                    out = new FileOutputStream(new File("input", filePartName));
                    splitedFiles.add(filePartName);
                }
                bytesAmount += bytesInline;


                out.write(line.getBytes());
                out.write("\n".getBytes());

                if(bytesAmount > maxFileSizeInByte) {
                    bytesAmount = 0;
                    bytesAmount += bytesInline;
                }

                // System.out.println(line + " --> " + bytesInline);
            }
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }

        System.out.println("<< Max Memory: " + Long.toString(getMaxMemory()/mb));
        System.out.println("<< Used Memory: " + Long.toString(getUsedMemory()/mb));
        System.out.println("<< Total Memory: " + Long.toString(getTotalMemory()/mb));
        System.out.println("<< Free Memory: " + Long.toString(getFreeMemory()/mb));
    }

    public String createNewFile(String baseFileName, Integer counter) {
        return String.format("%s-%03d.txt", baseFileName.substring(0, baseFileName.lastIndexOf('.')), counter);
    }

    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public static long getUsedMemory() {
        return getMaxMemory() - getFreeMemory();
    }

    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

}
