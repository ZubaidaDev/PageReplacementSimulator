import java.util.Scanner;
 
/*
 * Operation Systems - Page Replacement Simulator
 * - Zubaida (1095329)
 * - Syeda Namrah (1092716)
 * - Faaria Shoukat (1092486)
 *
 * This program simulates various page replacement algorithms where users can
 * visualize how pages are loaded into memory frames and how page faults occur.
 */
 
public class PageReplacementSimulator {
 
    static final int FRAMES = 3;
    static final int N = 18;
    static final int TAU = 4;
 
    static int[] refString = {0, 4, 1, 4, 2, 4, 3, 4, 2, 4, 0, 4, 1, 4, 2, 2, 3, 1};
 
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice;
 
        do {
            System.out.println("\n------------------------------------------");
            System.out.println(" Virtual Memory Page Replacement Simulator");
            System.out.println("------------------------------------------");
            System.out.println("1. FIFO");
            System.out.println("2. LRU");
            System.out.println("3. Second Chance Page Replacement");
            System.out.println("4. LFU with Dynamic Aging");
            System.out.println("5. MRU");
            System.out.println("6. WSClock");
            System.out.println("-1. Exit");
            System.out.println("------------------------------------------");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
 
            System.out.println();
 
            if (choice == 1) {
                runFIFO();
            } else if (choice == 2) {
                runLRU();
            } else if (choice == 3) {
                runSecondChance();
            } else if (choice == 4) {
                runLFUAging();
            } else if (choice == 5) {
                runMRU();
            } else if (choice == 6) {
                runWSClock();
            } else if (choice == -1) {
                System.out.println("Exiting program...");
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
 
        } while (choice != -1);
 
        sc.close();
    }
 
    /* -------------------- PLACEHOLDER: MEMBER 1 -------------------- */
 
    static void runFIFO() {
        System.out.println("\nFIFO Page Replacement");
        System.out.println("This algorithm will be added by Member 1.");
    }
 
    /* -------------------- Faaria: LRU -------------------- */
 
    static void runLRU() {
        int[] frame = new int[FRAMES];
        int[] lastUsed = new int[FRAMES];
 
        int[][] historyFrame = new int[FRAMES][N];
        String[] historyStatus = new String[N];
 
        int page, found, time = 0, filled = 0, faults = 0, hits = 0, totalOccupiedUnits = 0;
        int victimIndex, oldestTime;
 
        int totalDashes = 13 + (N * 8);
 
        for (int i = 0; i < FRAMES; i++) {
            frame[i] = -1;
            lastUsed[i] = 0;
        }
 
        System.out.println("\nLRU Page Replacement");
        System.out.println("Page that was least recently used is replaced.\n");
 
        for (int i = 0; i < N; i++) {
            page = refString[i];
            found = 0;
            time++;
 
            // Check if requested page is already in memory
            for (int j = 0; j < FRAMES; j++) {
                if (frame[j] == page) {
                    found = 1;
                    lastUsed[j] = time;
                    hits++;
                    historyStatus[i] = "HIT  ";
                    break;
                }
            }
 
            // Page fault
            if (found == 0) {
                faults++;
                historyStatus[i] = "FAULT";
 
                if (filled < FRAMES) {
                    frame[filled] = page;
                    lastUsed[filled] = time;
                    filled++;
                } else {
                    // Find least recently used page
                    victimIndex = 0;
                    oldestTime = lastUsed[0];
 
                    for (int j = 1; j < FRAMES; j++) {
                        if (lastUsed[j] < oldestTime) {
                            oldestTime = lastUsed[j];
                            victimIndex = j;
                        }
                    }
 
                    frame[victimIndex] = page;
                    lastUsed[victimIndex] = time;
                }
            }
 
            totalOccupiedUnits += filled;
 
            for (int j = 0; j < FRAMES; j++) {
                historyFrame[j][i] = frame[j];
            }
        }
 
        // Print layout
        System.out.print("Step:\t\t");
        for (int i = 0; i < N; i++) System.out.print((i + 1) + "\t");
 
        System.out.print("\nRef Page:\t");
        for (int i = 0; i < N; i++) System.out.print(refString[i] + "\t");
 
        System.out.println();
        System.out.println("-".repeat(totalDashes));
 
        for (int j = 0; j < FRAMES; j++) {
            System.out.print("Frame " + (j + 1) + ":\t");
            for (int i = 0; i < N; i++) {
                if (historyFrame[j][i] == -1) System.out.print("[ - ]\t");
                else System.out.print("[ " + historyFrame[j][i] + " ]\t");
            }
            System.out.println();
        }
 
        System.out.println("-".repeat(totalDashes));
 
        System.out.print("Status:\t\t");
        for (int i = 0; i < N; i++) System.out.print(historyStatus[i] + "\t");
 
        printSummary(faults, hits, totalOccupiedUnits);
    }
 
