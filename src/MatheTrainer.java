import mratools.MTools;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Timer;
import java.util.*;

public class MatheTrainer extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

    private static JFrame frame;
    private static Clip clip;
    private final String sound1 = "sound/6 Minuten Jeopardy Theme Music 5%.wav";
    private final String sound2 = "sound/Madonna - Frozen 10%.wav";
    private String soundOnDisplay = sound1;
    private Timer timer;
    private MyCountDown countDown;

    ArrayList<ColorSheme> allColorSchemes = new ArrayList<>();
    AllTasks allTasks = new AllTasks();
    ArrayList<Klasse> alleKlassen = new ArrayList<>();
    Reihen reihen = new Reihen(10);

    File[][] imagesMatrix = new File[10][10];

    BufferedImage bgImg = null;

    private final int fontSizeStatistics = 30;
    private int numberTasksProSchueler = 6;
    private int colorSchemeId = 0;
    private int fontSizeNumbers = 220;
    private int taskCounter = 0;
    private int iterationCount = 0;

    private final float factorDrawStatistics = 1.4f;
    private float transparency = 0.5f;

    private int actualKlasse = KlassenId.nineB;

    private long timerStart;
    private long deltaT;
    private long finalDeltaT;

    private final String settingsFileName = "MatheTrainer.binary.settings";
    private String pinnedName = "";

    private boolean timeStartIsReseted = false;
    private boolean drawStatistics = false;
    private boolean showDuration = false;
    private boolean beginning = true;
    private boolean drawHelp = false;
    private boolean drawSettings = false;
    private boolean limitedToSelectedSeries = false;
    private boolean debugMode = true;

    private int zeitProAufgabeUndSchueler;
    private int pinnedHighScore = 10000;
    private int countDownCounter = -1;
    private int penalty = 0;
    private boolean shallWriteHighScore = false;
    private boolean drawAufgabe = true;
    private int nextTaskCountDownFrom = 5;
    private int countDownFrom = 9;
    private int nextTaskCountDown = nextTaskCountDownFrom;
    private Timer nextTask;
    private float soundVolume = 1.0f;
    private boolean playMusic = true;

    public MatheTrainer() {

        setFocusable(true);

        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        /// make sure also whe window is closed via X or cmd q all settings are written
        Thread t = new Thread(() -> {
            writeSettings();
            writeHighScores();
        });
        Runtime.getRuntime().addShutdownHook(t);

        readSettings();

        initColors();

        initBeginning();

        setAndPlaySound(sound1);

        setVolume();
    }

    private void initBeginning() {

//        MTools.println("initBeginning:");

        timeStartIsReseted = false;
        beginning = true;
        taskCounter = 0;
        iterationCount = 0;
        countDownCounter = -1;
        penalty = 0;

        showDuration = false;
        drawStatistics = false;
        shallWriteHighScore = false;
        drawHelp = false;
        drawSettings = false;

        if (countDown != null) {
            countDown.cancel();
        }

        readImages();

        initNames(true);
        initAllTasks(true);

        try {
            setImageForTask();
        } catch (IOException e) {
            e.printStackTrace();
        }

        timer = new Timer();
        resetTimerStart();
    }

    private void initNames(boolean shuffle) {

        alleKlassen.clear();

        new Klasse();
        for (int klassenId = 0; klassenId < Klasse.klassenString.length; klassenId++) {
            alleKlassen.add(new Klasse(klassenId));
        }
        if (shuffle) {
            Collections.shuffle(alleKlassen.get(actualKlasse));
        }
    }

    private void initAllTasks(boolean shuffle) {

        allTasks.clear();
        for (int i = 0; i < numberTasksProSchueler; i++) {

            Klasse klasse = alleKlassen.get(actualKlasse);

            for (int k = 0; k < klasse.size(); k++) {

                if (!klasse.get(k).anwesend) {
                    continue;
                }
                short operation = -1;

                operation = Operations.getRandomOperation();

                allTasks.add(new CalculationTask(klasse.get(k).name, reihen, limitedToSelectedSeries, operation));
            }
        }

        if (shuffle) {

            for (int j = 0; j < alleKlassen.size(); j++) {
                Collections.shuffle(alleKlassen.get(j));
            }
        }

        Klasse klasse = alleKlassen.get(actualKlasse);

        for (int j = 0; j < klasse.size(); j++) {

            OneSchueler osch = klasse.get(j);

            for (int k = 0; k < allTasks.size(); k++) {

                if (allTasks.get(k).name.contentEquals(osch.name)) {
                    osch.setNumberTasks(osch.getNumberTasks() + 1);
                    if (osch.getNumberTasks() > numberTasksProSchueler) {
                        osch.setNumberTasks(numberTasksProSchueler);
                    }
                }
            }
        }
        Collections.shuffle(allTasks);
        allTasks.checkForDoubleNames();

        try {
            setImageForTask();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    private void initColors() {

        allColorSchemes.add(new ColorSheme(Color.LIGHT_GRAY, Color.DARK_GRAY, Color.GRAY));
        allColorSchemes.add(new ColorSheme(Color.DARK_GRAY, Color.WHITE, Color.LIGHT_GRAY));
        allColorSchemes.add(new ColorSheme(ColorSheme.darkBlue, ColorSheme.orange, Color.lightGray));
        allColorSchemes.add(new ColorSheme(ColorSheme.darkBlue, ColorSheme.niceGreen, Color.WHITE));
    }

    private void writeSettings() {

        FileOutputStream f;
        try {
            System.out.println("writeSettings: " + settingsFileName);
            f = new FileOutputStream(settingsFileName);
            ObjectOutputStream os = new ObjectOutputStream(f);
            os.writeInt(frame.getX());
            os.writeInt(frame.getY());
            os.writeInt(frame.getWidth());
            os.writeInt(frame.getHeight());
            os.writeInt(colorSchemeId);
            os.writeInt(numberTasksProSchueler);
            os.writeFloat(transparency);
            os.writeBoolean(debugMode);
            os.writeInt(fontSizeNumbers);
            os.writeFloat(soundVolume);
            os.writeBoolean(playMusic);

            Operations.write(os);

            os.close();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readSettings() {

        FileInputStream f;
        try {
            f = new FileInputStream(settingsFileName);
            ObjectInputStream in = new ObjectInputStream(f);
            int x = in.readInt();
            int y = in.readInt();
            int w = in.readInt();
            int h = in.readInt();
            frame.setBounds(x, y, w, h);
            colorSchemeId = in.readInt();
            numberTasksProSchueler = in.readInt();
            if (numberTasksProSchueler < 2) {
                numberTasksProSchueler = 2;
            }
            transparency = in.readFloat();
            debugMode = in.readBoolean();
            fontSizeNumbers = in.readInt();
            soundVolume = in.readFloat();
            playMusic = in.readBoolean();

            Operations.read(in);

            in.close();
            f.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getHeight() {
        return frame.getHeight();
    }

    @Override
    public int getWidth() {
        return frame.getWidth();
    }

    @Override
    public void paint(Graphics g) {

        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        ColorSheme cs = allColorSchemes.get(colorSchemeId);

        float scWidth = (float) this.getWidth() / (float) bgImg.getWidth();
        float scHeight = (float) this.getHeight() / (float) bgImg.getHeight();
        if (scWidth < scHeight) {
            g2d.drawImage(bgImg, 0, 0, (int) (bgImg.getWidth() * scHeight), (int) (bgImg.getHeight() * scHeight), this);
        } else {
            g2d.drawImage(bgImg, 0, 0, (int) (bgImg.getWidth() * scWidth), (int) (bgImg.getHeight() * scWidth), this);
        }

        if (beginning) {
            g2d.setColor(ColorSheme.darkBlue);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        } else {
            drawTransparentCover(g2d);
        }

        g2d.setColor(cs.fgLight);
        int yPos = getHeight() / 2;
        int xPos = getWidth() / 2;

        drawKlasseAndNumberTasks(g2d);

        if (drawHelp) {
            drawHelp(g2d, cs);
            return;
        }

        if (drawSettings) {
            drawSettings(g2d, cs);
            return;
        }

        if (drawStatistics) {
            drawStatistics(g2d, cs);
            drawKlasseAndNumberTasks(g2d);
            return;
        }

        if (showDuration) {
            drawDurationAtTheEnd(g2d, xPos, cs);
            return;
        }

        drawOperations(g2d, cs);

        if (drawAufgabe) {
            drawAufgaben(g2d, cs, yPos, xPos);
        }

        if (!beginning) {
            drawRunningTime(g2d, xPos, cs);
        }
    }

    private void drawTransparentCover(Graphics2D g2d) {
        g2d.setColor(new Color(0.0f, 0.0f, 0.0f, transparency));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawKlasseAndNumberTasks(Graphics2D g2d) {

        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.setColor(Color.WHITE);

        /// draw number Schueler
        String str = "" + alleKlassen.get(actualKlasse).size() + " Schüler";

        if (pinnedName.length() > 0) {
            g2d.setColor(Color.RED);
            str = pinnedName;
        }

        FontMetrics metrics = g2d.getFontMetrics();
        int width = (int) metrics.getStringBounds(str, g2d).getWidth();
        g2d.drawString(str, (float) (getWidth() / 2.0 - width / 2.0), 26);

        g2d.setColor(Color.WHITE);

        if (limitedToSelectedSeries) {

            str = "limit to selected series";
            width = (int) metrics.getStringBounds(str, g2d).getWidth();
            Color sc = g2d.getColor();
            g2d.setColor(Color.ORANGE);
            g2d.drawString(str, (float) (getWidth() / 2.0 - width / 2.0), 66);
            g2d.setColor(sc);
        }

        double hs = alleKlassen.get(actualKlasse).highScore / 1000.0;

        if (pinnedName.length() > 0) {
            MTools.println(pinnedName + " pinned hs: " + pinnedHighScore);
            hs = pinnedHighScore / 1000.0;
            zeitProAufgabeUndSchueler = pinnedHighScore;
        }

        String hss = " - high score " + hs + " s";
        if (alleKlassen.get(actualKlasse).highScore >= 10000) {
            hss = " - kein high score vorhanden";
        }

        str = "Klasse " + Klasse.klassenString[actualKlasse] + " " + hss;
        g2d.drawString(str, 10, 26);


        str = alleKlassen.get(actualKlasse).getNumberTasks() + " Aufgaben";
        width = (int) metrics.getStringBounds(str, g2d).getWidth();
        g2d.drawString(str, getWidth() - width - 10, 26);
    }

    private void drawHelp(Graphics2D g2d, ColorSheme cs) {

        g2d.setColor(ColorSheme.darkBlue);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeStatistics));
        g2d.setColor(cs.fgDark);

        int yShift = 40;
        int xShift = 360;
        int i = 1;
        float yPos = factorDrawStatistics * fontSizeStatistics;
        g2d.drawString("H   ", 50, ((yShift + yPos * i)));
        g2d.drawString("Help", xShift, yShift + (yPos * i++));

        g2d.drawString("↓", 50, yShift + (yPos * i));
        g2d.drawString("Start des Trainings", xShift, yShift + (yPos * i++));

        g2d.drawString("Ctrl Q", 50, yShift + (yPos * i));
        g2d.drawString("Quit beendet das Programm", xShift, yShift + (yPos * i++));

        g2d.drawString("ESC", 50, yShift + (yPos * i));
        g2d.drawString("Zufällige Wahl eines Kandidaten", xShift, yShift + (yPos * i++));

        g2d.drawString("+ | -", 50, yShift + (yPos * i));
        g2d.drawString("Erhöht | erniedrigt Aufgaben pro Schüler", xShift, yShift + (yPos * i++));

        g2d.drawString("B", 50, yShift + (yPos * i));
        g2d.drawString("Zurück auf Beginn", xShift, yShift + (yPos * i++));

        g2d.drawString("E", 50, yShift + (yPos * i));
        g2d.drawString("Einstellungen", xShift, yShift + (yPos * i++));

        g2d.drawString("L", 50, yShift + (yPos * i));
        g2d.drawString("Limit Modus ein/aus", xShift, yShift + (yPos * i++));

        g2d.drawString("M", 50, yShift + (yPos * i));
        g2d.drawString("Hintergrundmusik ein/aus", xShift, yShift + (yPos * i++));

        g2d.drawString("S", 50, yShift + (yPos * i));
        g2d.drawString("Zeigt die Statistik", xShift, yShift + (yPos * i++));

        g2d.drawString("T | Shift T", 50, yShift + (yPos * i));
        g2d.drawString("Ändere die Tranzparenz des Bildes (+|-)", xShift, yShift + (yPos * i++));

        g2d.drawString("V | Shift V", 50, yShift + (yPos * i));
        g2d.drawString("Ändere die Lautstärke (volume) der Hintergrundmusik (+|-)", xShift, yShift + (yPos * i++));


        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        FontMetrics metrics = g2d.getFontMetrics();
        String str = "Mathematiktrainer von Dr. Michael R. Alvers - ©2020 - all rights reserved";
        Rectangle2D bounds = metrics.getStringBounds(str, g2d);
        g2d.setColor(Color.GRAY);
        g2d.drawString(str, (float) (getWidth() / 2 - bounds.getWidth() / 2), getHeight() - 30);

        drawKlasseAndNumberTasks(g2d);
    }

    private void drawSettings(Graphics2D g2d, ColorSheme cs) {

        g2d.setColor(ColorSheme.darkBlue);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeStatistics));
        g2d.setColor(cs.fgDark);

        int xIndent = 160;
        Color colorStore = g2d.getColor();

        int yShift = 80;
        for (int i = 2; i < reihen.size(); i++) {

            g2d.setColor(colorStore);

            if (reihen.get(i)) {
                g2d.drawString("✓  ", 50, yShift + factorDrawStatistics * fontSizeStatistics * (i - 2));
            } else {
                g2d.setColor(cs.fgLight);
            }

            String str = "er Reihe";
            g2d.drawString("" + (i) + str, xIndent, yShift + factorDrawStatistics * fontSizeStatistics * (i - 2));
        }

        drawKlasseAndNumberTasks(g2d);
    }

    private void drawStatistics(Graphics2D g2d, ColorSheme cs) {

        g2d.setColor(ColorSheme.darkBlue);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeStatistics));
        g2d.setColor(cs.fgDark);

        int xIndent = 160;
        Color colorStore = g2d.getColor();

        Klasse kl = alleKlassen.get(actualKlasse);

        int yShift = 80;
        for (int i = 0; i < kl.size(); i++) {

            g2d.setColor(colorStore);
            if (kl.get(i).anwesend) {
                g2d.drawString("✓  ", 50, yShift + factorDrawStatistics * fontSizeStatistics * i);
            } else {
                g2d.setColor(cs.fgLight);
            }

            String name = kl.get(i).name;
            if (name.contentEquals(pinnedName)) {
                g2d.setColor(Color.RED);
            }
            g2d.drawString(name, xIndent, yShift + factorDrawStatistics * fontSizeStatistics * i);
            int num = kl.get(i).getNumberTasks();
            String str = "" + num;
            if (num < 10) {
                str = "  " + str;
            }

            if (!kl.get(i).anwesend) {
                continue;
            }

            g2d.drawString(str, 500, yShift + factorDrawStatistics * fontSizeStatistics * i);
            num = kl.get(i).numberRightSolutions;
            str = "" + num;
            if (num < 10) {
                str = "  " + str;
            }
            g2d.drawString(str, 800, yShift + factorDrawStatistics * fontSizeStatistics * i);
        }
    }

    private void drawAufgaben(Graphics2D g2d, ColorSheme cs, int yPos, int xPos) {

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers));

        int yShift = -60;

        FontMetrics metrics;

        if (beginning) {
            drawMathIsImportant(g2d, cs, xPos);
            return;
        }

        drawNameAndCountDown(g2d, cs, xPos);

        /// draw numbers 1  &  number 2 & result

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers));
        metrics = g2d.getFontMetrics();
        Rectangle2D bounds;

        CalculationTask task = allTasks.get(taskCounter);

        if (debugMode) {
            g2d.setColor(Color.DARK_GRAY);
        } else {
            g2d.setColor(task.getColor());
        }


        if (iterationCount % 2 > 0 || iterationCount == 0) {

            if (iterationCount == 0) {
                iterationCount++;
            }

            String aufgabe = "";

            aufgabe = task.getTaskString();

            bounds = metrics.getStringBounds(aufgabe, g2d);
            g2d.drawString(aufgabe, (float) (xPos - (bounds.getWidth() / 2.0)), (float) (yShift + yPos - (bounds.getY() / 2)));

        } else {

            String res = task.getTaskString() + " = " + task.getResult();

            bounds = metrics.getStringBounds(res, g2d);
            g2d.drawString(res, (float) (xPos - (bounds.getWidth() / 2.0)), (float) (yShift + yPos - (bounds.getY() / 2)));
        }
    }

    private void drawOperations(Graphics2D g2d, ColorSheme cs) {

        g2d.setFont(new Font("Times", Font.PLAIN, 30));
        FontMetrics metrics = g2d.getFontMetrics();

        g2d.setColor(cs.fgLight);

        String sOperations = "";
        int sw = -1;
        int xPos = 10;
        int yPos = getHeight() - 40;

        /// PLUS
        g2d.setColor(Color.WHITE);
        sOperations = " + ";
        if (Operations.isOn(Operations.plus) == Operations.ON) {
            g2d.setColor(Color.CYAN);
        }
        g2d.drawString(sOperations, xPos, yPos);
        xPos += metrics.stringWidth(sOperations);

        /// MINUS
        g2d.setColor(Color.WHITE);
        sOperations = " − ";
        if (Operations.isOn(Operations.minus) == Operations.ON) {
            g2d.setColor(Color.RED.darker());
        }
        g2d.drawString(sOperations, xPos, yPos);
        xPos += metrics.stringWidth(sOperations);

        /// TIMES
        g2d.setColor(Color.WHITE);
        sOperations = " × ";
        if (Operations.isOn(Operations.multiply) == Operations.ON) {
            g2d.setColor(Color.GREEN);
        }
        g2d.drawString(sOperations, xPos, yPos);
        xPos += metrics.stringWidth(sOperations);

        /// DIVIDE
        g2d.setColor(Color.WHITE);
        sOperations = " ÷ ";
        if (Operations.isOn(Operations.divide) == Operations.ON) {
            g2d.setColor(Color.ORANGE);
        }
        g2d.drawString(sOperations, xPos, yPos);
    }

    private void drawNameAndCountDown(Graphics2D g2d, ColorSheme cs, int xPos) {

        FontMetrics metrics;
        g2d.setColor(cs.fgLight);
        g2d.setFont(new Font("Arial", Font.PLAIN, 90));

        if (taskCounter >= 0) {
            metrics = g2d.getFontMetrics();
            String str = "Aufgabe " + (taskCounter + 1);
            int sWidth = metrics.stringWidth(str);
            g2d.drawString(str, xPos - sWidth / 2, 160);
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, 90));

        /// draw name

        metrics = g2d.getFontMetrics();
        g2d.setColor(cs.fgLight);
        String str;
        if (pinnedName.length() > 0) {
            str = pinnedName;
        } else {
            str = allTasks.get(taskCounter).name;
        }
        int sw = metrics.stringWidth(str);
        g2d.drawString(str, xPos - (sw / 2), getHeight() - 200);

        /// draw count down

        str = "" + countDownCounter;
        g2d.setFont(new Font("Arial", Font.PLAIN, 50));
        metrics = g2d.getFontMetrics();
        sw = metrics.stringWidth(str);
        if (countDownCounter > -1) {
            int rw = 60;
            g2d.setColor(Color.CYAN.darker());
            if (countDownCounter < 4) {
                g2d.setColor(Color.ORANGE);
            }
            if (countDownCounter < 2) {
                g2d.setColor(Color.RED);
            }
            g2d.drawOval(getWidth() / 2 - rw / 2, getHeight() - 110 - 48, rw, rw);
            g2d.drawString(str, (float) getWidth() / 2.0f - (float) sw / 2.0f, getHeight() - 110);
        }
    }

    private void drawMathIsImportant(Graphics2D g2d, ColorSheme cs, int xPos) {

        FontMetrics metrics;
        g2d.setFont(new Font("Arial", Font.PLAIN, 90));
        metrics = g2d.getFontMetrics();

        /// draw Das kleine

        String dasKleine = "Mathematik";
        int sw = metrics.stringWidth(dasKleine);
        g2d.setColor(cs.fgLight);
        g2d.drawString(dasKleine, xPos - (sw / 2), 190);

        /// draw ist wichtig

        g2d.setColor(cs.fgLight);
        String istWichtig = "ist wichtig!";
        sw = metrics.stringWidth(istWichtig);
        g2d.drawString(istWichtig, xPos - (sw / 2), getHeight() - 160);

        /// arrow down for go
        g2d.setColor(Color.GRAY);
        String gForGo = "Press ↓ to start training! Press h for help.";
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        metrics = g2d.getFontMetrics();
        sw = metrics.stringWidth(gForGo);
        g2d.drawString(gForGo, xPos - (sw / 2), getHeight() - 100);

        if (debugMode) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(cs.fgDark);
        }
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers));
        metrics = g2d.getFontMetrics();
        String str = "・";

        Rectangle2D bounds = metrics.getStringBounds("1" + str + "1", g2d);
        g2d.drawString("1" + str + "1", (float) (xPos - (bounds.getWidth() / 2.0)), (float) ((getHeight() / 2.0) + (bounds.getHeight() / 4.0f)));

    }

    private void drawRunningTime(Graphics2D g2d, int xPos, ColorSheme cs) {

        FontMetrics metrics;
        long now = System.currentTimeMillis();

        deltaT = now - timerStart;

        String duration = Util.getTimeStringDuration(deltaT);

        duration = duration.substring(3);
        duration = duration.substring(0, duration.length() - 4);

        if (penalty > 0) {
            duration += " + " + penalty + " s Strafe";
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, 26));
        metrics = g2d.getFontMetrics();
        int sWidth = metrics.stringWidth(duration);

        g2d.setColor(cs.fgLight);
        g2d.drawString(duration, xPos - (sWidth / 2), getHeight() - 40);
    }

    private String getHighScoreString() {

        return "" + alleKlassen.get(actualKlasse).highScore / 1000.0;
    }

    private void drawDurationAtTheEnd(Graphics2D g2d, int xPos, ColorSheme cs) {

        g2d.setColor(ColorSheme.darkBlue);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        drawKlasseAndNumberTasks(g2d);

        int yShift = -40;

        drawGesamtzeitTeam(g2d, cs, xPos, yShift);

        drawZeitProSchueler(g2d, cs, xPos, yShift);

        drawHighScore(g2d, cs, xPos, yShift);
    }

    private void drawGesamtzeitTeam(Graphics2D g2d, ColorSheme cs, int xPos, int yShift) {

        FontMetrics metrics;
        int sWidth;

        String ttt = "Gesamtzeit für das Team";
        g2d.setColor(cs.fgLight);

        if (penalty > 0) {
            g2d.setColor(Color.RED);
            ttt = "Gesamtzeit für das Team mit " + penalty + " s Strafe";
        }

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        metrics = g2d.getFontMetrics();
        sWidth = metrics.stringWidth(ttt);
        g2d.drawString(ttt, xPos - (sWidth / 2), yShift + (getHeight() / 2 - 220));

        String duration = Util.getTimeStringDuration(finalDeltaT + penalty * 1000);

        duration = duration.substring(3);
        duration = duration.substring(0, duration.length() - 4);
        String min = duration.substring(0, duration.indexOf(":"));
        String sec = duration.substring(duration.indexOf(":") + 1);

        duration = min + " min " + sec + " s";

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers / 2));
        g2d.setColor(ColorSheme.niceGreen);
        metrics = g2d.getFontMetrics();
        sWidth = metrics.stringWidth(duration);

        g2d.drawString(duration, xPos - (sWidth / 2), yShift + (getHeight() / 2 - 120));
    }

    private void drawZeitProSchueler(Graphics2D g2d, ColorSheme cs, int xPos, int yShift) {

        FontMetrics metrics;

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        metrics = g2d.getFontMetrics();
        String ttt = "Zeit pro Aufgabe pro Schüler";
        int sWidth = metrics.stringWidth(ttt);
        g2d.setColor(cs.fgLight);
        g2d.drawString(ttt, xPos - (sWidth / 2), yShift + (getHeight() / 2));

        zeitProAufgabeUndSchueler = (int) ((double) finalDeltaT / (double) alleKlassen.get(actualKlasse).getNumberTasks());
        String ts = "" + (double) zeitProAufgabeUndSchueler / 1000.0 + " s";
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers / 2));
        metrics = g2d.getFontMetrics();
        sWidth = metrics.stringWidth(ts);
        g2d.setColor(Color.ORANGE);
        g2d.drawString(ts, xPos - (sWidth / 2), yShift + (getHeight() / 2 + 100));
    }

    private void drawHighScore(Graphics2D g2d, ColorSheme cs, int xPos, int yShift) {

        String ttt;
        FontMetrics metrics;
        int sWidth;
        String ts;

        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        metrics = g2d.getFontMetrics();
        ttt = "High Score Team";
        sWidth = metrics.stringWidth(ttt);
        g2d.setColor(cs.fgLight);
        g2d.drawString(ttt, xPos - (sWidth / 2), yShift + (getHeight() / 2 + 200));

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers / 2));
        metrics = g2d.getFontMetrics();

        ts = getHighScoreString() + " s";
        sWidth = metrics.stringWidth(ts);
        g2d.setColor(Color.CYAN);
        g2d.drawString(ts, xPos - (sWidth / 2), yShift + (getHeight() / 2 + 300));
    }

    private void display() {

        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

//        MTools.println("mousy");

        if (handleOperationSwitsching(e)) {
            return;
        }

        double yPos = e.getY();

        int id = (int) ((yPos - (fontSizeStatistics)) / (factorDrawStatistics * fontSizeStatistics));

        if (id >= alleKlassen.get(actualKlasse).size()) {
            return;
        }

        if (drawStatistics) {

            if (SwingUtilities.isRightMouseButton(e)) {

                if (pinnedName.length() == 0) {

                    pinnedName = alleKlassen.get(actualKlasse).get(id).name;

                    /// read pinned high score
                    String fileName = pinnedName + "_HighScore.txt";
                    Scanner sc = null;
                    try {
                        sc = new Scanner(new File(fileName));
                    } catch (FileNotFoundException fileNotFoundException) {

                        /// do nothing

                    } finally {

                        while (sc.hasNextLine()) {
                            String str = sc.nextLine();
                            pinnedHighScore = Integer.parseInt(str);
                        }
                    }

                } else {
                    pinnedName = "";
                }

            } else {
                alleKlassen.get(actualKlasse).get(id).anwesend = !alleKlassen.get(actualKlasse).get(id).anwesend;
            }

        } else if (drawSettings) {
            if (id + 2 < reihen.size()) {
                reihen.set(id + 2, !reihen.get(id + 2));
            }
        }

        initAllTasks(false);

        repaint();
    }

    private boolean handleOperationSwitsching(MouseEvent e) {

        int xPos = e.getX();
        int yPos = e.getY();
        if (yPos < getHeight() - 60) {
            return false;
        }

        int xInc = 38;
        int xShift = xInc;
        int xStart = 0;

        if (xPos < xShift) {
            MTools.println(" + ");
            Operations.toggleOperationOnOff(Operations.plus);
            initAllTasks(true);
            repaint();
            return true;
        } else if (xPos > (xStart += xInc) && xPos < (xStart + xInc)) {
            MTools.println(" - ");
            Operations.toggleOperationOnOff(Operations.minus);
            initAllTasks(true);
            repaint();
            return true;
        } else if (xPos > (xStart += xInc) && xPos < (xStart + xInc)) {
            MTools.println(" X ");
            Operations.toggleOperationOnOff(Operations.multiply);
            initAllTasks(true);
            repaint();
            return true;
        } else if (xPos > (xStart += xInc) && xPos < (xStart + xInc)) {
            Operations.toggleOperationOnOff(Operations.divide);
            MTools.println(" : ");
            initAllTasks(true);
            repaint();
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

//        MTools.println("key: " + e.getKeyCode() + " shift: " + e.isShiftDown());

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {

            handleEscape();

        } else if ((e.isShiftDown() && e.getKeyCode() == 48) || e.getKeyCode() == KeyEvent.VK_ENTER) {

        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {

            handleWrong();

        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == 34) {

            if (handleDown()) {
                return;
            }

        } else if (e.getKeyCode() == 93) {

            /// PLUS

            if (e.isShiftDown()) {
                fontSizeNumbers += 5;
            } else {
                numberTasksProSchueler++;
                //initNames();
                initAllTasks(true);
            }

        } else if (e.getKeyCode() == 47) {

            /// MINUS

            if (e.isShiftDown()) {
                fontSizeNumbers -= 5;
            } else {
                numberTasksProSchueler--;
                initAllTasks(true);
            }

        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (clip.isRunning()) {
                clip.stop();
            } else {
                clip.start();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_1) {
            colorSchemeId = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_2) {
            colorSchemeId = 1;
        } else if (e.getKeyCode() == KeyEvent.VK_3) {
            colorSchemeId = 2;
        } else if (e.getKeyCode() == KeyEvent.VK_4) {
            colorSchemeId = 3;
        } else if (e.getKeyCode() == KeyEvent.VK_5) {
        } else if (e.getKeyCode() == KeyEvent.VK_6) {
        } else if (e.getKeyCode() == KeyEvent.VK_7) {
            actualKlasse = KlassenId.seven;
            initNames(false);
            initAllTasks(true);
        } else if (e.getKeyCode() == KeyEvent.VK_8) {
            actualKlasse = KlassenId.eight;
            initNames(false);
            initAllTasks(true);
        } else if (e.getKeyCode() == KeyEvent.VK_9) {
            if (actualKlasse == KlassenId.nineA || actualKlasse == KlassenId.eight) {
                actualKlasse = KlassenId.nineB;
            } else {
                actualKlasse = KlassenId.nineA;
            }
            initNames(false);
            initAllTasks(true);
        } else if (e.getKeyCode() == KeyEvent.VK_0) {
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            drawAufgabe = !drawAufgabe;
        } else if (e.getKeyCode() == KeyEvent.VK_B) {
            initBeginning();
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            debugMode = !debugMode;
        } else if (e.getKeyCode() == KeyEvent.VK_E) {

            drawHelp = false;
            drawStatistics = false;
            drawSettings = !drawSettings;

        } else if (e.getKeyCode() == KeyEvent.VK_H) {

            drawStatistics = false;
            drawSettings = false;
            drawHelp = !drawHelp;

        } else if (e.getKeyCode() == KeyEvent.VK_I) {
        } else if (e.getKeyCode() == KeyEvent.VK_G) {
        } else if (e.getKeyCode() == KeyEvent.VK_L) {

            limitedToSelectedSeries = !limitedToSelectedSeries;
            initAllTasks(true);

        } else if (e.getKeyCode() == KeyEvent.VK_M) {

            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            playMusic = !playMusic;

            MTools.println("M - soundVolume: " + soundVolume);

            if (playMusic) {
                gainControl.setValue(20f * (float) Math.log10(soundVolume));
            } else {
                gainControl.setValue(-100);
            }

        } else if (e.getKeyCode() == KeyEvent.VK_P) {
            allTasks.print();
        } else if (e.getKeyCode() == KeyEvent.VK_Q) {

        } else if (e.getKeyCode() == KeyEvent.VK_R) {

            if (e.isShiftDown()) {
                readImages();
            }
            initNames(false);

        } else if (e.getKeyCode() == KeyEvent.VK_S) {

            drawSettings = false;
            drawHelp = false;
            drawStatistics = !drawStatistics;

        } else if (e.getKeyCode() == KeyEvent.VK_T) {

            if (e.isShiftDown()) {
                transparency -= 0.1;
            } else {
                transparency += 0.1;
            }
            MTools.println("transparency: " + transparency);

        } else if (e.getKeyCode() == KeyEvent.VK_X) {

            handelExperimental();

        } else if (e.getKeyCode() == KeyEvent.VK_W) {

            if (clip.isOpen() || clip.isRunning()) {
                clip.stop();
            }
            setAndPlaySound("sound/6 Minuten Jeopardy Theme Music 5%.wav");

        } else if (e.getKeyCode() == KeyEvent.VK_V) {

            if (e.isShiftDown()) {
                soundVolume += 0.1f;
            } else {
                soundVolume -= 0.1f;
            }
            if (soundVolume < 0.1f) {
                soundVolume = 0.1f;
            }
            if (soundVolume > 1.0f) {
                soundVolume = 1.0f;
            }
            MTools.println("soundVolume: " + soundVolume);

            setVolume();

        } else if (e.getKeyCode() == KeyEvent.VK_Z) {

            for (int i = 0; i < 20; i++) {
                int num1 = (int) (Math.random() * 8 + 2);
                int num2 = (int) (Math.random() * 8 + 2);
                MTools.println(num1 + " x " + num2 + " = ");
            }
            for (int i = 0; i < 20; i++) {
                int num1 = (int) (Math.random() * 8 + 2);
                int num2 = (int) (Math.random() * 8 + 2);
                num1 = num1 * num2;
                MTools.println(num1 + " : " + num2 + " = ");
            }
        }

        display();
    }

    private void handelExperimental() {

        MTools.println("Xperimental ...");

        if (clip.isOpen() || clip.isRunning()) {
            clip.stop();
        }

        if (soundOnDisplay.contentEquals(sound1)) {
            soundOnDisplay = sound2;
            setAndPlaySound(soundOnDisplay);
        } else {
            soundOnDisplay = sound1;
            setAndPlaySound(soundOnDisplay);
        }
    }

    private void setVolume() {

        if (clip == null) {
            return;
        }

        MTools.println("soundVolume: " + soundVolume);
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(20f * (float) Math.log10(soundVolume));
        MTools.println("setVolume() - volume: " + gainControl.getValue());
    }

    private void handleEscape() {

        drawStatistics = false;
        drawSettings = false;
        beginning = false;
        initAllTasks(true);
    }

    private boolean handleDown() {

        drawSettings = false;
        drawStatistics = false;
        drawHelp = false;

        if (beginning) {

            beginning = false;
            shallWriteHighScore = false;

            if (taskCounter == 0 && !timeStartIsReseted) {

//                MTools.println("start timer ... " + taskCounter);
                countDown = new MyCountDown(this, countDownFrom);
                timeStartIsReseted = true;
                resetTimerStart();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
//                        MTools.println( "running:" );
                        repaint();
                    }
                }, 0, 200);
            }
            playSchuelerName(allTasks.get(taskCounter).name);
            return true;
        }

        if (showDuration) {
            return true;
        }

        if (nextTask != null) {
            nextTask.cancel();
        }

        iterationCount++;

        if (iterationCount % 2 > 0) {

            taskCounter++;

            if (taskCounter >= alleKlassen.get(actualKlasse).getNumberTasks()) {
                handleFinished();
            }

            countDown = new MyCountDown(this, countDownFrom);

            try {
                setImageForTask();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

            playSchuelerName(allTasks.get(taskCounter).name);

        } else {


            if (taskCounter >= alleKlassen.get(actualKlasse).getNumberTasks()) {
                taskCounter = numberTasksProSchueler;
            }

            Klasse klasse = alleKlassen.get(actualKlasse);

            for (int i = 0; i < klasse.size(); i++) {
                if (klasse.getSchueler(i).name.contentEquals(allTasks.get(taskCounter).name)) {
                    klasse.getSchueler(i).numberRightSolutions++;
                }
            }
            if (countDown != null) {
                countDown.cancel();
                nextTask = new Timer();
                nextTask.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                nextTaskCountDown--;
                                if (nextTaskCountDown == 0) {
                                    nextTaskCountDown = nextTaskCountDownFrom;
                                    this.cancel();
                                    fireDown();
                                }
                            }
                        }, 0, 1000
                );
            }
            countDownCounter = -1;
        }

        return false;
    }

    private void handleFinished() {

        showDuration = true;
        taskCounter = numberTasksProSchueler;
        finalDeltaT = deltaT;

        int numSchueler = alleKlassen.get(actualKlasse).size();
        int numTasks = numberTasksProSchueler * numSchueler;

        long newHighScore = (long) ((double) finalDeltaT / (double) numTasks);

        if (!debugMode && pinnedName.length() == 0 && newHighScore < alleKlassen.get(actualKlasse).highScore) {
            alleKlassen.get(actualKlasse).highScore = newHighScore;
        }
        shallWriteHighScore = true;

        timer.cancel();
    }

    private void handleWrong() {

        if (drawStatistics && drawSettings) {
            return;
        }
        penalty += countDownFrom;
        handleDown();

    }

    private void writeHighScores() {

        if (!shallWriteHighScore) {
            return;
        }

        MTools.println("write high score Klasse: " + Klasse.klassenString[actualKlasse]);

        if (pinnedName.length() > 0) {

            Writer writer;
            try {
                writer = new OutputStreamWriter(new FileOutputStream(pinnedName + "_HighScore.txt"));
                writer.append("" + zeitProAufgabeUndSchueler);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {

            try {
                alleKlassen.get(actualKlasse).writeHighScore();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setImageForTask() throws IOException {

        CalculationTask task = allTasks.get(taskCounter);
        if (task.getOperation() == Operations.multiply) {
            if (task.number1 > task.number2) {
                bgImg = ImageIO.read(imagesMatrix[task.number2][task.number1]);
            } else {
                bgImg = ImageIO.read(imagesMatrix[task.number1][task.number2]);
            }
        } else if (task.getOperation() == Operations.divide) {

            int ind1 = task.number1 / task.number2;
            int ind2 = task.number2;

            if (ind1 > ind2) {
                bgImg = ImageIO.read(imagesMatrix[ind2][ind1]);
            } else {
                bgImg = ImageIO.read(imagesMatrix[ind1][ind2]);
            }
        }
    }

    private void readImages() {

        File folder = new File("images");
        File[] listOfFiles = folder.listFiles();

        int counter = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                imagesMatrix[i][j] = listOfFiles[counter];

//                MTools.println(i + " - " + j + " name: " + listOfFiles[counter].getName());

                counter++;
                if (counter > listOfFiles.length) {
                    break;
                }
            }
        }

        try {
            bgImg = ImageIO.read(imagesMatrix[7][8]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fireDown() {
        handleDown();
    }

    public void setCountDown(int counter) {
        countDownCounter = counter;
        if (countDownCounter == 0) {
            countDownCounter = -1;
        }

        repaint();
    }

    private void resetTimerStart() {
        timer = new Timer();
        timerStart = System.currentTimeMillis();
    }

    @Override
    public void keyReleased(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            resetTimerStart();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    repaint();
                }
            }, 0, 1000);
            if (countDown != null) {
                countDown.cancel();
            }
            countDown = new MyCountDown(this, countDownFrom);
        }
    }

    /// main for testing
    public static void main(String[] args) throws InterruptedException {

        frame = new JFrame();
        frame.setLayout(new GridLayout());
        frame.setBounds(0, 0, 1280, 800);
        frame.add(new MatheTrainer());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//        frame.setUndecorated(true);
        frame.setVisible(true);
    }

    private static void playSchuelerName(String schueler) {

        String gender = "_male";

        if (Math.random() < 0.5) {
            gender = "_female";
        }

        String name = "sound/" + schueler + "_de-DE" + gender + ".wav";

        MTools.println("Playing sound: " + name);

        setAndPlaySound(name);
    }

    private static void setAndPlaySound(String name) {

        try {
            setSound(name);
            clip.start();
        } catch (Exception ex) {
            System.out.println("Error with playing sound: " + name);
        }
    }

    private static void setSound(String name) {

        AudioInputStream audioInputStream = null;
        try {
            clip = null;
            audioInputStream = AudioSystem.getAudioInputStream(new File(name).getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
//            clip.loop(10);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            MTools.println( "file not found: " + name);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
