//import mratool.System.out;

import java.util.Arrays;

/// FROM: https://www.geeksforgeeks.org/print-all-permutations-with-repetition-of-characters/

class AllCombinationsWithRepeats {

    private static String allCombis[];
    private static int count = 0;
    private static int numberCombis;

    public static int getNumberCombis() {
        return numberCombis;
    }

    private static void allLexicographicRecur(String str, char[] data, int last, int index) {

        int length = str.length();

        for (int i = 0; i < length; i++) {
            data[index] = str.charAt(i);
            if (index == last) {
                allCombis[count++] = new String(data);
            } else {
                allLexicographicRecur(str, data, last, index + 1);
            }
        }
    }

    private static void allLexicographic(String str) {

        int length = str.length();

        char[] data = new char[length];
        char[] temp = str.toCharArray();

        Arrays.sort(temp);
        str = new String(temp);

        allLexicographicRecur(str, data, length - 1, 0);
    }

    static String[] getAllCombinations(int rows) {

        String str = "";
        for (int i = 0; i < rows; i++) {
            str += i;
        }

        count = 0;
        numberCombis = (int) Math.pow(str.length(), str.length());
        allCombis = new String[numberCombis];

        allLexicographic(str);

        return allCombis;
    }

    /// main for testing
    public static void main(String[] args) {

        String[] all = getAllCombinations(3);

        for (String string : allCombis) {
//            System.out.println(string);
        }
    }
}