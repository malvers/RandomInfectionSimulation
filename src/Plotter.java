import mratools.MTools;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Plotter extends JPanel {

    private ArrayList<ArrayList> allDistributions = new ArrayList<>();
    private double[][] data;
    private int maxTimeSteps;
    private int numberCurves;

    public Plotter() throws IOException {

        readData();
    }

    private List<String> listFiles() {


        List<String> files = new ArrayList<>();
        String pathToData = System.getProperty("user.home") + File.separator + "CoronaSimulationData";

        File folder = new File(pathToData);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {

                String name = pathToData + File.separator + listOfFiles[i].getName();
                if (name.endsWith(".simu")) {
                    files.add(name);
                    MTools.println(i + " name: " + name);
                }
            }
        }
        return files;
    }

    private void readData() throws IOException {

        Charset charset = Charset.forName("US-ASCII");

        List<String> files = listFiles();

        for ( String name  : files ) {

            String line;
            BufferedReader reader = Files.newBufferedReader(Paths.get(name), charset);

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("//")) {
                    continue;
                } else {
                    break;
                }
            }

            numberCurves = Integer.parseInt(line);
            line = reader.readLine();
            maxTimeSteps = Integer.parseInt(line);

            data = new double[maxTimeSteps][numberCurves * 4];

            int lineCount = 0;
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("/")) {
                    continue;
                }
                lineCount++;
                StringTokenizer tok = new StringTokenizer(line);

                int di = 0;
                while (tok.hasMoreElements()) {
                    data[lineCount][di++] = Double.parseDouble(tok.nextToken());
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {

        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        double y;
        double factor = getWidth() / (double) maxTimeSteps;

        for (int i = 0; i < maxTimeSteps; i++) {

            double x = i * factor;

            int di = 0;
            int to = numberCurves;
            for (int j = 0; j < to; j++) {

                double infected = data[i][di++];
                double immune = data[i][di++];
                double susceptible = data[i][di++];

                g2d.setColor(Color.GRAY);
                y = infected * getHeight();
                if (j == (to - 1)) {
                    g2d.setColor(Color.RED);
                }
                g2d.fill(new Rectangle.Double(x, getHeight() - y, 1.0, 1.0));
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 600);
    }

    public static void main(String[] args) throws IOException {
        JFrame f = new JFrame();
        f.add(new Plotter());
        f.setBounds(10, 10, 400, 400);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
