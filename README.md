# Page Replacement Simulator

A virtual memory page replacement simulator that demonstrates how pages are loaded into memory frames and how page faults and hits occur.

This project includes two versions:

* C console version
* Java Swing GUI version

## Features

The simulator supports the following page replacement algorithms:

* FIFO
* LRU
* Second Chance
* LFU with Dynamic Aging
* MRU
* WSClock

## Project Files

* `page_replacement_simulator.c` - C console version
* `PageReplacementSimulator.java` - Java Swing GUI version
* `README.md` - Project documentation

## Description

This project simulates different virtual memory page replacement algorithms. It helps users understand how pages are stored in memory frames and how the system handles page hits and page faults.

The C version runs in the console and displays the output in text format.
The Java version uses a Swing GUI to display the results in a more visual and user-friendly way.

## Algorithms Included

### FIFO

FIFO stands for First In, First Out.
The oldest page in memory is replaced first when a page fault occurs.

### LRU

LRU stands for Least Recently Used.
The page that has not been used for the longest time is replaced.

### Second Chance

Second Chance uses a reference bit.
If a page has been recently used, it gets a second chance before replacement.

### LFU with Dynamic Aging

LFU stands for Least Frequently Used.
The page with the lowest frequency score is replaced. Dynamic aging helps reduce the effect of old page usage over time.

### MRU

MRU stands for Most Recently Used.
The page that was most recently used is replaced when a page fault occurs.

### WSClock

WSClock stands for Working Set Clock.
It uses reference bits, modified bits, page age, and write backs to decide which page should be replaced.

## How to Run the C Version

Compile the C program:

```bash
gcc page_replacement_simulator.c -o simulator
```

Run the program:

```bash
./simulator
```

On Windows, run:

```bash
simulator.exe
```

## How to Run the Java GUI Version

Make sure the Java file name is exactly:

```text
PageReplacementSimulator.java
```

Compile the Java program:

```bash
javac PageReplacementSimulator.java
```

Run the Java program:

```bash
java PageReplacementSimulator
```

## How to Run in Eclipse

1. Open Eclipse.
2. Create a new Java Project.
3. Create a new class named `PageReplacementSimulator`.
4. Paste the Java code into the class file.
5. Save the file.
6. Right-click the file.
7. Select `Run As > Java Application`.

## Java GUI Version

The Java version provides a graphical interface using Swing.

It includes:

* Setup window
* Algorithm menu
* Result table
* Page fault and hit summary
* Fault rate and hit rate
* Memory utilization
* Write backs for WSClock
* Fault vs hit chart

## Sample Default Input

Default number of frames:

```text
3
```

Default reference string:

```text
0, 4, 1, 4, 2, 4, 3, 4, 2, 4, 0, 4, 1, 4, 2, 2, 3, 1
```

Default WSClock operations:

```text
R, W, R, R, W, R, R, W, R, R, W, R, R, W, R, R, W, R
```

## Output Summary

The simulator displays:

* Frame contents at each step
* Hit or fault status
* Total page faults
* Total hits
* Fault rate
* Hit rate
* Memory utilization
* Write backs for WSClock

## Purpose

The purpose of this project is to understand and compare different page replacement algorithms used in operating systems. It shows how each algorithm makes a replacement decision when memory frames are full.