    /* -------------------- Zubaida: SECOND CHANCE (CLOCK) -------------------- */
 
    static void runSecondChance() {
        int[] frame = new int[FRAMES];
        int[] refBit = new int[FRAMES];
 
        int[][] historyFrame = new int[FRAMES][N];
        int[][] historyRefBit = new int[FRAMES][N];
        String[] historyStatus = new String[N];
 
        int page, found, pointer = 0, filled = 0, faults = 0, hits = 0, totalOccupiedUnits = 0;
 
        int totalDashes = 13 + (N * 8);
 
        for (int i = 0; i < FRAMES; i++) {
            frame[i] = -1;
            refBit[i] = 0;
        }
 
        System.out.println("\nSecond Chance Page Replacement");
        System.out.println("New page R=0, hit makes R=1\n");
 
        for (int i = 0; i < N; i++) {
            page = refString[i];
            found = 0;
 
            // Check hit
            for (int j = 0; j < FRAMES; j++) {
                if (frame[j] == page) {
                    found = 1;
                    refBit[j] = 1;
                    hits++;
                    historyStatus[i] = "HIT  ";
                    break;
                }
            }
 
            // Page fault
            if (found == 0) {
                faults++;
                historyStatus[i] = "FAULT";
 
                if (filled < FRAMES) {
                    frame[filled] = page;
                    refBit[filled] = 0;
                    filled++;
                } else {
                    // Second Chance replacement
                    while (refBit[pointer] == 1) {
                        refBit[pointer] = 0;
                        pointer = (pointer + 1) % FRAMES;
                    }
 
                    frame[pointer] = page;
                    refBit[pointer] = 0;
                    pointer = (pointer + 1) % FRAMES;
                }
            }
 
            totalOccupiedUnits += filled;
 
            for (int j = 0; j < FRAMES; j++) {
                historyFrame[j][i] = frame[j];
                historyRefBit[j][i] = refBit[j];
            }
        }
 
        // Print layout
        System.out.print("Step:\t\t");
        for (int i = 0; i < N; i++) System.out.print((i + 1) + "\t");
 
        System.out.print("\nRef Page:\t");
        for (int i = 0; i < N; i++) System.out.print(refString[i] + "\t");
 
        System.out.println();
        System.out.println("-".repeat(totalDashes));
 
        for (int j = 0; j < FRAMES; j++) {
            System.out.print("Frame " + (j + 1) + ":\t");
            for (int i = 0; i < N; i++) {
                if (historyFrame[j][i] == -1) System.out.print("[ - ]\t");
                else System.out.print("[" + historyFrame[j][i] + "|" + historyRefBit[j][i] + "]\t");
            }
            System.out.println();
        }
 
        System.out.println("-".repeat(totalDashes));
 
        System.out.print("Status:\t\t");
        for (int i = 0; i < N; i++) System.out.print(historyStatus[i] + "\t");
 
        printSummary(faults, hits, totalOccupiedUnits);
        System.out.println("\n[P|R] means [Page|Reference Bit]");
    }
 
    /* -------------------- PLACEHOLDER: MEMBER 1 -------------------- */
 
    static void runLFUAging() {
        System.out.println("\nLFU with Dynamic Aging");
        System.out.println("This algorithm will be added by Member 1.");
    }
 
    /* -------------------- Faaria: MRU -------------------- */
 
