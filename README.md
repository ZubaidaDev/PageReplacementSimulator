# Page Replacement Simulator

A virtual memory page replacement simulator that demonstrates how pages are loaded into memory frames and how page faults and hits occur.

The project includes both a C console version and a Java Swing GUI version.

## Features

- FIFO
- LRU
- Second Chance
- LFU with Dynamic Aging
- MRU
- WSClock

## Files

- `page_replacement_simulator.c` - C console version
- `PageReplacementSimulator.java` - Java Swing GUI version

## How to Run the C Version

Compile the program:

```bash
gcc page_replacement_simulator.c -o simulator
