import mratools.MTools;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Combinizer {

    private int cols;
    private int rows;
    private String[] allCombinations;
    private String[] allPossibilities;
    private static String[][] variations;

    /// constructor
    public Combinizer(int r, int c )  {

        createVariations(r, c);

//        try {
//            writeVariations();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    private void printCombinations() {
        MTools.println("Print combinations ...");
        for (String str : allCombinations) {
            MTools.println(str);
        }
    }

    private void printPossibilities() {
        MTools.println("Print possibilities ...");
        for (String str : allPossibilities) {
            MTools.println(str);
        }
    }

    void createVariations(int r, int c) {

        rows = r; // positions
        cols = c; // size alphabet

        allCombinations = MyCombinationsWithRepeats.get(rows, cols);

//        MTools.println("Create possibilities ...");
        createPossibilities(cols);

//        printPossibilities();
//        printCombinations();


//        MTools.println("Create variations ...");
        variations = new String[allCombinations.length][rows];

        int combiCount = 0;
        for (String combination : allCombinations) {

            for (int i = 0; i < combination.length(); i++) {

                int v = Integer.parseInt(Character.toString(combination.charAt(i)));

                variations[combiCount][i] = allPossibilities[v];
            }
            combiCount++;
        }
    }

    private void writeVariations() throws FileNotFoundException {

//        MTools.println("Write variations ...");

        PrintWriter pw = new PrintWriter(rows + " x " + cols + ".dat");
        pw.write("// rows: " + rows + " cols: " + cols + " number possibilities: " + allCombinations.length + "\n");

        for (String[] var : variations) {

            for (String string : var) {
                pw.write(string + "\n");
            }
            pw.write("\n");
        }
        pw.close();
    }

    void printVariations() {

        MTools.println("Print all " + variations.length + " variations ...");

        for (String[] var : variations) {

            for (String string : var) {
                MTools.println( string);
            }
            MTools.println( "" );
        }
    }

    static int getNumberAllVariations() {
        return variations.length;
    }

    String[][] getVariations() {
        return variations;
    }

    private void createPossibilities(int cols) {

        allPossibilities = new String[cols];

        String[] oneRow = new String[cols];
        for (int i = 0; i < cols; i++) {
            oneRow[i] = "";
            for (int j = 0; j < cols; j++) {

                if (i == j) {
                    oneRow[i] += "1";
                } else {
                    oneRow[i] += "0";
                }
            }
            allPossibilities[i] = oneRow[i];
        }
    }

    /// main for testing
    public static void main(String[] args) {
        new Combinizer(8,3);
    }
}
