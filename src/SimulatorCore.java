import mratools.MTools;

import java.io.File;
import java.io.FileNotFoundException;

public class SimulatorCore implements IRunner {

    private static Grapher grapher = null;
    static int numThreads = 10;
    private static long globalStartTime;
    private static double[][] parameterVariations;
    private static int numParameterVaried;
    private static String[][] variations;
    private static int totalNumSimulations;
    private static int numParameterVariationSettings;
    private final String simulationName;
    private Thread thread;
    private static int readyCount = 0;

    private static int actualVariation = 1;
    private static int totalSimulationCount = 0;

    private final static int NUM_INDIVIDUALS = 0;
    private final static int INFECTION_PROB = 1;
    private final static int RECOVER_TIME = 2;
    private final static int QARANTINE_PROB = 3;
    private final static int QUARANTINE_TIME = 4;

    private static String[] parameterNames;
    private static double averageSimusPerSecond = 0.0;
    private static int averageSimusCounter = 0;
//    private long duration;

    /// constructor
    public SimulatorCore(String name) {

        simulationName = name;
        grapher = new Grapher();

        start(Long.MAX_VALUE);
    }

    private static void printParameterVariations() {

        MTools.println();

        MTools.println("These " + numParameterVaried +
                " parameters are varied in " +
                numParameterVariationSettings +
                " settings - number of all variation combinations: " +
                Combinizer.getNumberAllVariations());

        for (int n = 0; n < parameterVariations.length; n++) {
            MTools.print(parameterNames[n]);
            for (int col = 0; col < parameterVariations[n].length; col++) {
                MTools.print(Util.myFormatter(parameterVariations[n][col], 8, 2));
            }
            MTools.println("");
        }
    }

    private static void createGeneralVariations(int r, int c) {
        Combinizer combinizer = new Combinizer(r, c);
        variations = combinizer.getVariations();
//        combinizer.printVariations();
    }

    private static void createParameterVariations(int numSettingsCols) {

        /// ATTENTION! If changed here search for ADJUST_ALSO
        numParameterVaried = 5;
        parameterNames = new String[numParameterVaried];
        parameterNames[0] = "number individuals:     ";
        parameterNames[1] = "infection probability:  ";
        parameterNames[2] = "recover time:           ";
        parameterNames[3] = "quarantine probability: ";
        parameterNames[4] = "quarantine time:        ";

        /// possible parameters to vary
        PlayGround.worldSize = 160 * PlayGround.scale;
        PlayGround.numIndividuals = (int) (PlayGround.numIndividuals * PlayGround.scale * PlayGround.scale);
        PlayGround.infectionRadius = 5 * PlayGround.scale;

        /// initialize simulation run parameter matrix of these [numParameterVariations] parameters

        double delta;
        parameterVariations = new double[numParameterVaried][numSettingsCols];

        PlayGround.quarantineProbability = 0.01;
        double maxQuarantineProbability = 1.0;
        delta = (maxQuarantineProbability - PlayGround.quarantineProbability) / (double) (numSettingsCols - 1);
        for (int i = 0; i < numSettingsCols; i++) {
            parameterVariations[QARANTINE_PROB][i] = PlayGround.quarantineProbability + (i * delta);
        }

        PlayGround.numIndividuals = 400;
        double maxNumIndividuals = 600;
        delta = (maxNumIndividuals - PlayGround.numIndividuals) / (double) (numSettingsCols - 1);
        for (int i = 0; i < numSettingsCols; i++) {
            parameterVariations[NUM_INDIVIDUALS][i] = (int) (PlayGround.numIndividuals + (i * delta));
        }

        PlayGround.infectionProbability = 0.01;
        double maxInfectionProbability = 1.0;
        delta = (maxInfectionProbability - PlayGround.infectionProbability) / (double) (numSettingsCols - 1);
        for (int i = 0; i < numSettingsCols; i++) {
            parameterVariations[INFECTION_PROB][i] = PlayGround.infectionProbability + (i * delta);
        }

        PlayGround.quarantineTime = 200;
        double maxQuarantineTime = 400;
        delta = (maxNumIndividuals - PlayGround.quarantineTime) / (double) (numSettingsCols - 1);
        for (int i = 0; i < numSettingsCols; i++) {
            parameterVariations[QUARANTINE_TIME][i] = (int) (PlayGround.quarantineTime + (i * delta));
        }

        PlayGround.recoverTime = 500;
        double maxRecoverTime = 700;
        delta = (maxRecoverTime - PlayGround.recoverTime) / (double) (numSettingsCols - 1);
        for (int i = 0; i < numSettingsCols; i++) {
            parameterVariations[RECOVER_TIME][i] = (int) (PlayGround.recoverTime + (i * delta));
        }
    }

