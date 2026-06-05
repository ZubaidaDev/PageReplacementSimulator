/*Operation Systems - Page Replacement Simulator

This program simulates various page replacement algorithms where users can visualize how pages are loaded into memory frames 
and how page faults occur.*/

#include <stdio.h>

#define FRAMES 3 // no. of available page frames
#define N 18 // no. of page ref
#define TAU 4 // aging time/working set age threshold for wsclock

int refString[N] = {1, 2, 3, 1, 2, 1, 4, 1, 2, 5, 1, 2, 3, 4, 5, 1, 2, 1};

void runFIFO();
void runLRU();
void runSecondChance();
void runLFUAging();
void runMRU();
void runWSClock();
void printSummary(int faults, int hits, int totalFilledFrames);

int main() {
	int choice;

	do {
		printf("\n------------------------------------------\n");
		printf(" Virtual Memory Page Replacement Simulator\n");
		printf("------------------------------------------\n");
		printf("1. FIFO\n");
		printf("2. LRU\n");
		printf("3. Second Chance Page Replacement\n");
		printf("4. LFU with Dynamic Aging\n");
		printf("5. MRU\n");
		printf("6. WSClock\n");
		printf("-1. Exit\n");
		printf("------------------------------------------\n");
		printf("Enter your choice: ");
		scanf("%d", &choice);

		printf("\n");

		if (choice == 1) {
			runFIFO();
		}
		else if (choice == 2) {
			runLRU();
		}
		else if (choice == 3) {
			runSecondChance();
		}
		else if (choice == 4) {
			runLFUAging();
		}
		else if (choice == 5) {
			runMRU();
		}
		else if (choice == 6) {
			runWSClock();
		}
		else if (choice == -1) {
			printf("Exiting program...\n");
		}
		else {
			printf("Invalid choice. Please try again.\n");
		}

	} while (choice != -1);

	return 0;
}

/* -------------------- FIFO (First In, First Out) Algorithm -------------------- */

void runFIFO() {
	int frame[FRAMES];
	int frameStore[FRAMES][N];
	char* statusStore[N];

	int i, j;
	int page;
	int found;
	int pointer = 0;
	int filled = 0;
	int faults = 0;
	int hits = 0;
	int totalFilledFrames = 0;

	int totalDashes = 13 + (N * 8);

	for (i = 0; i < FRAMES; i++) {
		frame[i] = -1;
	}

	printf("\nFIFO Page Replacement\n");
	printf("Oldest page is replaced first.\n\n");

	for (i = 0; i < N; i++) {
		page = refString[i];
		found = 0;

		// Check hit
		for (j = 0; j < FRAMES; j++) {
			if (frame[j] == page) {
				found = 1;
				hits++;
				statusStore[i] = "HIT  ";
				break;
			}
		}

		// Page fault
		if (found == 0) {
			faults++;
			statusStore[i] = "FAULT";

			// If empty frame exists, insert without replacement
			if (filled < FRAMES) {
				frame[filled] = page;
				filled++;
			}
			else {
				// FIFO replacement
				frame[pointer] = page;
				pointer = (pointer + 1) % FRAMES;
			}
		}

		totalFilledFrames += filled;

		// store current state values into history matrix to print later
		for (j = 0; j < FRAMES; j++) {
			frameStore[j][i] = frame[j];
		}
	}

	//---print layout---
	printf("Step:      ");
	for (i = 0; i < N; i++) {
		printf("%-6d", i + 1);
	}

	printf("\nRef Page:  ");
	for (i = 0; i < N; i++) {
		printf("%-6d", refString[i]);
	}

	printf("\n");
	for (i = 0; i < totalDashes; i++) {
		printf("-");
	}
	printf("\n");

	for (j = 0; j < FRAMES; j++) {
		printf("Frame %d:\t", j + 1);

		for (i = 0; i < N; i++) {
			if (frameStore[j][i] == -1) {
				printf("[ - ] ");
			}
			else {
				printf("[ %d ] ", frameStore[j][i]);
			}
		}

		printf("\n");
	}

	for (i = 0; i < totalDashes; i++) {
		printf("-");
	}
	printf("\n");

	printf("Status:\t\t");
	for (i = 0; i < N; i++) {
		printf("%-7s", statusStore[i]);
	}

	printSummary(faults, hits, totalFilledFrames);
}

/* -------------------- LRU (Least Recently Used) Algorithm -------------------- */

