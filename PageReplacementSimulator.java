/*Operation Systems - Page Replacement Simulator

This program simulates various page replacement algorithms where users can visualize how pages are loaded into memory frames
and how page faults occur.

Java version:
- Same concepts and logic as C version
- GUI added using Java Swing
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class PageReplacementSimulator {

    static final int MAX_FRAMES = 10;
    static final int MAX_N = 50;

    static int frames = 3; // no. of available page frames, default value
    static int n = 18;     // no. of page ref
    static int TAU = 4;    // aging time/working set age threshold for wsclock

    static int[] refString = {0, 4, 1, 4, 2, 4, 3, 4, 2, 4, 0, 4, 1, 4, 2, 2, 3, 1};
    static char[] op = {'R', 'W', 'R', 'R', 'W', 'R', 'R', 'W', 'R', 'R', 'W', 'R', 'R', 'W', 'R', 'R', 'W', 'R'}; // R = read, W = write

    // GUI colors
    static final Color C_BG = new Color(10, 12, 18);
    static final Color C_SURFACE = new Color(16, 19, 28);
    static final Color C_CARD = new Color(22, 26, 38);
    static final Color C_BORDER = new Color(38, 44, 62);
    static final Color C_ACCENT = new Color(99, 179, 237);
    static final Color C_GREEN = new Color(72, 199, 142);
    static final Color C_RED = new Color(252, 100, 100);
    static final Color C_PURPLE = new Color(160, 132, 255);
    static final Color C_YELLOW = new Color(246, 194, 62);
    static final Color C_TEXT = new Color(220, 225, 235);
    static final Color C_MUTED = new Color(90, 100, 120);
    static final Color C_ACTIVE = new Color(99, 179, 237, 18);

    static JPanel mainArea;
    static JButton[] sideButtons = new JButton[6];

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            if (showInputDialog()) {
                launchGUI();
            } else {
                System.exit(0);
            }
        });
    }

    /* ----------------------------------------------- User Input Data --------------------------------------------------- */

    static boolean showInputDialog() {
        JDialog dlg = new JDialog((Frame) null, "Simulator Setup", true);
        dlg.setSize(560, 610);
        dlg.setMinimumSize(new Dimension(500, 460));
        dlg.setLocationRelativeTo(null);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(C_BG);

        boolean[] confirmed = {false};
        boolean[] useCustom = {false};

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        header.setBackground(C_SURFACE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));

        JLabel title = new JLabel("Page Replacement Simulator");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(C_TEXT);
        header.add(title);
        dlg.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(C_BG);
        body.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        JLabel q = new JLabel("Use default configuration?");
        q.setFont(new Font("SansSerif", Font.BOLD, 15));
        q.setForeground(C_TEXT);
        q.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(q);

        JLabel sub = new JLabel("Default: Frames=3, Pages=18, TAU=4, standard reference string and R/W operations");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(C_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(Box.createVerticalStrut(6));
        body.add(sub);
        body.add(Box.createVerticalStrut(18));

        JPanel toggleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toggleRow.setBackground(C_BG);
        toggleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnDefault = makeToggleBtn("Use Defaults", true);
        JButton btnCustom = makeToggleBtn("Enter Custom", false);
        toggleRow.add(btnDefault);
        toggleRow.add(Box.createHorizontalStrut(8));
        toggleRow.add(btnCustom);
        body.add(toggleRow);
        body.add(Box.createVerticalStrut(20));

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBackground(C_BG);
        fields.setAlignmentX(Component.LEFT_ALIGNMENT);
        fields.setVisible(false);

        JTextField tfFrames = inputField("3");
        JTextField tfTau = inputField("4");
        JTextField tfRef = inputField("0,4,1,4,2,4,3,4,2,4,0,4,1,4,2,2,3,1");
        JTextField tfOp = inputField("R,W,R,R,W,R,R,W,R,R,W,R,R,W,R,R,W,R");

        fields.add(fieldRow("Number of Frames", "Allowed: 1 to 10", tfFrames));
        fields.add(Box.createVerticalStrut(14));
        fields.add(fieldRow("TAU for WSClock", "Example: 4", tfTau));
        fields.add(Box.createVerticalStrut(14));
        fields.add(fieldRow("Reference String", "Comma-separated page numbers", tfRef));
        fields.add(Box.createVerticalStrut(14));
        fields.add(fieldRow("Operations for WSClock", "Use R/W separated by commas. Leave blank to make all operations R.", tfOp));
        body.add(fields);

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        errLabel.setForeground(C_RED);
        errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(Box.createVerticalStrut(8));
        body.add(errLabel);

        btnDefault.addActionListener(e -> {
            useCustom[0] = false;
            setToggleActive(btnDefault, true);
            setToggleActive(btnCustom, false);
            fields.setVisible(false);
            body.revalidate();
            body.repaint();
        });

        btnCustom.addActionListener(e -> {
            useCustom[0] = true;
            setToggleActive(btnDefault, false);
            setToggleActive(btnCustom, true);
            fields.setVisible(true);
            body.revalidate();
            body.repaint();
        });

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.getViewport().setBackground(C_BG);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(12);
        dlg.add(bodyScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        footer.setBackground(C_SURFACE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));

        JButton cancel = footerBtn("Cancel", C_MUTED, C_CARD);
        JButton launch = footerBtn("Launch Simulator", C_BG, C_ACCENT);

        cancel.addActionListener(e -> dlg.dispose());

        launch.addActionListener(e -> {
            if (useCustom[0]) {
                try {
                    int newFrames = Integer.parseInt(tfFrames.getText().trim());
                    int newTau = Integer.parseInt(tfTau.getText().trim());

                    if (newFrames < 1 || newFrames > MAX_FRAMES) {
                        errLabel.setText("Frames must be between 1 and 10.");
                        return;
                    }

                    String[] refParts = tfRef.getText().trim().split(",");
                    if (refParts.length < 1 || refParts.length > MAX_N) {
                        errLabel.setText("Reference string must contain 1 to 50 values.");
                        return;
                    }

                    int[] newRef = new int[refParts.length];
                    for (int i = 0; i < refParts.length; i++) {
                        newRef[i] = Integer.parseInt(refParts[i].trim());
                        if (newRef[i] < 0) {
                            errLabel.setText("Page numbers must be 0 or greater.");
                            return;
                        }
                    }

                    char[] newOp = new char[newRef.length];
                    String opText = tfOp.getText().trim();

                    if (opText.isEmpty()) {
                        for (int i = 0; i < newOp.length; i++) {
                            newOp[i] = 'R';
                        }
                    } else {
                        String[] opParts = opText.split(",");

                        if (opParts.length != newRef.length) {
                            errLabel.setText("Operations count must match reference string count, or leave it blank.");
                            return;
                        }

                        for (int i = 0; i < opParts.length; i++) {
                            char c = Character.toUpperCase(opParts[i].trim().charAt(0));

                            if (c != 'R' && c != 'W') {
                                errLabel.setText("Operations must be only R or W.");
                                return;
                            }

                            newOp[i] = c;
                        }
                    }

                    frames = newFrames;
                    TAU = newTau;
                    refString = newRef;
                    op = newOp;
                    n = refString.length;

                } catch (Exception ex) {
                    errLabel.setText("Invalid input. Use numbers and commas only.");
                    return;
                }
            }

            confirmed[0] = true;
            dlg.dispose();
        });

        footer.add(cancel);
        footer.add(launch);
        dlg.add(footer, BorderLayout.SOUTH);

        dlg.setVisible(true);
        return confirmed[0];
    }

    static void runAlgorithm(String id) {
        SimResult result;

        if (id.equals("fifo")) {
            result = runFIFO();
        } else if (id.equals("lru")) {
            result = runLRU();
        } else if (id.equals("secondchance")) {
            result = runSecondChance();
        } else if (id.equals("lfuaging")) {
            result = runLFUAging();
        } else if (id.equals("mru")) {
            result = runMRU();
        } else {
            result = runWSClock();
        }

        showResult(result);
    }

    /* ------------------------------------- FIFO (First In, First Out) Algorithm ---------------------------------------- */

    static SimResult runFIFO() {
        int[] frame = new int[MAX_FRAMES];
        int[][] frameStore = new int[MAX_FRAMES][MAX_N];
        String[] statusStore = new String[MAX_N];

        int page;
        int found;
        int pointer = 0;
        int filled = 0;
        int faults = 0;
        int hits = 0;
        int totalFilledFrames = 0;

        for (int i = 0; i < frames; i++) {
            frame[i] = -1;
        }

        for (int i = 0; i < n; i++) {
            page = refString[i];
            found = 0;

            // Check hit
            for (int j = 0; j < frames; j++) {
                if (frame[j] == page) {
                    found = 1;
                    hits++;
                    statusStore[i] = "HIT";
                    break;
                }
            }

            // Page fault
            if (found == 0) {
                faults++;
                statusStore[i] = "FAULT";

                // If empty frame exists, insert without replacement
                if (filled < frames) {
                    frame[filled] = page;
                    filled++;
                } else {
                    // FIFO replacement
                    frame[pointer] = page;
                    pointer = (pointer + 1) % frames;
                }
            }

            totalFilledFrames += filled;

            // store current state values into history matrix to print later
            for (int j = 0; j < frames; j++) {
                frameStore[j][i] = frame[j];
            }
        }

        SimResult r = new SimResult();
        r.title = "FIFO";
        r.subtitle = "First In, First Out";
        r.desc = "Oldest page is replaced first.";
        r.hFrame = frameStore;
        r.hStatus = statusStore;
        r.faults = faults;
        r.hits = hits;
        r.totalFilledFrames = totalFilledFrames;
        r.writeBacks = -1;
        return r;
    }

    /* ------------------------------------- LRU (Least Recently Used) Algorithm ----------------------------------------- */

    static SimResult runLRU() {
        int[] frame = new int[MAX_FRAMES];
        int[] lastUsed = new int[MAX_FRAMES];

        int[][] historyFrame = new int[MAX_FRAMES][MAX_N];
        String[] historyStatus = new String[MAX_N];

        int page;
        int found;
        int time = 0;
        int filled = 0;
        int faults = 0;
        int hits = 0;
        int totalOccupiedUnits = 0;

        int victimIndex;
        int oldestTime;

        for (int i = 0; i < frames; i++) {
            frame[i] = -1;
            lastUsed[i] = 0;
        }

        for (int i = 0; i < n; i++) {
            page = refString[i];
            found = 0;
            time++;

            // Check if requested page is already in memory
            for (int j = 0; j < frames; j++) {
                if (frame[j] == page) {
                    found = 1;
                    lastUsed[j] = time;
                    hits++;
                    historyStatus[i] = "HIT";
                    break;
                }
            }

            // Page fault
            if (found == 0) {
                faults++;
                historyStatus[i] = "FAULT";

                // If empty frame exists, insert page directly
                if (filled < frames) {
                    frame[filled] = page;
                    lastUsed[filled] = time;
                    filled++;
                } else {
                    // Find the least recently used page
                    victimIndex = 0;
                    oldestTime = lastUsed[0];

                    for (int j = 1; j < frames; j++) {
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
            for (int j = 0; j < frames; j++) {
                historyFrame[j][i] = frame[j];
            }
        }

        SimResult r = new SimResult();
        r.title = "LRU";
        r.subtitle = "Least Recently Used";
        r.desc = "Page that was least recently used is replaced.";
        r.hFrame = historyFrame;
        r.hStatus = historyStatus;
        r.faults = faults;
        r.hits = hits;
        r.totalFilledFrames = totalOccupiedUnits;
        r.writeBacks = -1;
        return r;
    }

    /* ---------------------------------------- Second Chance (Clock) Algorithm ------------------------------------------- */

    static SimResult runSecondChance() {
        int[] frame = new int[MAX_FRAMES];
        int[] rBit = new int[MAX_FRAMES];

        int[][] frameStore = new int[MAX_FRAMES][MAX_N];
        int[][] rBitStore = new int[MAX_FRAMES][MAX_N];
        String[] statusStore = new String[MAX_N];

        int page;
        int found;
        int pointer = 0;
        int filled = 0;
        int faults = 0;
        int hits = 0;
        int totalFilledFrames = 0; // counts total filled frame slots needed for memory utilization calc

        for (int i = 0; i < frames; i++) {
            frame[i] = -1;
            rBit[i] = 0;
        }

        for (int i = 0; i < n; i++) {
            page = refString[i];
            found = 0;

            // Check hit
            for (int j = 0; j < frames; j++) {
                if (frame[j] == page) {
                    found = 1;
                    rBit[j] = 1;
                    hits++;
                    statusStore[i] = "HIT";
                    break;
                }
            }

            // Page fault
            if (found == 0) {
                faults++;
                statusStore[i] = "FAULT";
                
                // If empty frame exists, insert without replacement
                if (filled < frames) {
                    frame[filled] = page;
                    rBit[filled] = 0;
                    filled++;
                } else {
                    // Second Chance replacement
                    while (rBit[pointer] == 1) {
                        rBit[pointer] = 0;
                        pointer = (pointer + 1) % frames;
                    }

                    frame[pointer] = page;
                    rBit[pointer] = 0;

                    pointer = (pointer + 1) % frames;
                }
            }

            totalFilledFrames += filled;

            // store current state values into history matrix to print later
            for (int j = 0; j < frames; j++) {
                frameStore[j][i] = frame[j];
                rBitStore[j][i] = rBit[j];
            }
        }

        SimResult r = new SimResult();
        r.title = "Second Chance";
        r.subtitle = "Clock Algorithm";
        r.desc = "New page R=0, hit makes R=1.";
        r.note = "[P|R] means [Page|Reference Bit]";
        r.hFrame = frameStore;
        r.hRef = rBitStore;
        r.hStatus = statusStore;
        r.faults = faults;
        r.hits = hits;
        r.totalFilledFrames = totalFilledFrames;
        r.writeBacks = -1;
        return r;
    }

    /* -------------------- LFU with Dynamic Aging (Least Frequently Used with Dynamic Aging) Algorithm -------------------- */

    static SimResult runLFUAging() {
        int[] frame = new int[MAX_FRAMES];
        int[] freq = new int[MAX_FRAMES];
        int[] age = new int[MAX_FRAMES];

        int[][] frameStore = new int[MAX_FRAMES][MAX_N];
        int[][] freqStore = new int[MAX_FRAMES][MAX_N];
        String[] statusStore = new String[MAX_N];

        int page;
        int found;
        int filled = 0;
        int faults = 0;
        int hits = 0;
        int totalFilledFrames = 0;
        int victim;

        for (int i = 0; i < frames; i++) {
            frame[i] = -1;
            freq[i] = 0;
            age[i] = 0;
        }

        for (int i = 0; i < n; i++) {
            page = refString[i];
            found = 0;

            // Check hit
            for (int j = 0; j < frames; j++) {
                if (frame[j] == page) {
                    found = 1;
                    freq[j]++;
                    age[j] = i;
                    hits++;
                    statusStore[i] = "HIT";
                    break;
                }
            }

            // Page fault
            if (found == 0) {
                faults++;
                statusStore[i] = "FAULT";

                if (filled < frames) {
                    frame[filled] = page;
                    freq[filled] = 1;
                    age[filled] = i;
                    filled++;
                } else {
                    victim = 0;

                    for (int j = 1; j < frames; j++) {
                        if (freq[j] < freq[victim]) {
                            victim = j;
                        } else if (freq[j] == freq[victim] && age[j] < age[victim]) {
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
                for (int j = 0; j < filled; j++) {
                    if (freq[j] > 1) {
                        freq[j] = freq[j] / 2;
                    }
                }
            }

            totalFilledFrames += filled;

            for (int j = 0; j < frames; j++) {
                frameStore[j][i] = frame[j];
                freqStore[j][i] = freq[j];
            }
        }

        SimResult r = new SimResult();
        r.title = "LFU with Dynamic Aging";
        r.subtitle = "Least Frequently Used with Dynamic Aging";
        r.desc = "Page with lowest frequency count is replaced. If frequencies are equal, the older page is replaced.";
        r.note = "[P|F] means [Page|Frequency]";
        r.hFrame = frameStore;
        r.hFreq = freqStore;
        r.hStatus = statusStore;
        r.faults = faults;
        r.hits = hits;
        r.totalFilledFrames = totalFilledFrames;
        r.writeBacks = -1;
        return r;
    }
    /* ------------------------------------ MRU (Most Recently Used) Algorithm -------------------------------------------- */

    static SimResult runMRU() {
        int[] frame = new int[MAX_FRAMES];
        int[] lastUsed = new int[MAX_FRAMES];

        int[][] historyFrame = new int[MAX_FRAMES][MAX_N];
        String[] historyStatus = new String[MAX_N];

        int page;
        int found;
        int time = 0;
        int filled = 0;
        int faults = 0;
        int hits = 0;
        int totalOccupiedUnits = 0;

        int victimIndex;
        int newestTime;

        for (int i = 0; i < frames; i++) {
            frame[i] = -1;
            lastUsed[i] = 0;
        }

        for (int i = 0; i < n; i++) {
            page = refString[i];
            found = 0;
            time++;

            // Check if requested page is already in memory
            for (int j = 0; j < frames; j++) {
                if (frame[j] == page) {
                    found = 1;
                    lastUsed[j] = time;
                    hits++;
                    historyStatus[i] = "HIT";
                    break;
                }
            }

            // Page fault
            if (found == 0) {
                faults++;
                historyStatus[i] = "FAULT";

                // If empty frame exists, insert page directly
                if (filled < frames) {
                    frame[filled] = page;
                    lastUsed[filled] = time;
                    filled++;
                } else {
                    // Find the most recently used page
                    victimIndex = 0;
                    newestTime = lastUsed[0];

                    for (int j = 1; j < frames; j++) {
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
            for (int j = 0; j < frames; j++) {
                historyFrame[j][i] = frame[j];
            }
        }

        SimResult r = new SimResult();
        r.title = "MRU";
        r.subtitle = "Most Recently Used";
        r.desc = "Page that was most recently used is replaced.";
        r.hFrame = historyFrame;
        r.hStatus = historyStatus;
        r.faults = faults;
        r.hits = hits;
        r.totalFilledFrames = totalOccupiedUnits;
        r.writeBacks = -1;
        return r;
    }

    /* ---------------------------------- WSCLOCK (Working Set Clock) Algorithm ------------------------------------------- */

    static SimResult runWSClock() {
        int[] frame = new int[MAX_FRAMES];
        int[] rBit = new int[MAX_FRAMES];
        int[] mBit = new int[MAX_FRAMES];
        int[] lastUsed = new int[MAX_FRAMES];

        int[][] frameStore = new int[MAX_FRAMES][MAX_N];
        int[][] rBitStore = new int[MAX_FRAMES][MAX_N];
        int[][] mBitStore = new int[MAX_FRAMES][MAX_N];
        String[] statusStore = new String[MAX_N];

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

        for (int i = 0; i < frames; i++) {
            frame[i] = -1;
            rBit[i] = 0;
            mBit[i] = 0;
            lastUsed[i] = 0;
        }

        for (int i = 0; i < n; i++) {
            page = refString[i];
            found = 0;
            time++;

            // Check hit
            for (int j = 0; j < frames; j++) {
                if (frame[j] == page) {
                    found = 1;
                    rBit[j] = 1;
                    lastUsed[j] = time;

                    if (op[i] == 'W') { // if page hit is W update. no change in mBit for hit when op is read but previous same page was write
                        mBit[j] = 1;
                    }

                    hits++;
                    statusStore[i] = "HIT";
                    break;
                }
            }

            // Page fault
            if (found == 0) {
                faults++;
                statusStore[i] = "FAULT";

                // If empty frame exists, insert without replacement
                if (filled < frames) {
                    frame[filled] = page;
                    rBit[filled] = 1;

                    if (op[i] == 'W') {
                        mBit[filled] = 1;
                    } else {
                        mBit[filled] = 0;
                    }

                    lastUsed[filled] = time;
                    filled++;
                } else {
                    // wsclock replacement algo
                    replaced = 0;
                    scanned = 0;
                    oldestIndex = pointer;
                    oldestAge = -1;

                    while (replaced == 0 && scanned < frames * 2) {
                        age = time - lastUsed[pointer];

                        if (age > oldestAge) {
                            oldestAge = age;
                            oldestIndex = pointer;
                        }

                        if (rBit[pointer] == 1) { // if ref bit == 1, give second chance
                            rBit[pointer] = 0;
                            pointer = (pointer + 1) % frames;
                        } else { // else if R-bit == 0, check age/working set condition
                            if (age <= TAU) { // if pg in working set, give second chance
                                pointer = (pointer + 1) % frames;
                            } else { // else if pg not in working set (age > TAU), check dirty/modified bit
                                if (mBit[pointer] == 1) { // if dirty (M-bit == 1), write back, give second chance
                                    mBit[pointer] = 0;
                                    writeBacks++;
                                    pointer = (pointer + 1) % frames;
                                } else { // else if clean (M-bit == 0), replace
                                    frame[pointer] = page;
                                    rBit[pointer] = 1;

                                    if (op[i] == 'W') { // if new pg is write, set dirty/m bit = 1
                                        mBit[pointer] = 1;
                                    } else {
                                        mBit[pointer] = 0; // if new pg is read, set dirty bit = 0
                                    }

                                    lastUsed[pointer] = time; // update last used time for new pg
                                    pointer = (pointer + 1) % frames;
                                    replaced = 1; // replacement done, exit loop
                                }
                            }
                        }

                        scanned++; // increment scanned count to prevent infinite loop if all pages in WS
                    }

                    // If no perfect victim found, replace oldest pg
                    if (replaced == 0) {
                        pointer = oldestIndex;

                        if (mBit[pointer] == 1) {
                            writeBacks++;
                        }

                        frame[pointer] = page;
                        rBit[pointer] = 1;

                        if (op[i] == 'W') {
                            mBit[pointer] = 1;
                        } else {
                            mBit[pointer] = 0;
                        }

                        lastUsed[pointer] = time;
                        pointer = (pointer + 1) % frames;
                    }
                }
            }

            totalFilledFrames += filled;

            // store current state values into history/store matrix
            for (int j = 0; j < frames; j++) {
                frameStore[j][i] = frame[j];
                rBitStore[j][i] = rBit[j];
                mBitStore[j][i] = mBit[j];
            }
        }

        SimResult r = new SimResult();
        r.title = "WSClock";
        r.subtitle = "Working Set Clock (TAU = " + TAU + ")";
        r.desc = "TAU = " + TAU + ", requested page R=1, W operation makes M=1.";
        r.note = "[P|R|M] means [Page|Reference Bit|Modified Bit]. Operation: R = Read, W = Write.";
        r.hFrame = frameStore;
        r.hRef = rBitStore;
        r.hDirty = mBitStore;
        r.hStatus = statusStore;
        r.operations = op;
        r.faults = faults;
        r.hits = hits;
        r.totalFilledFrames = totalFilledFrames;
        r.writeBacks = writeBacks;
        return r;
    }

    // for storing result data. This replace printed table from java (C) with data shown in JTable
    static class SimResult {
        String title;
        String subtitle;
        String desc;
        String note;
        int[][] hFrame;
        int[][] hRef;
        int[][] hDirty;
        int[][] hFreq;
        String[] hStatus;
        char[] operations;
        int faults;
        int hits;
        int totalFilledFrames;
        int writeBacks;
    }

    /* ----------------------------------------------------- GUI Part ----------------------------------------------------- */

    static void launchGUI() {
        JFrame win = new JFrame("Virtual Memory Page Replacement Simulator");
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        win.setSize(1180, 720);
        win.setMinimumSize(new Dimension(900, 580));
        win.setLocationRelativeTo(null);
        win.getContentPane().setBackground(C_BG);
        win.setLayout(new BorderLayout());

        win.add(buildHeader(), BorderLayout.NORTH);
        win.add(buildSidebar(), BorderLayout.WEST);

        mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(C_BG);
        mainArea.add(buildWelcome(), BorderLayout.CENTER);

        win.add(mainArea, BorderLayout.CENTER);
        win.setVisible(true);
    }

    static JPanel buildHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 13));
        p.setBackground(C_SURFACE);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));

        JLabel title = new JLabel("Virtual Memory Page Replacement Simulator");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(C_TEXT);

        JLabel badge = pill("OS Lab", C_ACCENT);
        JLabel badge2 = pill("Java Swing GUI", C_PURPLE);

        p.add(title);
        p.add(Box.createHorizontalStrut(6));
        p.add(badge);
        p.add(badge2);

        return p;
    }

    static JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, C_BORDER),
                BorderFactory.createEmptyBorder(20, 12, 20, 12)));
        p.setPreferredSize(new Dimension(220, 0));

        String[] names = {"FIFO", "LRU", "Second Chance", "LFU Aging", "MRU", "WSClock"};
        String[] ids = {"fifo", "lru", "secondchance", "lfuaging", "mru", "wsclock"};
        String[] hints = {
                "First In, First Out",
                "Least Recently Used",
                "Clock / R-bit",
                "Freq + Aging",
                "Most Recently Used",
                "Working Set Clock"
        };

        p.add(sectionLabel("ALGORITHMS"));
        p.add(Box.createVerticalStrut(8));

        for (int i = 0; i < names.length; i++) {
            final int index = i;
            final String id = ids[i];

            JButton btn = new JButton();
            btn.setLayout(new BorderLayout());
            btn.setBackground(C_SURFACE);
            btn.setOpaque(true);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 0, 0, 0)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));

            JPanel textCol = new JPanel();
            textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
            textCol.setOpaque(false);

            JLabel nameLbl = new JLabel(names[i]);
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            nameLbl.setForeground(C_MUTED);

            JLabel hintLbl = new JLabel(hints[i]);
            hintLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
            hintLbl.setForeground(new Color(60, 70, 90));

            textCol.add(nameLbl);
            textCol.add(hintLbl);
            btn.add(textCol, BorderLayout.CENTER);

            btn.addActionListener(e -> {
                setActiveButton(index);
                runAlgorithm(id);
            });

            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (btn.getClientProperty("active") != Boolean.TRUE) {
                        btn.setBackground(C_CARD);
                        nameLbl.setForeground(C_TEXT);
                    }
                }

                public void mouseExited(MouseEvent e) {
                    if (btn.getClientProperty("active") != Boolean.TRUE) {
                        btn.setBackground(C_SURFACE);
                        nameLbl.setForeground(C_MUTED);
                    }
                }
            });

            sideButtons[i] = btn;
            p.add(btn);
            p.add(Box.createVerticalStrut(2));
        }

        // Back btn to return to welcome/menu screen
        p.add(Box.createVerticalStrut(12));

        JButton backBtn = new JButton("Back to Menu");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        backBtn.setBackground(C_CARD);
        backBtn.setForeground(C_TEXT);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        backBtn.addActionListener(e -> goBackToMenu());

        p.add(backBtn);

        p.add(Box.createVerticalStrut(20));
        p.add(sectionLabel("CONFIG"));
        p.add(Box.createVerticalStrut(8));
        p.add(configPanel());

        p.add(Box.createVerticalStrut(18));
        p.add(sectionLabel("REFERENCE STRING"));
        p.add(Box.createVerticalStrut(8));
        p.add(refLabel());
        
        p.add(Box.createVerticalStrut(18));
        p.add(sectionLabel("WSCLOCK OPERATIONS"));
        p.add(Box.createVerticalStrut(8));
        p.add(opLabel());
        
        p.add(Box.createVerticalGlue());

        return p;
    }

    static void goBackToMenu() {
        mainArea.removeAll();
        mainArea.add(buildWelcome(), BorderLayout.CENTER);

        for (int i = 0; i < sideButtons.length; i++) {
            JButton b = sideButtons[i];

            if (b != null) {
                b.putClientProperty("active", Boolean.FALSE);
                b.setBackground(C_SURFACE);
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 0, 0, 0)),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));

                Component[] components = b.getComponents();

                for (Component c : components) {
                    if (c instanceof JPanel) {
                        JPanel panel = (JPanel) c;

                        for (Component inner : panel.getComponents()) {
                            if (inner instanceof JLabel) {
                                JLabel label = (JLabel) inner;

                                if (label.getFont().isBold()) {
                                    label.setForeground(C_MUTED);
                                } else {
                                    label.setForeground(new Color(60, 70, 90));
                                }
                            }
                        }
                    }
                }
            }
        }

        mainArea.revalidate();
        mainArea.repaint();
    }
    
    static JPanel configPanel() {
        JPanel cfg = new JPanel(new GridLayout(3, 2, 4, 6));
        cfg.setBackground(C_SURFACE);
        cfg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        cfg.setAlignmentX(Component.LEFT_ALIGNMENT);

        cfg.add(cfgKey("Frames"));
        cfg.add(cfgVal(String.valueOf(frames), C_GREEN));
        cfg.add(cfgKey("Pages"));
        cfg.add(cfgVal(String.valueOf(n), C_GREEN));
        cfg.add(cfgKey("TAU"));
        cfg.add(cfgVal(String.valueOf(TAU), C_YELLOW));

        return cfg;
    }

    static JLabel refLabel() { //display ref string, which contains pg request sequence used in simulation
        StringBuilder sb = new StringBuilder("<html><span style='color:#5a6478;font-family:monospace;font-size:10px;'>");

        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(",");
            }

            if (i > 0 && i % 8 == 0) {
                sb.append("<br>");
            }

            sb.append(refString[i]);
        }

        sb.append("</span></html>");

        JLabel l = new JLabel(sb.toString());
        l.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
    
    static JLabel opLabel() { // display R/W operation sequence used by WSClock algo in simulation
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < n && i < op.length; i++) {
            if (i > 0) {
                sb.append(",");
            }

            if (i > 0 && i % 8 == 0) {
                sb.append("<br>");
            }

            sb.append(op[i]);
        }

        JLabel l = new JLabel("<html><span style='color:#5f6f8f;font-family:monospace;font-size:10px;'>"
                + sb.toString()
                + "</span></html>");

        l.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        return l;
    }

    static JPanel buildWelcome() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_BG);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(C_BG);

        JLabel t1 = new JLabel("Select an algorithm");
        t1.setFont(new Font("SansSerif", Font.BOLD, 23));
        t1.setForeground(C_TEXT);
        t1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel t2 = new JLabel("Click any algorithm from the left side to run the simulation.");
        t2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t2.setForeground(C_MUTED);
        t2.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(t1);
        box.add(Box.createVerticalStrut(10));
        box.add(t2);
        p.add(box);
        return p;
    }

    static void showResult(SimResult r) {
        mainArea.removeAll();

        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(C_BG);
        page.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);

        int row = 0;

        JLabel title = new JLabel(r.title + " - " + r.subtitle);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(C_TEXT);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 5, 0);
        page.add(title, gbc);

        JLabel desc = new JLabel(r.desc);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(C_MUTED);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 20, 0);
        page.add(desc, gbc);

        int cardCount = (r.writeBacks >= 0) ? 6 : 5;
        JPanel stats = new JPanel(new GridLayout(1, cardCount, 10, 0));
        stats.setBackground(C_BG);

        stats.add(statCard("Faults", String.valueOf(r.faults), C_RED));
        stats.add(statCard("Hits", String.valueOf(r.hits), C_GREEN));
        stats.add(statCard("Fault Rate", String.format("%.2f%%", ((float) r.faults / n) * 100), C_RED));
        stats.add(statCard("Hit Rate", String.format("%.2f%%", ((float) r.hits / n) * 100), C_GREEN));
        stats.add(statCard("Memory Util", String.format("%.2f%%", ((float) r.totalFilledFrames / (n * frames)) * 100), C_PURPLE));

        if (r.writeBacks >= 0) {
            stats.add(statCard("Write Backs", String.valueOf(r.writeBacks), C_ACCENT));
        }

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 22, 0);
        page.add(stats, gbc);

        JTable table = buildTable(r);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(C_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(C_BORDER));
        scroll.setPreferredSize(new Dimension(100, 36 + (frames + 4) * 30));

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 0, 0);
        page.add(scroll, gbc);

        if (r.note != null) {
            JLabel note = new JLabel(r.note);
            note.setFont(new Font("SansSerif", Font.PLAIN, 11));
            note.setForeground(C_MUTED);
            note.setOpaque(true);
            note.setBackground(C_CARD);
            note.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, C_PURPLE),
                    BorderFactory.createEmptyBorder(9, 12, 9, 12)));

            gbc.gridy = row++;
            gbc.insets = new Insets(14, 0, 0, 0);
            page.add(note, gbc);
        }

        JLabel chartTitle = new JLabel("Fault vs Hit per Step");
        chartTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        chartTitle.setForeground(C_MUTED);
        gbc.gridy = row++;
        gbc.insets = new Insets(24, 0, 8, 0);
        page.add(chartTitle, gbc);

        FaultHitChart chart = new FaultHitChart(r.hStatus);
        chart.setPreferredSize(new Dimension(100, 60));
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 0, 0);
        page.add(chart, gbc);

        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        page.add(Box.createGlue(), gbc);

        JScrollPane outer = new JScrollPane(page);
        outer.setBorder(null);
        outer.getViewport().setBackground(C_BG);
        outer.getVerticalScrollBar().setUnitIncrement(16);

        mainArea.add(outer, BorderLayout.CENTER);
        mainArea.revalidate();
        mainArea.repaint();
    }

    static JTable buildTable(SimResult r) {
        String[] cols = new String[n + 1];
        cols[0] = "";

        for (int i = 0; i < n; i++) {
            cols[i + 1] = String.valueOf(i + 1);
        }

        int rows = 2 + frames + 1;
        if (r.operations != null) {
            rows++;
        }

        Object[][] data = new Object[rows][n + 1];
        int row = 0;

        data[row][0] = "Step";
        for (int i = 0; i < n; i++) {
            data[row][i + 1] = i + 1;
        }
        row++;

        if (r.operations != null) {
            data[row][0] = "Operation";
            for (int i = 0; i < n; i++) {
                data[row][i + 1] = r.operations[i];
            }
            row++;
        }

        data[row][0] = "Ref Page";
        for (int i = 0; i < n; i++) {
            data[row][i + 1] = refString[i];
        }
        row++;

        for (int j = 0; j < frames; j++) {
            data[row][0] = "Frame " + (j + 1);

            for (int i = 0; i < n; i++) {
                if (r.hFrame[j][i] == -1) {
                    data[row][i + 1] = "-";
                } else {
                    String cell = String.valueOf(r.hFrame[j][i]);

                    if (r.hRef != null) {
                        cell += "|" + r.hRef[j][i];
                    }

                    if (r.hDirty != null) {
                        cell += "|" + r.hDirty[j][i];
                    }

                    if (r.hFreq != null) {
                        cell += "|" + r.hFreq[j][i];
                    }

                    data[row][i + 1] = cell;
                }
            }
            row++;
        }

        data[row][0] = "Status";
        for (int i = 0; i < n; i++) {
            data[row][i + 1] = r.hStatus[i];
        }

        return new JTable(data, cols);
    }

    static void styleTable(JTable t) {
        t.setFont(new Font("Monospaced", Font.PLAIN, 12));
        t.setRowHeight(30);
        t.setBackground(C_CARD);
        t.setForeground(C_TEXT);
        t.setGridColor(C_BORDER);
        t.setShowGrid(true);
        t.setEnabled(false);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        t.getColumnModel().getColumn(0).setPreferredWidth(78);
        for (int i = 1; i <= n; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(58);
        }

        JTableHeader header = t.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 10));
        header.setBackground(new Color(14, 17, 26));
        header.setForeground(C_MUTED);
        header.setReorderingAllowed(false);

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, focus, row, col);

                String v = val == null ? "" : val.toString();
                String rowKey = tbl.getValueAt(row, 0).toString();

                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

                if (col == 0) {
                    setBackground(new Color(14, 17, 26));
                    setForeground(C_MUTED);
                    setFont(new Font("SansSerif", Font.BOLD, 10));
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    return this;
                }

                if (rowKey.equals("Status")) {
                    if (v.equals("FAULT")) {
                        setBackground(new Color(252, 100, 100, 30));
                        setForeground(C_RED);
                    } else if (v.equals("HIT")) {
                        setBackground(new Color(72, 199, 142, 30));
                        setForeground(C_GREEN);
                    } else {
                        setBackground(C_CARD);
                        setForeground(C_MUTED);
                    }
                } else if (rowKey.equals("Ref Page")) {
                    setBackground(new Color(99, 179, 237, 15));
                    setForeground(C_ACCENT);
                } else if (rowKey.equals("Operation")) {
                    setBackground(new Color(160, 132, 255, 15));
                    setForeground(C_PURPLE);
                } else if (rowKey.equals("Step")) {
                    setBackground(new Color(14, 17, 26));
                    setForeground(C_MUTED);
                } else {
                    if (v.equals("-")) {
                        setBackground(C_CARD);
                        setForeground(new Color(38, 44, 62));
                    } else {
                        setBackground(new Color(22, 28, 42));
                        setForeground(C_TEXT);
                    }
                }

                return this;
            }
        });
    }

    static JPanel statCard(String label, String value, Color col) {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(C_CARD);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER),
                BorderFactory.createEmptyBorder(10, 14, 12, 14)));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 9));
        lbl.setForeground(C_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Monospaced", Font.BOLD, 19));
        val.setForeground(col);

        c.add(lbl);
        c.add(Box.createVerticalStrut(5));
        c.add(val);
        return c;
    }

    static JButton makeToggleBtn(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(140, 36));
        setToggleActive(b, active);
        return b;
    }

    static void setToggleActive(JButton b, boolean active) {
        if (active) {
            b.setBackground(C_ACCENT);
            b.setForeground(C_BG);
            b.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        } else {
            b.setBackground(C_CARD);
            b.setForeground(C_MUTED);
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(C_BORDER),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        }
        b.setOpaque(true);
    }

    static JTextField inputField(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tf.setBackground(C_CARD);
        tf.setForeground(C_TEXT);
        tf.setCaretColor(C_ACCENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return tf;
    }

    static JPanel fieldRow(String label, String hint, JTextField tf) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_BG);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(C_TEXT);

        JLabel hnt = new JLabel(hint);
        hnt.setFont(new Font("SansSerif", Font.PLAIN, 10));
        hnt.setForeground(C_MUTED);

        p.add(lbl);
        p.add(Box.createVerticalStrut(3));
        p.add(hnt);
        p.add(Box.createVerticalStrut(5));
        p.add(tf);

        return p;
    }

    static JButton footerBtn(String text, Color fg, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(fg);
        b.setBackground(bg);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        return b;
    }

    static JLabel pill(String text, Color col) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", Font.BOLD, 10));
        l.setForeground(col);
        l.setOpaque(true);
        l.setBackground(new Color(col.getRed(), col.getGreen(), col.getBlue(), 22));
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(col.getRed(), col.getGreen(), col.getBlue(), 80)),
                BorderFactory.createEmptyBorder(3, 9, 3, 9)));
        return l;
    }

    static JLabel sectionLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", Font.BOLD, 9));
        l.setForeground(new Color(50, 60, 80));
        l.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    static JLabel cfgKey(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(C_MUTED);
        return l;
    }

    static JLabel cfgVal(String t, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Monospaced", Font.BOLD, 11));
        l.setForeground(c);
        return l;
    }

    static void setActiveButton(int activeIdx) {
        for (int i = 0; i < sideButtons.length; i++) {
            JButton b = sideButtons[i];

            if (i == activeIdx) {
                b.putClientProperty("active", Boolean.TRUE);
                b.setBackground(C_ACTIVE);
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(C_ACCENT.getRed(), C_ACCENT.getGreen(), C_ACCENT.getBlue(), 80)),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            } else {
                b.putClientProperty("active", Boolean.FALSE);
                b.setBackground(C_SURFACE);
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 0, 0, 0)),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            }
        }
    }

    static class FaultHitChart extends JPanel {
        String[] status;

        FaultHitChart(String[] status) {
            this.status = status;
            setBackground(C_BG);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int count = Math.min(n, status.length);
            int barWidth = Math.max(4, (width - 20) / count - 3);
            int startX = 10;

            for (int i = 0; i < count; i++) {
                int x = startX + i * (barWidth + 3);

                // Null-safe check to avoid error if unused status cells exist.
                boolean fault = "FAULT".equals(status[i]);

                if (fault) {
                    g2.setColor(new Color(252, 100, 100, 200));
                } else {
                    g2.setColor(new Color(72, 199, 142, 200));
                }

                int barHeight = fault ? height - 10 : (height - 10) / 2;
                g2.fillRoundRect(x, height - barHeight - 4, barWidth, barHeight, 3, 3);
            }
        }
    }
}