    static void runMRU() {
        int[] frame = new int[FRAMES];
        int[] lastUsed = new int[FRAMES];
 
        int[][] historyFrame = new int[FRAMES][N];
        String[] historyStatus = new String[N];
 
        int page, found, time = 0, filled = 0, faults = 0, hits = 0, totalOccupiedUnits = 0;
        int victimIndex, newestTime;
 
        int totalDashes = 13 + (N * 8);
 
        for (int i = 0; i < FRAMES; i++) {
            frame[i] = -1;
            lastUsed[i] = 0;
        }
 
        System.out.println("\nMRU Page Replacement");
        System.out.println("Page that was most recently used is replaced.\n");
 
        for (int i = 0; i < N; i++) {
            page = refString[i];
            found = 0;
            time++;
 
            // Check if requested page is already in memory
            for (int j = 0; j < FRAMES; j++) {
                if (frame[j] == page) {
                    found = 1;
                    lastUsed[j] = time;
                    hits++;
                    historyStatus[i] = "HIT  ";
                    break;
                }
            }
 
            // Page fault
            if (found == 0) {
                faults++;
                historyStatus[i] = "FAULT";
 
                if (filled < FRAMES) {
                    frame[filled] = page;
                    lastUsed[filled] = time;
                    filled++;
                } else {
                    // Find most recently used page
                    victimIndex = 0;
                    newestTime = lastUsed[0];
 
                    for (int j = 1; j < FRAMES; j++) {
                        if (lastUsed[j] > newestTime) {
                            newestTime = lastUsed[j];
                            victimIndex = j;
                        }
                    }
 
                    frame[victimIndex] = page;
                    lastUsed[victimIndex] = time;
                }
            }
 
            totalOccupiedUnits += filled;
 
            for (int j = 0; j < FRAMES; j++) {
                historyFrame[j][i] = frame[j];
            }
        }
 
        // Print layout
        System.out.print("Step:\t\t");
        for (int i = 0; i < N; i++) System.out.print((i + 1) + "\t");
 
        System.out.print("\nRef Page:\t");
        for (int i = 0; i < N; i++) System.out.print(refString[i] + "\t");
 
        System.out.println();
        System.out.println("-".repeat(totalDashes));
 
        for (int j = 0; j < FRAMES; j++) {
            System.out.print("Frame " + (j + 1) + ":\t");
            for (int i = 0; i < N; i++) {
                if (historyFrame[j][i] == -1) System.out.print("[ - ]\t");
                else System.out.print("[ " + historyFrame[j][i] + " ]\t");
            }
            System.out.println();
        }
 
        System.out.println("-".repeat(totalDashes));
 
        System.out.print("Status:\t\t");
        for (int i = 0; i < N; i++) System.out.print(historyStatus[i] + "\t");
 
        printSummary(faults, hits, totalOccupiedUnits);
    }
 
    /* -------------------- Zubaida: WSCLOCK (Working Set Clock) -------------------- */
 
