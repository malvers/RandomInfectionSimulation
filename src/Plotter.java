import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Plotter extends JPanel {

    private ArrayList<ArrayList> allDistributions = new ArrayList<>();
    private double[][] data;
    private int maxTimeSteps;
    private int numberCurves;

    public Plotter() throws IOException {

        readData();
    }

    private void readData() throws IOException {

        Charset charset = Charset.forName("US-ASCII");
        String name = "cw 10000 in 500 rt 600 ip 0.01 ws 160.0 is 4.0 qp 0.0 qt 300 sf 1.0.txt";
        Path p = Paths.get(name);
        String line = null;
        BufferedReader reader = Files.newBufferedReader(p, charset);

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