void runLRU() {
    int frame[FRAMES];
    int lastUsed[FRAMES];

    int historyFrame[FRAMES][N];
    char* historyStatus[N];

    int i, j;
    int page;
    int found;
    int time = 0;
    int filled = 0;
    int faults = 0;
    int hits = 0;
    int totalOccupiedUnits = 0;

    int victimIndex;
    int oldestTime;

    int totalDashes = 13 + (N * 8);

    for (i = 0; i < FRAMES; i++) {
        frame[i] = -1;
        lastUsed[i] = 0;
    }

    printf("\nLRU Page Replacement\n");
    printf("Page that was least recently used is replaced.\n\n");

    for (i = 0; i < N; i++) {
        page = refString[i];
        found = 0;
        time++;

        // Check if requested page is already in memory
        for (j = 0; j < FRAMES; j++) {
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

            // If empty frame exists, insert page directly
            if (filled < FRAMES) {
                frame[filled] = page;
                lastUsed[filled] = time;
                filled++;
            }
            else {
                // Find the least recently used page
                victimIndex = 0;
                oldestTime = lastUsed[0];

                for (j = 1; j < FRAMES; j++) {
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

        // Capture current memory state into history matrix
        for (j = 0; j < FRAMES; j++) {
            historyFrame[j][i] = frame[j];
        }
    }

    //---Print layout---
    printf("Step:\t\t");
    for (i = 0; i < N; i++) {
        printf("%d\t", i + 1);
    }

    printf("\nRef Page:\t");
    for (i = 0; i < N; i++) {
        printf("%d\t", refString[i]);
    }

    printf("\n");
    for (i = 0; i < totalDashes; i++) {
        printf("-");
    }
    printf("\n");

    for (j = 0; j < FRAMES; j++) {
        printf("Frame %d:\t", j + 1);

        for (i = 0; i < N; i++) {
            if (historyFrame[j][i] == -1) {
                printf("[ - ]\t");
            }
            else {
                printf("[ %d ]\t", historyFrame[j][i]);
            }
        }

        printf("\n");
    }

    for (i = 0; i < totalDashes; i++) {
        printf("-");
    }
    printf("\n");

    printf("Status:\t\t");
    for (i = 0; i < N; i++) {
        printf("%s\t", historyStatus[i]);
    }

    printSummary(faults, hits, totalOccupiedUnits);
}

/* -------------------- Second Chance (Clock) Algorithm -------------------- */

void runSecondChance() {
	int frame[FRAMES];
	int rBit[FRAMES];

	int frameStore[FRAMES][N];
	int rBitStore[FRAMES][N];
	char* statusHistory[N];

	int i, j;
	int page;
	int found;
	int pointer = 0;
	int filled = 0;
	int faults = 0;
	int hits = 0;
	int totalFilledFrames = 0; // counts total filled frame slots needed for memory utilization calc

	int totalDashes = 13 + (N * 8);

	for (i = 0; i < FRAMES; i++) {
		frame[i] = -1;
		rBit[i] = 0;
	}

	printf("\nSecond Chance Page Replacement\n");
	printf("New page R=0, hit makes R=1\n\n");

	for (i = 0; i < N; i++) {
		page = refString[i];
		found = 0;

		// Check hit
		for (j = 0; j < FRAMES; j++) {
			if (frame[j] == page) {
				found = 1;
				rBit[j] = 1;
				hits++;
				statusHistory[i] = "HIT  ";
				break;
			}
		}

		// Page fault
		if (found == 0) {
			faults++;
			statusHistory[i] = "FAULT";

			// If empty frame exists, insert without replacement
			if (filled < FRAMES) {
				frame[filled] = page;
				rBit[filled] = 0;
				filled++;
			}
			else {
				// Second Chance replacement
				while (rBit[pointer] == 1) {
					rBit[pointer] = 0;
					pointer = (pointer + 1) % FRAMES;
				}

				frame[pointer] = page;
				rBit[pointer] = 0;

				pointer = (pointer + 1) % FRAMES;
			}
		}

		totalFilledFrames += filled;
		// store current state values into history matrix to print later
		for (j = 0; j < FRAMES; j++) {
			frameStore[j][i] = frame[j];
			rBitStore[j][i] = rBit[j];
		}
	}

	//---print layout---
	printf("Step:\t\t");
	for (i = 0; i < N; i++) {
		printf("%d\t", i + 1);
	}

	printf("\nRef Page:\t");
	for (i = 0; i < N; i++) {
		printf("%d\t", refString[i]);
	}

	printf("\n");
	for (i = 0; i < totalDashes; i++) {
		printf("-");
	}
	printf("\n");

	for (j = 0; j < FRAMES; j++) {
		printf("Frame %d:\t", j + 1);

		for (i = 0; i < N; i++) {
			if (frameStore[j][i] == -1) {
				printf("[ - ]\t");
			}
			else {
				printf("[%d|%d]\t", frameStore[j][i], rBitStore[j][i]);
			}
		}

		printf("\n");
	}

	for (i = 0; i < totalDashes; i++) {
		printf("-");
	}
	printf("\n");

	printf("Status:\t\t");
	for (i = 0; i < N; i++) {
		printf("%s\t", statusHistory[i]);
	}

	printSummary(faults, hits, totalFilledFrames);
	printf("\n[P|R] means [Page|Reference Bit]\n");
}

/* -------------------- LFU with Dynamic Aging (Least Frequently Used with Dynamic Aging) Algorithm -------------------- */

void runLFUAging() {
	int frame[FRAMES];
	int freq[FRAMES];
	int age[FRAMES];

	int frameStore[FRAMES][N];
	int freqStore[FRAMES][N];
	char* statusStore[N];

	int i, j;
	int page;
	int found;
	int filled = 0;
	int faults = 0;
	int hits = 0;
	int totalFilledFrames = 0;

	int victim;
	int minScore;
	int score;

	int totalDashes = 13 + (N * 8);

	for (i = 0; i < FRAMES; i++) {
		frame[i] = -1;
		freq[i] = 0;
		age[i] = 0;
	}

	printf("\nLFU with Dynamic Aging Page Replacement\n");
	printf("Page with lowest frequency score is replaced.\n\n");

	for (i = 0; i < N; i++) {
		page = refString[i];
		found = 0;

		// Check hit
		for (j = 0; j < FRAMES; j++) {
			if (frame[j] == page) {
				found = 1;
				freq[j]++;
				hits++;
				statusStore[i] = "HIT  ";
				break;
			}
		}

		// Page fault
		if (found == 0) {
			faults++;
			statusStore[i] = "FAULT";

			// If empty frame exists, insert without replacement
			if (filled < FRAMES) {
				frame[filled] = page;
				freq[filled] = 1;
				age[filled] = i;
				filled++;
			}
			else {
				// LFU with Dynamic Aging replacement
				victim = 0;
				minScore = freq[0] + age[0];

				for (j = 1; j < FRAMES; j++) {
					score = freq[j] + age[j];

					if (score < minScore) {
						minScore = score;
						victim = j;
					}
				}

				frame[victim] = page;
				freq[victim] = 1;
				age[victim] = i;
			}
		}

		// Dynamic aging step: apply aging every 3 page requests
		if ((i + 1) % 3 == 0) {
			for (j = 0; j < filled; j++) {
				if (freq[j] > 1) {
					freq[j] = freq[j] / 2;
				}
			}
		}

		totalFilledFrames += filled;

		// store current state values into history matrix to print later
		for (j = 0; j < FRAMES; j++) {
			frameStore[j][i] = frame[j];
			freqStore[j][i] = freq[j];
		}
	}

	//---print layout---
	printf("Step:\t\t");
	for (i = 0; i < N; i++) {
		printf("%d\t", i + 1);
	}

	printf("\nRef Page:\t");
	for (i = 0; i < N; i++) {
		printf("%d\t", refString[i]);
	}

	printf("\n");
	for (i = 0; i < totalDashes; i++) {
		printf("-");
	}
	printf("\n");

	for (j = 0; j < FRAMES; j++) {
		printf("Frame %d:\t", j + 1);

		for (i = 0; i < N; i++) {
			if (frameStore[j][i] == -1) {
				printf("[ - ]\t");
			}
			else {
				printf("[%d|%d] ", frameStore[j][i], freqStore[j][i]);
			}
		}

		printf("\n");
	}

	for (i = 0; i < totalDashes; i++) {
		printf("-");
	}
	printf("\n");

	printf("Status:\t\t");
	for (i = 0; i < N; i++) {
		printf("%s\t", statusStore[i]);
	}

	printSummary(faults, hits, totalFilledFrames);
	printf("\n[P|F] means [Page|Frequency]\n");
}

/* -------------------- MRU (Most Recently Used) Algorithm -------------------- */

void runMRU() {
    int frame[FRAMES];
    int lastUsed[FRAMES];

    int historyFrame[FRAMES][N];
    char* historyStatus[N];

    int i, j;
    int page;
    int found;
    int time = 0;
    int filled = 0;
    int faults = 0;
    int hits = 0;
    int totalOccupiedUnits = 0;

    int victimIndex;
    int newestTime;

    int totalDashes = 13 + (N * 8);

    for (i = 0; i < FRAMES; i++) {
        frame[i] = -1;
        lastUsed[i] = 0;
    }

    printf("\nMRU Page Replacement\n");
    printf("Page that was most recently used is replaced.\n\n");

    for (i = 0; i < N; i++) {
        page = refString[i];
        found = 0;
        time++;

        // Check if requested page is already in memory
        for (j = 0; j < FRAMES; j++) {
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

            // If empty frame exists, insert page directly
            if (filled < FRAMES) {
                frame[filled] = page;
                lastUsed[filled] = time;
                filled++;
            }
            else {
                // Find the most recently used page
                victimIndex = 0;
                newestTime = lastUsed[0];

                for (j = 1; j < FRAMES; j++) {
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

        // Capture current memory state into history matrix
        for (j = 0; j < FRAMES; j++) {
            historyFrame[j][i] = frame[j];
        }
    }

    //---Print layout---
    printf("Step:\t\t");
    for (i = 0; i < N; i++) {
        printf("%d\t", i + 1);
    }

    printf("\nRef Page:\t");
    for (i = 0; i < N; i++) {
        printf("%d\t", refString[i]);
    }

    printf("\n");
    for (i = 0; i < totalDashes; i++) {
        printf("-");
    }
    printf("\n");

    for (j = 0; j < FRAMES; j++) {
        printf("Frame %d:\t", j + 1);

        for (i = 0; i < N; i++) {
            if (historyFrame[j][i] == -1) {
                printf("[ - ]\t");
            }
            else {
                printf("[ %d ]\t", historyFrame[j][i]);
            }
        }

        printf("\n");
    }

    for (i = 0; i < totalDashes; i++) {
        printf("-");
    }
    printf("\n");

    printf("Status:\t\t");
    for (i = 0; i < N; i++) {
        printf("%s\t", historyStatus[i]);
    }

    printSummary(faults, hits, totalOccupiedUnits);
}

/* -------------------- WSCLOCK (Working Set Clock) Algorithm-------------------- */

void runWSClock() {
	// R = read, W = write
	char op[N] = {'R', 'W', 'R', 'R', 'W', 'R', 'R', 'W', 'R', 'R', 'W', 'R', 'R', 'W', 'R', 'R', 'W', 'R'};

	int frame[FRAMES];
	int rBit[FRAMES];
	int mBit[FRAMES];
	int lastUsed[FRAMES];

	int frameStore[FRAMES][N];
	int rBitStore[FRAMES][N];
	int mBitStore[FRAMES][N];
	char* statusStore[N];

	int i, j;
	int page;
	int found;
	int pointer = 0;
	int filled = 0;
	int faults = 0;
	int hits = 0;
	int writeBacks = 0;
	int time = 0;
	int totalFilledFrames = 0; // counts total filled frame slots needed for memory utilization calc

	int age;
	int replaced;
	int scanned;
	int oldestIndex;
	int oldestAge;

	int totalDashes = 15 + (N * 8);

	for (i = 0; i < FRAMES; i++) {
		frame[i] = -1;
		rBit[i] = 0;
		mBit[i] = 0;
		lastUsed[i] = 0;
	}

	printf("\nWSClock Page Replacement\n");
	printf("TAU = %d, requested page R=1, W operation makes M=1\n\n", TAU);

	for (i = 0; i < N; i++) {
		page = refString[i];
		found = 0;
		time++;

		// Check hit
		for (j = 0; j < FRAMES; j++) {
			if (frame[j] == page) {
				found = 1;
				rBit[j] = 1;
				lastUsed[j] = time;

				if (op[i] == 'W') {//if page hit is 'w' update. no change in mBit for hit when op is read but previous same page was write
					mBit[j] = 1;
				}

				hits++;
				statusStore[i] = "HIT  ";
				break;
			}
		}

		// Page fault
		if (found == 0) {
			faults++;
			statusStore[i] = "FAULT";

			// If empty frame exists, insert without replacement
			if (filled < FRAMES) {
				frame[filled] = page;
				rBit[filled] = 1;

				if (op[i] == 'W') {
					mBit[filled] = 1;
				}
				else {
					mBit[filled] = 0;
				}

				lastUsed[filled] = time;
				filled++;
			}
			else {
				//wsclock replacement algo
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

					if (rBit[pointer] == 1) { //if ref bit == 1, give second chance
						rBit[pointer] = 0;
						pointer = (pointer + 1) % FRAMES;
					}
					else { //else if R-bit == 0, check age/working set condition
						if (age <= TAU) { //if page in working set, give second chance
							pointer = (pointer + 1) % FRAMES;
						}
						else {//else if page not in working set (age>TAU), check dirty/modified bit
							if (mBit[pointer] == 1) { //if dirty (M-bit == 1), write back, give second chance
								mBit[pointer] = 0;
								writeBacks++;
								pointer = (pointer + 1) % FRAMES;
							}
							else { //else if clean (M-bit == 0), replace
								frame[pointer] = page;
								rBit[pointer] = 1;

								if (op[i] == 'W') { //if new page is write, set dirty/m bit = 1
									mBit[pointer] = 1;
								}
								else {
									mBit[pointer] = 0; //if new page is read, set dirty bit = 0
								}

								lastUsed[pointer] = time; //update last used time for new page
								pointer = (pointer + 1) % FRAMES;
								replaced = 1;//replacement done, exit loop
							}
						}
					}

					scanned++;//increment scanned count to prevent infinite loop if all pages are in working set
				}

				// If no perfect victim found, replace oldest page
				if (replaced == 0) {
					pointer = oldestIndex;

					if (mBit[pointer] == 1) {
						writeBacks++;
					}

					frame[pointer] = page;
					rBit[pointer] = 1;

					if (op[i] == 'W') {
						mBit[pointer] = 1;
					}
					else {
						mBit[pointer] = 0;
					}

					lastUsed[pointer] = time;
					pointer = (pointer + 1) % FRAMES;
				}
			}
		}
		totalFilledFrames += filled;

		// Capture current state values into the history matrix
		for (j = 0; j < FRAMES; j++) {
			frameStore[j][i] = frame[j];
			rBitStore[j][i] = rBit[j];
			mBitStore[j][i] = mBit[j];
		}
	}

	//---Print layout---
	printf("Step:\t\t");
	for (i = 0; i < N; i++) {
		printf("%d\t", i + 1);
	}

	printf("\nOperation:\t");
	for (i = 0; i < N; i++) {
		printf("%c\t", op[i]);
	}

	printf("\nRef Page:\t");
	for (i = 0; i < N; i++) {
		printf("%d\t", refString[i]);
	}

	printf("\n");
	for (i = 0; i < totalDashes; i++) {
		printf("-");
	}
	printf("\n");

	for (j = 0; j < FRAMES; j++) {
		printf("Frame %d:\t", j + 1);

		for (i = 0; i < N; i++) {
			if (frameStore[j][i] == -1) {
				printf("[ - ]\t");
			}
			else {
				printf("[%d|%d|%d]\t", frameStore[j][i], rBitStore[j][i], mBitStore[j][i]);
			}
		}

		printf("\n");
	}

	for (i = 0; i < totalDashes; i++) {
		printf("-");
	}
	printf("\n");

	printf("Status:\t\t");
	for (i = 0; i < N; i++) {
		printf("%s\t", statusStore[i]);
	}

	printf("\n\n---------------------------------- Summary Table ----------------------------------\n");
	printf("Faults\tHits\tFault Rate\tHit Rate\tMemory Utilization\tWrite Backs\n");
	printf("%d\t%d\t%.2f%%\t\t%.2f%%\t\t%.2f%%\t\t\t%d\n", faults, hits, ((float)faults / N) * 100, ((float)hits / N) * 100, ((float)totalFilledFrames / (N * FRAMES)) * 100, writeBacks);
	printf("-----------------------------------------------------------------------------------\n");

	printf("\n[P|R|M] means [Page|Reference Bit|Modified Bit]\n");
	printf("Operation: R = Read, W = Write | TAU = %d (Pages older than %d units outside working set)\n", TAU, TAU);
}

// for printing summary table except wsclock which has its own summary with write back count
void printSummary(int faults, int hits, int totalFilledFrames) {
    printf("\n\n------------------------- Summary Table --------------------------\n");
    printf("Faults\tHits\tFault Rate\tHit Rate\tMemory Utilization\n");
    printf("%d\t%d\t%.2f%%\t\t%.2f%%\t\t%.2f%%\n",
           faults, hits, ((float)faults / N) * 100,
           ((float)hits / N) * 100,
           ((float)totalFilledFrames / (N * FRAMES)) * 100);
    printf("------------------------------------------------------------------\n");
}