    static void runWSClock() {
        char[] operation = {'R','W','R','R','W','R','R','W','R','R','W','R','R','W','R','R','W','R'};
 
        int[] frame = new int[FRAMES];
        int[] refBit = new int[FRAMES];
        int[] dirtyBit = new int[FRAMES];
        int[] lastUsed = new int[FRAMES];
 
        int[][] historyFrame = new int[FRAMES][N];
        int[][] historyRefBit = new int[FRAMES][N];
        int[][] historyDirtyBit = new int[FRAMES][N];
        String[] historyStatus = new String[N];
 
        int page, found, pointer = 0, filled = 0, faults = 0, hits = 0;
        int writeBacks = 0, time = 0, totalOccupiedUnits = 0;
        int age, replaced, scanned, oldestIndex, oldestAge;
 
        int totalDashes = 15 + (N * 8);
 
        for (int i = 0; i < FRAMES; i++) {
            frame[i] = -1;
            refBit[i] = 0;
            dirtyBit[i] = 0;
            lastUsed[i] = 0;
        }
 
        System.out.println("\nWSClock Page Replacement");
        System.out.println("TAU = " + TAU + ", requested page R=1, W operation makes M=1\n");
 
        for (int i = 0; i < N; i++) {
            page = refString[i];
            found = 0;
            time++;
 
            // Check hit
            for (int j = 0; j < FRAMES; j++) {
                if (frame[j] == page) {
                    found = 1;
                    refBit[j] = 1;
                    lastUsed[j] = time;
 
                    if (operation[i] == 'W') dirtyBit[j] = 1;
 
                    hits++;
                    historyStatus[i] = "HIT  ";
                    break;
                }
            }
 
            // Page fault
            if (found == 0) {
                faults++;
                historyStatus[i] = "FAULT";
 
                if (filled < FRAMES) {
                    frame[filled] = page;
                    refBit[filled] = 1;
                    dirtyBit[filled] = (operation[i] == 'W') ? 1 : 0;
                    lastUsed[filled] = time;
                    filled++;
                } else {
                    // WSClock replacement
                    replaced = 0;
                    scanned = 0;
                    oldestIndex = pointer;
                    oldestAge = -1;
 
                    while (replaced == 0 && scanned < FRAMES * 2) {
                        age = time - lastUsed[pointer];
 
                        if (age > oldestAge) {
                            oldestAge = age;
                            oldestIndex = pointer;
                        }
 
                        if (refBit[pointer] == 1) {
                            refBit[pointer] = 0;
                            lastUsed[pointer] = time;
                            pointer = (pointer + 1) % FRAMES;
                        } else {
                            if (age <= TAU) {
                                pointer = (pointer + 1) % FRAMES;
                            } else {
                                if (dirtyBit[pointer] == 1) {
                                    dirtyBit[pointer] = 0;
                                    writeBacks++;
                                    pointer = (pointer + 1) % FRAMES;
                                } else {
                                    frame[pointer] = page;
                                    refBit[pointer] = 1;
                                    dirtyBit[pointer] = (operation[i] == 'W') ? 1 : 0;
                                    lastUsed[pointer] = time;
                                    pointer = (pointer + 1) % FRAMES;
                                    replaced = 1;
                                }
                            }
                        }
 
                        scanned++;
                    }
 
                    // If no perfect victim found, replace the oldest page
                    if (replaced == 0) {
                        pointer = oldestIndex;
 
                        if (dirtyBit[pointer] == 1) writeBacks++;
 
                        frame[pointer] = page;
                        refBit[pointer] = 1;
                        dirtyBit[pointer] = (operation[i] == 'W') ? 1 : 0;
                        lastUsed[pointer] = time;
                        pointer = (pointer + 1) % FRAMES;
                    }
                }
            }
 
            totalOccupiedUnits += filled;
 
            for (int j = 0; j < FRAMES; j++) {
                historyFrame[j][i] = frame[j];
                historyRefBit[j][i] = refBit[j];
                historyDirtyBit[j][i] = dirtyBit[j];
            }
        }
 
        // Print layout
        System.out.print("Step:\t\t");
        for (int i = 0; i < N; i++) System.out.print((i + 1) + "\t");
 
        System.out.print("\nOperation:\t");
        for (int i = 0; i < N; i++) System.out.print(operation[i] + "\t");
 
        System.out.print("\nRef Page:\t");
        for (int i = 0; i < N; i++) System.out.print(refString[i] + "\t");
 
        System.out.println();
        System.out.println("-".repeat(totalDashes));
 
        for (int j = 0; j < FRAMES; j++) {
            System.out.print("Frame " + (j + 1) + ":\t");
            for (int i = 0; i < N; i++) {
                if (historyFrame[j][i] == -1) System.out.print("[ - ]\t");
                else System.out.print("[" + historyFrame[j][i] + "|" + historyRefBit[j][i] + "|" + historyDirtyBit[j][i] + "]\t");
            }
            System.out.println();
        }
 
        System.out.println("-".repeat(totalDashes));
 
        System.out.print("Status:\t\t");
        for (int i = 0; i < N; i++) System.out.print(historyStatus[i] + "\t");
 
        System.out.println("\n\n---------------------------------- Summary Table ----------------------------------");
        System.out.println("Faults\tHits\tFault Rate\tHit Rate\tMemory Utilization\tWrite Backs");
        System.out.printf("%d\t%d\t%.2f%%\t\t%.2f%%\t\t%.2f%%\t\t\t%d%n",
                faults, hits,
                ((float) faults / N) * 100,
                ((float) hits / N) * 100,
                ((float) totalOccupiedUnits / (N * FRAMES)) * 100,
                writeBacks);
        System.out.println("-----------------------------------------------------------------------------------");
 
        System.out.println("\n[P|R|M] means [Page|Reference Bit|Modified Bit]");
        System.out.println("Operation: R = Read, W = Write | TAU = " + TAU + " (Pages older than " + TAU + " units are outside working set)");
    }
 
    // For printing summary table except WSClock which has its own summary with write back count
    static void printSummary(int faults, int hits, int totalOccupiedUnits) {
        System.out.println("\n\n------------------------- Summary Table --------------------------");
        System.out.println("Faults\tHits\tFault Rate\tHit Rate\tMemory Utilization");
        System.out.printf("%d\t%d\t%.2f%%\t\t%.2f%%\t\t%.2f%%%n",
                faults, hits,
                ((float) faults / N) * 100,
                ((float) hits / N) * 100,
                ((float) totalOccupiedUnits / (N * FRAMES)) * 100);
        System.out.println("------------------------------------------------------------------");
    }
}
 
