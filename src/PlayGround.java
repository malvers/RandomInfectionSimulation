import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;

public class PlayGround extends JPanel {

    public static double quarantineProbability = 0.0;
    public static int quarantineTime = 300;
    public static int recoverTime = 600;
    static int numSimulations = 4000;
    static double scale = 1.0;
    private final Grapher grapher;
    static double infectionProbability = 0.01;
    private ArrayList<CoronaWorld> coronaWorlds = new ArrayList<>();
    private int doneCounter = 0;
    private static long startTime;

    static double worldSize = 160;
    static double move = worldSize * 0.3;
    final static double moveHalf = move / 2.0;

    static double infectionRadius = 4;
    static int numIndividuals = 500;
    private final static int randomLimit = 100000;
    private final static double[] randomList = new double[randomLimit];
    private static int randomCounter = 0;

    public PlayGround() {

        initRandom();

        System.out.println("[" + getClass() + "]->ip: " + infectionProbability);

        for (int i = 0; i < numSimulations; i++) {
            coronaWorlds.add(new CoronaWorld(this));
        }
        setLayout(new GridLayout(1, 2));
        JPanel simu = new JPanel();
        simu.setLayout(new GridLayout(4, 2, 0, 0));
        for (CoronaWorld cw : coronaWorlds) {
            simu.add(cw);
        }
        grapher = new Grapher();
        grapher.setGlobalStatistics(0, 0, 0, 0, infectionProbability, numSimulations, "00:00:00");

        removeAll();
        JSplitPane split = new JSplitPane();
        split.setDividerLocation(640);
        split.setLeftComponent(simu);
        split.setRightComponent(grapher);
        add(split);
    }

    /// constructor
    private void initRandom() {
        for (int i = 0; i < randomList.length; i++) {
            randomList[i] = Math.random() * move - moveHalf;
        }
    }

    static double getNextRandom() {

        randomCounter++;
        if (randomCounter >= randomLimit) {
            randomCounter = 0;
        }
        if (randomCounter >= randomLimit) {
            System.out.println("randomCounter: " + randomCounter + " rl: " + randomLimit);
        }
        return randomList[randomCounter];
    }

    void deactivateAll() {
        for (CoronaWorld cw : coronaWorlds) {
            cw.setActive(false);
        }
    }

    public void startStopAll() {
        startTime = System.currentTimeMillis();
        for (CoronaWorld cw : coronaWorlds) {
            cw.startStop();
        }
    }

    public void resetAll() {
        for (CoronaWorld cw : coronaWorlds) {
            cw.initIndividuals();
        }
    }

    public void done() {

        doneCounter++;
        if (doneCounter >= numSimulations) {
            statistics("duration " + Util.getTimeStringNow(System.currentTimeMillis() - startTime));
        }
    }

    public void statistics(String timeString) {

        System.out.println("[" + getClass() + "]->statistics: ");
        int maxGeneration = 0;
        int minGeneration = Integer.MAX_VALUE;
        int maxInfected = 0;
        int minInfected = Integer.MAX_VALUE;

        for (CoronaWorld cw : coronaWorlds) {

            grapher.add(cw.getDistributions());

            if (cw.getMaxGeneration() > maxGeneration) {
                maxGeneration = cw.getMaxGeneration();
            }
            if (cw.getMaxGeneration() < minGeneration) {
                minGeneration = cw.getMaxGeneration();
            }
            if (cw.getMaxInfected() > maxInfected) {
                maxInfected = cw.getMaxInfected();
            }
            if (cw.getMaxInfected() < minInfected) {
                minInfected = cw.getMaxInfected();
            }
        }
        System.out.println("[" + getClass() + "]->maxGeneration: " + maxGeneration);
        System.out.println("[" + getClass() + "]->minGeneration: " + minGeneration);
        System.out.println("[" + getClass() + "]->maxInfected:   " + maxInfected);
        System.out.println("[" + getClass() + "]->minInfected:   " + minInfected);

        grapher.calcAverageInfectionCurve();
        grapher.setGlobalStatistics(
                maxGeneration,
                minGeneration,
                maxInfected,
                minInfected,
                infectionProbability,
                numSimulations,
                timeString);
        grapher.savePaneImage(quarantineProbability * 100 + "% "
                + " qt " + quarantineTime
                + " ms " + infectionRadius
                + " cw " + numSimulations
                + " ip " + infectionProbability + "_corona_simu.png");
//        grapher.saveHistogramImage(CoronaPlayGround.quarantineProbability*100 + "% " + mysize + " " + numberCoronaWorlds + "_" + infectionProbability + "_corona_simu_histogram.png");
        repaint();
    }

    /// main for testing
    public static void main(String[] args) {

        scale = 1.0;
        worldSize = 160 * scale;
        numIndividuals = 500;
        numIndividuals = (int) (numIndividuals * scale * scale);
        infectionRadius = 5 * scale;
        quarantineProbability = 0.00;
        quarantineTime = 300;
        infectionProbability = 0.03;
        numSimulations = 10;

        PlayGround cpg = new PlayGround();

        double density = (worldSize * worldSize) / numIndividuals;
        System.out.println("population density: " + density);

        JFrame f = new JFrame();
        f.add(cpg);
        f.setVisible(true);
        Dimension sz = Toolkit.getDefaultToolkit().getScreenSize();
        f.setBounds(0, 0, sz.width, 640);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        f.setTitle("still waiting ... ");
        /// wait 5 seconds to start
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        f.setTitle("now running ... ");
//        cpg.startStopAll();
    }
}
