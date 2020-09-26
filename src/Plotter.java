import mratools.MTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Plotter extends JPanel {

    private boolean drawCurves = false;
    private boolean dragging = false;

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

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                dragging = false;
            }
        });

        addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                dragging = true;
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
                    readAndRepaint();
                }
                else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    decSelectedFileId();
                    readAndRepaint();
                }
                else if (e.getKeyCode() == KeyEvent.VK_C) {
                    drawCurves = !drawCurves;
                    repaint();
                }
            }
        });

        readData("/Users/malvers/CoronaSimulationData/ns 10 in 400 rt 700 ip 100.00[%] ws 160.0 is 5.0 qp  1.00[%] qt 200 sf 1.0.simu");
    }

    private void readAndRepaint() {
        try {
            readData(files.get(selectedFileId));
            repaint();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
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
                if (name.endsWith(".simu") && name.contains("ns 3")) {
                    files.add(name);
//                    MTools.println(i + " name: " + name);
                }
            }
        }
        return files;
    }

    void readData(String name) throws IOException {

        f.setTitle(name);
//        MTools.println("read: " + name);

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
        calculateMinMax();
        repaint();
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
                double immune = data[i][di++];
                double susceptible = data[i][di++];

                if (infected < 0) {
                    continue;
                }
                double delta = average - infected;
                minmaco.var += (delta) * (delta);
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
            minmaco.var /= numberCurves - 1;
//            MTools.println("var: " + minmaco.var);
            minmaco.average = average;
            minmaco.min = min;
            minmaco.max = max;
            minmaco.minAv /= minCount;
            minmaco.maxAv /= maxCount;
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
            for (int j = 0; j < numberCurves-1; j++) {

                double infected = data[i][di++];
                double immune = data[i][di++];
                double susceptible = data[i][di++];

                if (infected < 0) {
                    continue;
                }
                if (!drawCurves && j < numberCurves - 1) {
                    continue;
                }

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
        drawMinMaxCo(g2d, factor);

        handleDrag(g2d, factor);
    }

    private void drawMinMaxCo(Graphics2D g2d, double factor) {

        int height = getHeight();
        double y1, y2;
        for (int i = 0; i < minMaxCo.size() - 1; i++) {

            MinMaxCo mimaco1 = minMaxCo.get(i);
            MinMaxCo mimaco2 = minMaxCo.get(i + 1);
            double x1 = i * factor;
            double x2 = (i + 1) * factor;
            y1 = mimaco1.minAv * height;
            y2 = mimaco2.minAv * getHeight();
            g2d.setColor(Color.BLUE.darker());
//            g2d.fill(new Rectangle.Double(x1, height - y1, 1.6, 1.6));
            g2d.draw(new Line2D.Double(x1, height - y1, x2, height - y2));
            y1 = mimaco1.maxAv * getHeight();
            y2 = mimaco2.maxAv * getHeight();
            g2d.setColor(Color.GREEN.darker());
//            g2d.fill(new Rectangle.Double(x1, height - y1, 1.6, 1.6));
            g2d.draw(new Line2D.Double(x1, height - y1, x2, height - y2));

            y1 = mimaco1.min * getHeight();
            y2 = mimaco2.min * getHeight();
            g2d.setColor(Color.BLUE);
            g2d.draw(new Line2D.Double(x1, height - y1, x2, height - y2));
//            g2d.fill(new Rectangle.Double(x1, height - y1, 1.6, 1.6));
            y1 = mimaco1.max * getHeight();
            y2 = mimaco2.max * getHeight();
            g2d.setColor(Color.GREEN);
            g2d.draw(new Line2D.Double(x1, height - y1, x2, height - y2));
//            g2d.fill(new Rectangle.Double(x1, height - y1, 1.6, 1.6));

            y1 = (mimaco1.var) * height * 40;
            y2 = (mimaco2.var) * height * 40;
            g2d.setColor(Color.DARK_GRAY);
            g2d.draw(new Line2D.Double(x1, height - y1, x2, height - y2));
//            g2d.fill(new Rectangle.Double(x1, height - y1, 1.6, 1.6));

            y1 = (mimaco1.average) * height;
            y2 = (mimaco2.average) * height;
            g2d.setColor(Color.RED);
            g2d.draw(new Line2D.Double(x1, height - y1, x2, height - y2));
//            g2d.fill(new Rectangle.Double(x1, height - y1, 1.6, 1.6));
        }
    }

    private void handleDrag(Graphics2D g2d, double factor) {

        if( !dragging ) return;

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
        f.setLayout(new BorderLayout());
        Plotter plotter = new Plotter();
        SliderPanel sliderPanel = new SliderPanel(plotter);
        f.add(BorderLayout.WEST, sliderPanel);
        f.add(BorderLayout.CENTER, plotter);
        f.setBounds(10, 10, 1440, 500);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
