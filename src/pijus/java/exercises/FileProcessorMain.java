package pijus.java.exercises;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public class FileProcessorMain {

    public static void main(String args[]) throws IOException {
        System.out.println("Memory Use: " + (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024);

        // Thread t1 = new Thread(new FileSplitter(), "T-1");
        // Thread t2 = new Thread(new TextProcessor(), "T-2");

        // t1.start();
        // t2.start();
//        FileSplitter fls = new FileSplitter();
//        fls.split("t1");
        LineReader reader = new LineReader(1024);
        reader.ScanByLine("input/test.txt");
        Set<String> splitedFiles = reader.getSplitedFiles();

        Iterator<String> randomItr = splitedFiles.iterator();

        LineProcessor lineProcessor = new LineProcessor("input/test.txt", "t1");
        Thread t1 = new Thread(lineProcessor, "T-1");
        t1.start();

        int fileCount = 0;
        while(randomItr.hasNext()) {
            //lineProcessor.process("input/"+randomItr.next(), "t"+ Integer.toString(fileCount++));
            (new Thread(new LineProcessor("input/" + randomItr.next(), "t" + Integer.toString(fileCount++)), "T-"+Integer.toString(fileCount))).start();
        }

        //lineProcessor.process("input/test-000.txt", "t1");

//        splitedFiles.forEach(new Consumer<String>() {
//            @Override
//            public void accept(String s) {
//                System.out.println("--> File: "+s);
//                LineProcessor lineProcessor = new LineProcessor();
//                //lineProcessor.process("input/"+s, "t1");
//                try {
//                    testFunc(lineProcessor);
//                } catch (IOException ix) {
//
//                }
//            }
//
//            public void testFunc(LineProcessor lineProcessor, String s) throws IOException {
//                lineProcessor.process("input/"+s, "t1");
//            }
//        });
//        splitedFiles.forEachRemaining(new Consumer<Integer>() {
//            public void accept(Integer t) {
//                System.out.println("forEach anonymous class Value::"+t);
//            }
//        });
        // splitedFiles.
        // displayStateAndIsAlive(t1);
        // displayStateAndIsAlive(t2);

        System.out.println("Memory Use: " + (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024);
    }

    public static void displayStateAndIsAlive(Thread thread) {
        // java.lang.Thread.State can be NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
        System.out.println(">> " + thread.getName() + " >> State: " + thread.getState());
        System.out.println(">> " + thread.getName() + " >> Is alive?: " + thread.isAlive());
    }
}
