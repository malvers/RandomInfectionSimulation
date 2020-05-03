import mratools.MTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Plotter extends JPanel {

    class MinMaxCo {

        double min = +Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double average;
        double minAv;
        double maxAv;
        double var;
    }

    private static JFrame f;
    private final List<String> files;
    private double[][] data;
    private ArrayList<MinMaxCo> minMaxCo;
    private int maxTimeSteps;
    private int numberCurves;
    private int selectedFileId;
    private int dragPosition;

    public Plotter() throws IOException {

        selectedFileId = 0;
        setFocusable(true);

        files = listFiles();

        addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                dragPosition = e.getX();
                repaint();
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    incSelectedFileId();
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    decSelectedFileId();
                }
                try {
                    readData(files.get(selectedFileId));
                    repaint();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        readData("/Users/malvers/CoronaSimulationData/ns 1000 in 400 rt 700 ip 100.00[%] ws 160.0 is 5.0 qp  1.00[%] qt 200 sf 1.0.simu");
    }

    private void decSelectedFileId() {
        selectedFileId--;
        if (selectedFileId < 0) {
            selectedFileId = files.size() - 1;
        }
    }

    private void incSelectedFileId() {
        selectedFileId++;
        if (selectedFileId >= files.size()) {
            selectedFileId = 0;
        }
    }

    private List<String> listFiles() {

        List<String> files = new ArrayList<>();
        String pathToData = System.getProperty("user.home") + File.separator + "CoronaSimulationData";

        File folder = new File(pathToData);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {

                String name = pathToData + File.separator + listOfFiles[i].getName();
                if (name.endsWith(".simu") && name.contains("ns 1000")) {
                    files.add(name);
//                    MTools.println(i + " name: " + name);
                }
            }
        }
        return files;
    }

    private void readData(String name) throws IOException {

        f.setTitle(name);
        MTools.println("read: " + name);

        minMaxCo = new ArrayList<MinMaxCo>();

        String line;
        BufferedReader reader = Files.newBufferedReader(Paths.get(name));

        line = reader.readLine();

        /// forward comments
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("//")) {
                continue;
            } else {
                break;
            }
        }

        /// read number curves
        numberCurves = Integer.parseInt(line);
        line = reader.readLine();
        // read number time-steps
        maxTimeSteps = Integer.parseInt(line);

        data = new double[maxTimeSteps][numberCurves * 3 + 1]; /// add 1 for average

        int lineCount = 0;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("//")) {
                continue;
            }

            lineCount++;
            StringTokenizer tok = new StringTokenizer(line);

            int di = 0;
            while (tok.hasMoreElements()) {
                data[lineCount][di++] = Double.parseDouble(tok.nextToken());
            }
        }
        MTools.println( "done read ..." );
        calculateMinMax();
        MTools.println( "done read ..." );
    }

    void calculateMinMax() {

        for (int i = 0; i < maxTimeSteps; i++) {

            double average = data[i][(numberCurves - 1) * 3];
            if (average < 0) {
                continue;
            }

            int di = 0;
            int minCount = 0;
            int maxCount = 0;
            double min = Double.MAX_VALUE;
            double max = 0;
            MinMaxCo minmaco = new MinMaxCo();
            minMaxCo.add(minmaco);
            for (int j = 0; j < numberCurves - 1; j++) {

                double infected = data[i][di++];
                double immune = data[i][di++];      // skip unused by ++
                double susceptible = data[i][di++]; // skip unused by ++

                if (infected < 0) {
                    continue;
                }

                if (infected < min) {
                    min = infected;
                }
                if (infected > max) {
                    max = infected;
                }
                if (infected < average) {
                    minmaco.minAv += infected;
                    minCount++;
                }
                if (infected > average) {
                    minmaco.maxAv += infected;
                    maxCount++;
                }
            }
            minmaco.min = min;
            minmaco.max = max;
            minmaco.minAv /= minCount;
            minmaco.maxAv /= maxCount;

//            MTools.println(i +
//                    " min: " + Util.myFormatter(minMax[i][0], 6, 4) +
//                    " av: " + Util.myFormatter(average, 6, 4) +
//                    " max: " + Util.myFormatter(minMax[i][1], 6, 4));
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

        drawGrid(g2d, factor);

        Point last = new Point();
        for (int i = 0; i < maxTimeSteps; i++) {

            double x = i * factor;

            int di = 0;
            double average = data[i][(numberCurves - 1) * 3];
            for (int j = 0; j < numberCurves; j++) {

                double infected = data[i][di++];
                double immune = data[i][di++];
                double susceptible = data[i][di++];

//                if (infected < 0) {
//                    continue;
//                }
//                if (j < numberCurves - 1) {
//                    continue;
//                }

                g2d.setColor(Color.LIGHT_GRAY);
                if (j == numberCurves - 1) {
                    g2d.setColor(Color.RED);
                } else {
                    g2d.setColor(Color.LIGHT_GRAY);
                }
                y = infected * getHeight();
                g2d.fill(new Rectangle.Double(x, getHeight() - y, 1.6, 1.6));
//                g2d.setColor(Color.LIGHT_GRAY);
//                y = immune * getHeight();
//                g2d.fill(new Rectangle.Double(x, getHeight() - y, 1.6, 1.6));
//                g2d.setColor(Color.PINK);
//                y = susceptible * getHeight();
//                g2d.fill(new Rectangle.Double(x, getHeight() - y, 1.6, 1.6));
            }
        }
        drawMinMax(g2d, factor);

//        handleDrag(g2d, factor);
    }

    private void drawMinMax(Graphics2D g2d, double factor) {

        double y;
        for (int i = 0; i < minMaxCo.size(); i++) {
            MinMaxCo mimaco = minMaxCo.get(i);
            double x = i * factor;
            y = mimaco.minAv * getHeight();
            g2d.setColor(Color.BLUE.darker());
            g2d.fill(new Rectangle.Double(x, getHeight() - y, 1.6, 1.6));
            y = mimaco.maxAv * getHeight();
            g2d.setColor(Color.GREEN.darker());
            g2d.fill(new Rectangle.Double(x, getHeight() - y, 1.6, 1.6));

            y = mimaco.min * getHeight();
            g2d.setColor(Color.BLUE);
            g2d.fill(new Rectangle.Double(x, getHeight() - y, 1.6, 1.6));
            y = mimaco.max * getHeight();
            g2d.setColor(Color.GREEN);
            g2d.fill(new Rectangle.Double(x, getHeight() - y, 1.6, 1.6));
        }
    }

    private void handleDrag(Graphics2D g2d, double factor) {

        g2d.setColor(Color.RED);
        g2d.drawLine(dragPosition, 0, dragPosition, getHeight());

        int pos = (int) (dragPosition / factor);
        if (pos < 0) {
            pos = 0;
        }

        int averagePosition = (numberCurves - 1) * 3;
        MTools.println(pos + " av:  " + data[pos][averagePosition] + " nc: " + numberCurves);
        double lav = 0.0;
        int di = 0;
        for (int j = 0; j < numberCurves - 1; j++) {
            double infected = data[pos][di];
            lav += infected;
            di += 3;
        }
        MTools.println("lav: " + lav / (double) (numberCurves - 1));
    }

    private void drawGrid(Graphics2D g2d, double factor) {
        double y;
        g2d.setColor(new Color(230, 230, 230));
        for (int i = 0; i < maxTimeSteps; i += 100) {

            double x = i * factor;
            g2d.drawLine((int) x, 0, (int) x, getHeight());
        }

        for (int i = 0; i < 10; i++) {

            double x = i * factor;
            y = i * getHeight() / 10.0;
            g2d.drawLine(0, (int) y, getWidth(), (int) y);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 600);
    }

    public static void main(String[] args) throws IOException {
        f = new JFrame();
        f.add(new Plotter());
        f.setBounds(10, 10, 800, 600);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
