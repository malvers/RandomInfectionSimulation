import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Grapher extends JPanel {

    private double maxGeneration = 0;
    private ArrayList<ArrayList> allDistributions = new ArrayList<>();
    private ArrayList<Distribution> average = null;
    private int minGeneration = 0;
    private int minInfected = 0;
    private int maxInfected;
    private double infectionProbability;
    private double numberCoronaWorlds;
    private String timeString;

    public void add(ArrayList<Distribution> d) {

        allDistributions.add(d);
        if (d.size() > maxGeneration) {
            maxGeneration = d.size();
        }
    }

    void calcAverageInfectionCurve() {

        if (average == null) {
            average = new ArrayList<>();
        }

        for (int i = 0; i < maxGeneration; i++) {
            average.add(new Distribution());
        }

        for (ArrayList curve : allDistributions) {

            for (int i = 0; i < curve.size(); i++) {

                Distribution actDis = (Distribution) curve.get(i);
                average.get(i).infected += actDis.infected;
                average.get(i).immune += actDis.immune;
                average.get(i).susceptible += actDis.susceptible;
            }
        }
        for (int i = 0; i < average.size(); i++) {
            average.get(i).infected /= (double) allDistributions.size();
            average.get(i).immune /= (double) allDistributions.size();
            average.get(i).susceptible /= (double) allDistributions.size();
        }
        allDistributions.add(average);
    }

//    void saveHistogramImage(String name) {
//
//        int collectSize = 3500;//(int) maxGeneration;
//        MImage collectImage = new MImage(collectSize, collectSize);
//        collectImage.initGray(255);
//
//        double width = collectImage.getWidth();
//        double height = collectImage.getHeight();
//
//        int xHisto;
//        int yHisto;
//        int window = 10;
//        int histoSize = collectSize / window;
//        int[][] average = new int[histoSize][histoSize];
//        double max = 0.0;
//
//        /// loop all curves
//        for (int j = 0; j < allDistributions.size() - 1; j++) {
//
//            /// loop all distributions per curve
//            var curve = allDistributions.get(j);
//
//            for (int i = 0; i < curve.size(); i++) {
//
//                Distribution e = (Distribution) curve.get(i);
//
//                double x = (width * i) / maxGeneration;
//                double y = height - e.infected * height;
//
//                xHisto = (int) (0.1 * (double) x);
//                yHisto = (int) (0.1 * (double) y);
//                average[xHisto][yHisto]++;
//
//                if (average[xHisto][yHisto] > max) {
//                    max = average[xHisto][yHisto];
//                }
//
//                collectImage.addToPixel((int) x, (int) y, -1);
//            }
//        }
//
//        writeData(width, height);
//
//        System.out.println("max: " + max);
//        collectImage.scaleToFullRange();
//        collectImage.write(name);
//
//        MImage histogramImage = new MImage(histoSize, histoSize);
//        for (xHisto = 0; xHisto < histoSize; xHisto++) {
//            for (yHisto = 0; yHisto < histoSize; yHisto++) {
//                double v = 255 - (255 * ((average[xHisto][yHisto]) / max));
//                histogramImage.setPixel(xHisto, yHisto, (int) v);
//            }
//        }
//        histogramImage.write("sampled_" + name);
//    }

    void writeData(String path) {

        int max = 0;
        int numCurves = allDistributions.size();
        for (int j = 0; j < numCurves; j++) {
            var curve = allDistributions.get(j);
            if (curve.size() > max) {
                max = curve.size();
            }
        }
        String fileName = path + "/"
                + "ns " + (PlayGround.numSimulations * SimulatorCore.numThreads)
                + " in " + PlayGround.numIndividuals
                + " rt " + PlayGround.recoverTime
                + " ip " + Util.myFormatter(PlayGround.infectionProbability * 100, 5, 2) + "[%]"
                + " ws " + PlayGround.worldSize
                + " is " + PlayGround.infectionRadius
                + " qp " + Util.myFormatter(PlayGround.quarantineProbability * 100, 5, 2) + "[%]"
                + " qt " + PlayGround.quarantineTime
                + " sf " + PlayGround.scale
                + ".txt";

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(fileName);

            pw.write("// " + fileName + "\n");
            pw.write("//  ns - number simulations:     " + PlayGround.numSimulations * SimulatorCore.numThreads + "\n");
            pw.write("//  in - number individuals:     " + PlayGround.numIndividuals + "\n");
            pw.write("//  rt - number individuals:     " + PlayGround.recoverTime + "\n");
            pw.write("//  ip - infection probability:  " + PlayGround.infectionProbability + "\n");
            pw.write("//  ws - world size:             " + PlayGround.worldSize + "\n");
            pw.write("//  is - individual size:        " + PlayGround.infectionRadius + "\n");
            pw.write("//  qp - quarantine probability: " + PlayGround.quarantineProbability + "\n");
            pw.write("//  qt - quarantine time:        " + PlayGround.quarantineTime + "\n");
            pw.write("//  sf - scale factor:           " + PlayGround.scale + "\n");
            pw.write("//  following two lines: 1. cw + 1 for average 2. maximal number steps\n");
            pw.write("//  format below: cw * infected, immune, susceptible\n");

            pw.write(SimulatorCore.numThreads * PlayGround.numSimulations + 1 + "\n");
            pw.write((max + 1) + "\n");

            for (int step = 0; step < max; step++) {

                double[] data = new double[numCurves * 3];
                int di = 0;
                for (int dis = 0; dis < numCurves; dis++) {

                    var curve = allDistributions.get(dis);

                    if (step < curve.size()) {
                        Distribution e = (Distribution) curve.get(step);
                        data[di] = e.infected;
                        data[di + 1] = e.immune;
                        data[di + 2] = e.susceptible;
                    } else {
                        data[di] = -1;
                        data[di + 1] = -1;
                        data[di + 2] = -1;
                    }
                    di += 3;
                }
//            System.out.print(step + ", ");

//            pw.write(step + "  ");

                int to = data.length;
                for (int ds = 0; ds < to; ds++) {
//                System.out.print(data[ds] + ", ");
                    pw.write(data[ds] + " ");
                }
//            System.out.println();
                pw.write("\n");

            }
            pw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

//    private static MImage scaleImage(int collectSize, BufferedImage collectImage) {
//
//        int window = 10;
//        int histoSize = collectSize / window;
//        MImage histogramImage = new MImage(histoSize, histoSize);
//        histogramImage.initGray(255);
//        int xHisto = 0;
//        int yHisto = 0;
//        int average[][] = new int[histoSize][histoSize];
//        double max = 0;
//        try {
//            double m = (double) histoSize / (double) collectSize; // = 0.1
//
//            for (int xCollect = 0; xCollect < collectSize; xCollect++) {
//                for (int yCollect = 0; yCollect < collectSize; yCollect++) {
//
//                    Color c = new Color(collectImage.getRGB(xCollect, yCollect));
//
//                    xHisto = (int) (m * (double) xCollect);
//                    yHisto = (int) (m * (double) yCollect);
//                    int red = c.getRed();
//                    average[xHisto][yHisto] += red;
//
//                    if (average[xHisto][yHisto] > max) {
//                        max = average[xHisto][yHisto];
//                    }
//                }
//            }
//            System.out.println("max: " + max);
//
//            for (xHisto = 0; xHisto < histoSize; xHisto++) {
//                for (yHisto = 0; yHisto < histoSize; yHisto++) {
//                    double v = 255 * ((average[xHisto][yHisto]) / max);
////                    System.out.println("x: " + xHisto + " y: " + yHisto + " v: " + (int) v);
//                    histogramImage.setPixel(xHisto, yHisto, (int) v);
//                }
//            }
//
//        } catch (
//                Exception e) {
//            e.printStackTrace();
//        }
//        return histogramImage;
//    }

//    private static MImage colorImage(BufferedImage img) {
//
//        MImage coloredImage = new MImage(img.getWidth(), img.getHeight());
//        for (int xHisto = 0; xHisto < img.getWidth(); xHisto++) {
//            for (int yHisto = 0; yHisto < img.getHeight(); yHisto++) {
//                int colorInt = img.getRGB(xHisto, yHisto);
//                Color original = new Color(colorInt);
//                int r = original.getRed();
//                Color newColor = ColorGradient.myGetColor(r, 255);
//                coloredImage.setPixel(xHisto, yHisto, newColor);
//            }
//        }
//        return coloredImage;
//    }

    @Override
    public void paint(Graphics g) {

        super.paint(g);

        int width = getWidth();
        int height = getHeight();

        myPaint((Graphics2D) g, width, height);
    }

    private void myPaint(Graphics2D g2d, int width, int height) {

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);
        g2d.fill(new Rectangle2D.Double(0, 0, width, height));

        g2d.setColor(Color.GRAY);

        String text;
        int ypos = 20;
        int xpos = width - 400;
        int inc = 20;

        ypos += inc;
        text = "parallel coronaverses: ";
        g2d.drawString(text + (int) numberCoronaWorlds, xpos, ypos);

        ypos += 2 * inc;
        text = "infection propability: ";
        g2d.drawString(text + (infectionProbability * 100) + " %", xpos, ypos);

        ypos += 2 * inc;
        text = "infected individuals - max: " + maxInfected + " min: " + minInfected + " [of 500]";
        g2d.drawString(text, xpos, ypos);

        ypos += 2 * inc;
        text = "generations until through - max: " + (int) maxGeneration + " min: " + minGeneration;
        g2d.drawString(text, xpos, ypos);

        ypos += 2 * inc;
        text = "runtime: " + timeString;
        g2d.drawString(text, xpos, ypos);

        ypos += 2 * inc;
        text = "individual size: " + PlayGround.infectionRadius;
        g2d.drawString(text, xpos, ypos);

        ypos += 2 * inc;
        text = "scale: " + PlayGround.scale;
        g2d.drawString(text, xpos, ypos);

        ypos += 2 * inc;
        text = PlayGround.quarantineProbability * 100 + "% infected quarantined after " + PlayGround.quarantineTime + " steps (half time)";
        g2d.drawString(text, xpos, ypos);

        for (int j = 0; j < allDistributions.size(); j++) {

            ArrayList curve = allDistributions.get(j);

            if (j == allDistributions.size() - 1) {
                g2d.setColor(Color.RED);
            }

            for (int i = 0; i < curve.size(); i++) {

                Distribution e = (Distribution) curve.get(i);

//                double x = (width * i) / maxGeneration;
                double x = (width * i) / 3500;
                double y = (e.infected * (double) height);

                g2d.fill(new Rectangle2D.Double(x, height - y, 1, 1));
            }
        }
    }

    public void setGlobalStatistics(int maxg, int ming, int maxi, int mini, double ip, double num, String ts) {

        numberCoronaWorlds = num;
        infectionProbability = ip;
        maxGeneration = maxg;
        minGeneration = ming;
        minInfected = mini;
        maxInfected = maxi;
        timeString = ts;
    }

    public void savePaneImage(String name) {

        int h = 1000;
        int w = (int) (h * Math.sqrt(2.0));
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        myPaint((Graphics2D) g, w, h);
        try {
            ImageIO.write(image, "png", new File(name));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {

        try {
            String name = "test3.png";
            BufferedImage img = ImageIO.read(new File(name));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
