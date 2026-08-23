# Page Replacement Simulator

A virtual memory simulator for comparing page replacement algorithms against the same reference workload.

The project is centered on a **C console implementation** that shows page-by-page memory state, page faults, hits, and replacement decisions. A **Java Swing GUI** was added later as a companion visualization of the same algorithm family.

## Algorithms

| Algorithm      | Replacement Strategy                                                  |
| -------------- | --------------------------------------------------------------------- |
| FIFO           | Replaces the page that entered memory first                           |
| LRU            | Replaces the least recently used page                                 |
| Second Chance  | Extends FIFO with a reference bit                                     |
| LFU with Aging | Uses access frequency with periodic aging                             |
| MRU            | Replaces the most recently used page                                  |
| WSClock        | Uses reference, modified, and age information with a clock-style scan |

## Features

* Six page replacement algorithms
* Configurable frame count and reference string
* Optional read/write operations for WSClock
* Per-step frame state visualization
* Page fault and hit tracking
* Fault and hit rate calculation
* Average frame occupancy reporting
* Reference-bit tracking for Second Chance
* Reference and modified-bit tracking for WSClock
* WSClock write-back counting

## Implementations

### C Console Simulator

`page_replacement_simulator.c` is the primary implementation in this repository.

It supports up to:

* 10 memory frames
* 50 page references

The simulator displays frame contents after each page request together with the hit or fault status and a final summary.

The C WSClock implementation uses a fixed working-set threshold of `TAU = 4`.

### Java Swing GUI

`PageReplacementSimulator.java` is a later graphical implementation providing:

* interactive configuration
* algorithm selection
* result tables
* hit/fault statistics
* frame-state visualization
* fault-versus-hit chart
* configurable WSClock `TAU`

The Java version is a companion implementation rather than an exact port of the C version.

## Build and Run

### C

Compile with GCC or Clang:

```bash
gcc -std=c11 -Wall -Wextra page_replacement_simulator.c -o simulator
```

Run on Linux or macOS:

```bash
./simulator
```

Run on Windows:

```bash
simulator.exe
```

### Java

Compile:

```bash
javac PageReplacementSimulator.java
```

Run:

```bash
java PageReplacementSimulator
```

Java 8 or later is recommended.

## Default Workload

**Frames:** `3`

**Reference string:**

```text
0, 4, 1, 4, 2, 4, 3, 4, 2, 4, 0, 4, 1, 4, 2, 2, 3, 1
```

**WSClock operations:**

```text
R, W, R, R, W, R, R, W, R, R, W, R, R, W, R, R, W, R
```

For custom input, use:

* 1 to 10 frames
* 1 to 50 page references
* non-negative page numbers
* `R` or `W` operations for WSClock

## Output

Each simulation reports:

* frame contents after every page reference
* page hit or fault at each step
* total page faults
* total hits
* fault rate
* hit rate
* average frame occupancy
* WSClock write-backs when applicable

Second Chance displays frame entries as:

```text
[Page|Reference Bit]
```

WSClock displays:

```text
[Page|Reference Bit|Modified Bit]
```

## Project Structure

```text
PageReplacementSimulator/
├── page_replacement_simulator.c   # Primary C implementation
├── PageReplacementSimulator.java  # Java Swing companion GUI
└── README.md
```

## Scope

This project is designed to demonstrate and compare page replacement behavior under controlled workloads.

It models page replacement decision logic for educational and experimental purposes rather than implementing a complete operating-system virtual memory subsystem.
