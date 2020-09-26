import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Klasse extends ArrayList<OneSchueler> {

    int id;
    long highScore;
    static String[] klassenString;

    public Klasse(int idIn) {

        super(16);

        initKlassenId();

        highScore = Long.MAX_VALUE;

        id = idIn;

        File file = null;
        File fileHighScore = null;

        file = new File("Klasse" + klassenString[id] + ".txt");
        fileHighScore = new File("Klasse" + klassenString[id] + "_HighScore.txt");

        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.startsWith("//")) {
                continue;
            }
            add(new OneSchueler(line));
        }

        /// read high scores
        try {
            sc = new Scanner(fileHighScore);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.length() == 0) {
                continue;
            }
            highScore = Long.parseLong(line);
        }
    }

    private void initKlassenId() {
        klassenString = new String[3];
        klassenString[0] = "9b";
        klassenString[1] = "9a";
        klassenString[2] = "8";
    }

    public Klasse() {
        initKlassenId();
    }

    public int getNumberTasks() {

        int sum = 0;
        for (int i = 0; i < size(); i++) {
            OneSchueler osch = getSchueler(i);
            if (osch.anwesend) {
                sum += osch.getNumberTasks();
            }
        }
        return sum;
    }

    @Override
    public OneSchueler get(int index) {
//        MTools.println( "get -> call getSchueler!" );
        return getSchueler(index);
    }

    public OneSchueler getSchueler(int index) {
        return super.get(index);
    }

    void print() {
        for (int i = 0; i < size(); i++) {
            get(i).print();
        }
    }

    public void writeHighScore() throws IOException {

        File file = null;
        file = new File("Klasse" + klassenString[id] + "_HighScore.txt");

        Writer writer = new OutputStreamWriter(new FileOutputStream(file, true));
        try {
            writer.append("" + highScore + "\n");
        } finally {
            writer.close();
        }
    }
}