    @Override
    public void start(long l) {
        thread = new Thread(this);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    @Override
    public void stop() {
    }

    private static boolean setNextParameterVariation() {

//        MTools.println("");
//        MTools.println("/// next variation: " + actualVariation + " of " + variations.length);

        for (int paramVariation = 0; paramVariation < numParameterVaried; paramVariation++) {

            /// actualVariation starts from 1 NOT 0
            String str = variations[actualVariation - 1][paramVariation];
            int choice = str.indexOf("1");
            double value = parameterVariations[paramVariation][choice];

            /// ADJUST_ALSO
            if (paramVariation == NUM_INDIVIDUALS) {
                PlayGround.numIndividuals = (int) value;
            }
            if (paramVariation == INFECTION_PROB) {
                PlayGround.infectionProbability = value;
            }
            if (paramVariation == QARANTINE_PROB) {
                PlayGround.quarantineProbability = value;
            }
            if (paramVariation == QUARANTINE_TIME) {
                PlayGround.quarantineTime = (int) value;
            }
            if (paramVariation == RECOVER_TIME) {
                PlayGround.recoverTime = (int) value;
            }

//            MTools.println(str + " paramVariation: " + paramVariation + " choice: " + choice + " value: " + value);
        }
        actualVariation++;

        return actualVariation > variations.length;
    }

    private static void testNextParameterField() {
        for (int i = 0; i < variations.length; i++) {
            setNextParameterVariation();
        }
    }

    @Override
    public void run() {

        for (int run = 0; run < PlayGround.numSimulations; run++) {

            totalSimulationCount++;
            CoronaWorld cw = new CoronaWorld();

            while (cw.allImmune() > 0) {
                cw.oneInfectionStep();
            }
            grapher.add(cw.getDistributions());
        }

        readyCount++;

        if (readyCount >= numThreads) {

            handleReady();
        }
    }

    private void handleReady() {

        readyCount = 0;

        printAndWriteProgress();

        if (setNextParameterVariation()) {
            double val = 1000 * (averageSimusPerSecond / (double) averageSimusCounter);
            MTools.println("average simulations per second: " + Util.myFormatter(val, 5, 2));
            sendReadyEmail();
            return; // all work is done
        }

        startAllSimulations();
    }

    private void sendReadyEmail() {
        SimpleEmailer emailer = new SimpleEmailer();
        String message = "Ready at: " + Util.getTimeStringNow(System.currentTimeMillis());
        try {
            emailer.sendEmail("cloud.transinsight.com",
                    "malvers@transinsight.com", "Dr. Michael R. Alvers",
                    "malvers@transinsight.com", "Dr. Michael R. Alvers",
                    "Message " + getClass(), message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getEstimateReadyDateAndTime(long estimate) {

        return Util.getDateString(System.currentTimeMillis() + estimate)
                + " + " + Util.getTimeStringNow(System.currentTimeMillis() + estimate)
                + " [runtime (d:hh:mm:ss) " + Util.millisToTimeString(estimate) + "]";
    }

    private static void printTimeNow() {

        MTools.println("Date & time now:                "
                + Util.getDateString(System.currentTimeMillis())
                + " + " + Util.getTimeStringNow(System.currentTimeMillis()));
    }

    private void printAndWriteProgress() {

        long globalDuration = System.currentTimeMillis() - globalStartTime;
        int sims = PlayGround.numSimulations * numThreads;

        double simusPerMilliSecond = (double) totalSimulationCount / (double) (globalDuration);

        averageSimusPerSecond += simusPerMilliSecond;
        averageSimusCounter++;

        String sDuration = Util.millisToTimeString(globalDuration);

        String tns = "" + totalNumSimulations;
        int toGo = totalNumSimulations - totalSimulationCount;
        long timeToGo = (long) (toGo / simusPerMilliSecond);

        MTools.println(Util.getDateString(System.currentTimeMillis())
                + " + " + Util.getTimeStringNow(System.currentTimeMillis())
                + " - " + Util.myFormatter(totalSimulationCount, tns.length())
                + " | togo: " + Util.myFormatter(toGo, tns.length())
                + " | elapsed: " + sDuration
                + " | simus/s " + Util.myFormatter(1000 * simusPerMilliSecond, 5, 2)
                + " - ready: " + getEstimateReadyDateAndTime(timeToGo));

        grapher.calcAverageInfectionCurve();
        grapher.writeData(System.getProperty("user.home") + File.separator + "CoronaSimulationData");
    }

    private static void startAllSimulations() {
//        MTools.println( "start - num threads: "  + numThreads);
        for (int i = 0; i < numThreads; i++) {
            String sThread = "" + i;
            if (i < 10) {
                sThread = "0" + i;
            }
            new SimulatorCore("thread: " + i);
        }
    }

    private static long calculateEstimate() {

        totalNumSimulations = numThreads * PlayGround.numSimulations * variations.length;

        // realistic value for simulations per second on MacBook PRO i7 2.8 GHz 8 cores
        double experience = 9.8;

        return 1000 * (long) ((double) totalNumSimulations / (double) experience);
    }

    /// main for running
    public static void main(String[] args) throws FileNotFoundException {

        globalStartTime = System.currentTimeMillis();
        averageSimusPerSecond = 0.0;
        averageSimusCounter = 0;

        numThreads = 10;
        /// take PlayGround.numSimulations times numThreads to get the total number of runs !!!
        PlayGround.numSimulations = 1;

        numParameterVariationSettings = 2;
        createParameterVariations(numParameterVariationSettings);
        createGeneralVariations(numParameterVaried, numParameterVariationSettings);

        long estimate = calculateEstimate();

        String path = System.getProperty("user.home")
                + File.separator + "CoronaSimulationData"
                + File.separator + "simulation protocol "
                + Util.getDateString(globalStartTime)
                + "-" + Util.getTimeStringNow(globalStartTime)
                + ".txt";

        MTools.println("path: " + path);
        MTools.init(path, false);

        MTools.println("Total number of simulations:    " + totalNumSimulations);
        printParameterVariations();

        MTools.println();
        printTimeNow();

        MTools.println("Ready at date & time estimate:  " + getEstimateReadyDateAndTime(estimate));

        MTools.println();

        startAllSimulations();
    }
}



