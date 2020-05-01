public class MyCombinationsWithRepeats {

    private static String[] allCombis;

    static String[] get(final int positions, final int numLetters) {

        int numberCombis = 0;
        while (true) {

            String str = Integer.toString(numberCombis, numLetters);
            if (str.length() > positions) {
                break;
            }
            numberCombis++;
        }

        allCombis = new String[numberCombis];

        numberCombis = 0;
        while (true) {

            String str = Integer.toString(numberCombis, numLetters);
            if (str.length() < positions) {
                while (str.length() < positions) str = "0" + str;
            }
            if (str.length() > positions) {
                break;
            }
            allCombis[numberCombis] = str;
            numberCombis++;
        }
        return allCombis;
    }

    /// main for testing
    public static void main(String[] args) {
        get(6, 3);
    }
}
