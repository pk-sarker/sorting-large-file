package pijus.java.exercises;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.NoSuchFileException;
import java.nio.file.DirectoryNotEmptyException;


public class FreeDiskMemory {

    public static void deleteFile(String filePath) {
        try {
            Path fileToDeletePath = Paths.get(filePath);
            Files.delete(fileToDeletePath);
        } catch (NoSuchFileException x) {
            System.err.format("%s: no such" + " file or directory%n", filePath);
        } catch (DirectoryNotEmptyException x) {
            System.err.format("%s not empty%n", filePath);
        } catch (IOException x) {
            // File permission problems are caught here.
            System.err.println(x);
        }
    }
}
