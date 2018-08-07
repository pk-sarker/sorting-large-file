# Sorting a large file

## Problem Statement:
Given a very large text file that may not fit in available memory, create a file that contains the distinct words from the original file sorted in the ascending order.

## Proposed Solution:
 * **Step 1:** Slice the big file into smaller files.
 * **Step 2:** Create one thread for each file to process. Use a threadpool with fixed number of threads.
 * **Step 2.1:** Each thread will create new files for the words starting with same letter.( like _t1-a.txt_, _t1-b.txt_ ..)
 * **Step 3:** Then merge each files with same letters ( _t1-a.txt_, _t2-a.txt_, ...) and create a new file ( _a.txt_ ) with all the words starting with same letter.
 * **Step 4:** Sort the words in each file (_a.txt_, _b.txt_ .....) and write to a new file ( _sorted_a.txt_, _sorted_b.txt_, ...)
 * **Step 5:** Merge all the files,(_sorted_a.txt_, sorted_b.txt_ ... )

## Implementation Note
* Implemented wth **JDK 1.8_181**
* No dependencies are added.