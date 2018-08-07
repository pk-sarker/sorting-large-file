/**
 * FileSplitter class used to split a large file in to smaller files.
 * In the constructor it accepts the maximum file size for each splitted files.
 *
 * @author  Pijus Kumar Sarker
 * @version 1.0
 * @since   2018-08-04
 **/

package pijus.java.exercises;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;

public class FileSplitter {
    private int maxByte = 50;
    private Set<String> splitedFiles = new HashSet<String>();

    FileSplitter(int maxByte) {
        this.setMaxByte(maxByte);
    }

    /**
     *  This method return maximum allocated file size
     *
     *  @return maximum allocated file size
     **/
    public int getMaxByte() {
        return this.maxByte;
    }

    /**
     *  This method sets maximum allocated file size
     *
     *  @return void
     **/
    public void setMaxByte(int maxByte) {
        this.maxByte = maxByte;
    }

    /**
     *  This method returns a set of splitted files
     *
     *  @return set of splitted files.
     **/
    public Set<String> getSplitedFiles() {
        return this.splitedFiles;
    }

    /**
     * This method scans the input file line by line and creates a
     * new file if buffer or given memery space is full.
     * If a line is longer then the maximum allocated file size,
     * it parses the line by words and creates separate files with
     * the words where file size is less than maximum allocated file size
     *
     * Consider Sample.txt is the large file. In this step that Sample.txt
     * file is splitted in to smaller files: sample-001.txt, sample-002.txt ... sample-nnn.txt
     *
     * @param  inputFile  an relative path of the input file
     * @return  void
     */

    public void ScanByLine(String inputFile) throws IOException {
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            File file = new File(inputFile);
            int maxFileSizeInByte = getMaxByte();
            int bytesAmount = 0;
            int partCounter = 0;

            inputStream = new FileInputStream(file);
            sc = new Scanner(inputStream, "UTF-8");

            String fileName = file.getName();
            File newFile = new File("io","temp.txt");
            FileOutputStream out = new FileOutputStream(newFile);
            String filePartName = "";
            while (sc.hasNextLine()) {
                System.out.println("------------------------------------------------------------");
                String line = sc.nextLine();

                // Check if buffer is full or not
                // if buffer is not full then write the line to buffer

                int bytesInline = line.getBytes().length;

                if (bytesInline > maxFileSizeInByte) {
                    int wordsByteCount = 0;
                    String[] words = line.split(" ");
                    for (String word : words) {
                        int bytesInWord = word.getBytes().length;
                        if(wordsByteCount == 0 || (wordsByteCount + bytesInWord) > maxFileSizeInByte) {
                            filePartName = createNewFile(fileName, partCounter++);
                            out = new FileOutputStream(new File("io", filePartName));
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
                    out = new FileOutputStream(new File("io", filePartName));
                    splitedFiles.add(filePartName);
                }
                bytesAmount += bytesInline;

                out.write(line.getBytes());
                out.write("\n".getBytes());

                if(bytesAmount > maxFileSizeInByte) {
                    bytesAmount = 0;
                    bytesAmount += bytesInline;
                }
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
    }

    /**
     *  This method formates the file name.
     *
     *  @param baseFileName file name without path
     *  @param counter file counter
     *  @return formatted file name
     **/
    public String createNewFile(String baseFileName, Integer counter) {
        return String.format("%s-%03d.txt", baseFileName.substring(0, baseFileName.lastIndexOf('.')), counter);
    }

}
