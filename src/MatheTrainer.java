import mratools.MTools;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Timer;
import java.util.*;

public class MatheTrainer extends JPanel implements MouseListener, KeyListener {

    private static JFrame frame;
    private Timer timer;
    private MyCountDown countDown;

    ArrayList<ColorSheme> allColorSchemes = new ArrayList<>();
    AllTasks allTasks = new AllTasks();
    ArrayList<Klasse> alleKlassen = new ArrayList<>();
    Reihen reihen = new Reihen(10);

    File[][] imagesMatrix = new File[10][10];

    BufferedImage bgImage = null;

    private final int fontSizeStatistics = 40;
    private int numberTasksProSchueler = 6;
    private int taskCounter = 0;
    private int colorSchemeId = 0;
    private int fontSizeNumbers = 220;
    private int iterationCount = 0;

    private final float factorStatistics = 1.4f;
    private float transparency = 0.5f;

    private int actualKlasse = KlassenId.nineB;

    private long timerStart;
    private long deltaT;
    private long finalDeltaT;

    private final String settingsFileName = "MatheTrainer.binary.settings";
    private String sOperator = " + ";
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
    private int operation = Operations.multiply;

    public MatheTrainer() {

        setFocusable(true);

        addMouseListener(this);
        addKeyListener(this);

        /// make sure also whe window is closed via X or cmd q all settings are written
        Thread t = new Thread(() -> {
            writeSettings();
            writeHighScores();
        });
        Runtime.getRuntime().addShutdownHook(t);

        readSettings();

        initColors();

        initNames();

        initOperations();

        initAllTasks(true);

        readImages();

        try {
            setImageForTask();
        } catch (IOException e) {
            e.printStackTrace();
        }

        timer = new Timer();
        resetTimerStart();
    }

    private void initOperations() {
        operation = Operations.divide;
    }

    private void initNames() {

        alleKlassen.clear();

        new Klasse();
        for (int klassenId = 0; klassenId < Klasse.klassenString.length; klassenId++) {
            alleKlassen.add(new Klasse(klassenId));
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
                allTasks.add(new CalculationTask(klasse.get(k).name, reihen, limitedToSelectedSeries, operation));
            }
        }
        allTasks.print();

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

//        MTools.println("print alle Klassen");
//        for (int i = 0; i < alleKlassen.size(); i++) {
//            MTools.println("klasse: " + i);
//            alleKlassen.get(i).print();
//        }
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

            os.close();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readSettings() {

        FileInputStream f;
        try {
//            System.out.println("readSettings: " + settingsFileName);
            f = new FileInputStream(settingsFileName);
            ObjectInputStream in = new ObjectInputStream(f);
            int x = in.readInt();
            int y = in.readInt();
            int w = in.readInt();
            int h = in.readInt();
            frame.setBounds(x, y, w, h);
            colorSchemeId = in.readInt();
            numberTasksProSchueler = in.readInt();
            transparency = in.readFloat();
            debugMode = in.readBoolean();

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
        g2d.setColor(cs.backGround);
        g2d.setColor(new Color(0.0f, 0.0f, 0.0f, transparency));

        float scWidth = (float) this.getWidth() / (float) bgImage.getWidth();
        float scHeight = (float) this.getHeight() / (float) bgImage.getHeight();
        if (scWidth < scHeight) {
            g2d.drawImage(bgImage, 0, 0, (int) (bgImage.getWidth() * scHeight), (int) (bgImage.getHeight() * scHeight), this);
        } else {
            g2d.drawImage(bgImage, 0, 0, (int) (bgImage.getWidth() * scWidth), (int) (bgImage.getHeight() * scWidth), this);
        }

        if (beginning) {
            g2d.setColor(ColorSheme.darkBlue);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(cs.fgLight);
        int yPos = getHeight() / 2;
        int xPos = getWidth() / 2;

        drawKlasseAndNumberTasks(g2d);

        g2d.setColor(cs.fgDark);
        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers));

        if (drawHelp) {
            g2d.setColor(ColorSheme.darkBlue);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            drawHelp(g2d, cs);
            return;
        }

        if (drawSettings) {
            g2d.setColor(ColorSheme.darkBlue);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            drawSettings(g2d, cs);
            return;
        }

        if (drawStatistics) {
            g2d.setColor(ColorSheme.darkBlue);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            drawStatistics(g2d, cs);
            drawKlasseAndNumberTasks(g2d);
            return;
        }

        if (showDuration) {
            g2d.setColor(ColorSheme.darkBlue);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            drawDurationAtTheEnd(g2d, xPos, cs);
            return;
        }


        drawAufgaben(g2d, cs, yPos, xPos);

        if (!beginning) {
            drawRunningTime(g2d, xPos, cs);
        } else {
            resetTimerStart();
            timer = new Timer();
        }
        drawDebugIndicator();
    }

    private void drawDebugIndicator() {

        if (debugMode) {
            frame.setTitle("DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG ");
        } else {
            frame.setTitle("TRAINING");
        }
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

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeStatistics));
        g2d.setColor(cs.fgDark);

        int yShift = 30;
        int xShift = 360;
        int i = 1;
        float yPos = factorStatistics * fontSizeStatistics;
        g2d.drawString("H   ", 50, ((yShift + yPos * i)));
        g2d.drawString("Help", xShift, yShift + (yPos * i++));

        g2d.drawString("↓", 50, yShift + (yPos * i));
        g2d.drawString("Start des Trainings", xShift, yShift + (yPos * i++));

        g2d.drawString("ESC", 50, yShift + (yPos * i));
        g2d.drawString("Zufällige Wahl eines Kandidaten", xShift, yShift + (yPos * i++));

        g2d.drawString("+", 50, yShift + (yPos * i));
        g2d.drawString("Erhöht Aufgaben pro Schüler", xShift, yShift + (yPos * i++));

        g2d.drawString("-", 50, yShift + (yPos * i));
        g2d.drawString("Erniedrigt Aufgaben pro Schüler", xShift, yShift + (yPos * i++));

        g2d.drawString("B", 50, yShift + (yPos * i));
        g2d.drawString("Zurück auf Beginn", xShift, yShift + (yPos * i++));

        g2d.drawString("E", 50, yShift + (yPos * i));
        g2d.drawString("Einstellungen", xShift, yShift + (yPos * i++));

        g2d.drawString("L", 50, yShift + (yPos * i));
        g2d.drawString("Limit Modus ein/aus", xShift, yShift + (yPos * i++));

        g2d.drawString("Ctrl Q", 50, yShift + (yPos * i));
        g2d.drawString("Quit beendet das Programm", xShift, yShift + (yPos * i++));

        g2d.drawString("S", 50, yShift + (yPos * i));
        g2d.drawString("Zeigt die Statistik", xShift, yShift + (yPos * i++));

        g2d.drawString("T | Shift T", 50, yShift + (yPos * i));
        g2d.drawString("Ändere die Tranzparenz des Bildes (+|-)", xShift, yShift + (yPos * i++));


        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        FontMetrics metrics = g2d.getFontMetrics();
        String str = "Mathematiktrainer von Dr. Michael R. Alvers - ©2020 - all rights reserved";
        Rectangle2D bounds = metrics.getStringBounds(str, g2d);
        g2d.setColor(Color.GRAY);
        g2d.drawString(str, (float) (getWidth() / 2 - bounds.getWidth() / 2), getHeight() - 30);
    }

    private void drawSettings(Graphics2D g2d, ColorSheme cs) {

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeStatistics));
        g2d.setColor(cs.fgDark);

        int xIndent = 160;
        Color colorStore = g2d.getColor();

        for (int i = 2; i < reihen.size(); i++) {

            g2d.setColor(colorStore);
            int yShiftStatistics = 80;

            if (reihen.get(i)) {
                g2d.drawString("✓  ", 50, yShiftStatistics + factorStatistics * fontSizeStatistics * (i - 2));
            } else {
                g2d.setColor(cs.fgLight);
            }

            String str = "er Reihe";
            g2d.drawString("" + (i) + str, xIndent, yShiftStatistics + factorStatistics * fontSizeStatistics * (i - 2));
        }
    }

    private void drawStatistics(Graphics2D g2d, ColorSheme cs) {

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeStatistics));
        g2d.setColor(cs.fgDark);

        int xIndent = 160;
        Color colorStore = g2d.getColor();

        Klasse kl = alleKlassen.get(actualKlasse);

        for (int i = 0; i < kl.size(); i++) {

            g2d.setColor(colorStore);
            int yShiftStatistics = 80;
            if (kl.get(i).anwesend) {
                g2d.drawString("✓  ", 50, yShiftStatistics + factorStatistics * fontSizeStatistics * i);
            } else {
                g2d.setColor(cs.fgLight);
            }

            String name = kl.get(i).name;
            if (name.contentEquals(pinnedName)) {
                g2d.setColor(Color.RED);
            }
            g2d.drawString(name, xIndent, yShiftStatistics + factorStatistics * fontSizeStatistics * i);
            int num = kl.get(i).getNumberTasks();
            String str = "" + num;
            if (num < 10) {
                str = "  " + str;
            }

            if (!kl.get(i).anwesend) {
                continue;
            }

            g2d.drawString(str, 500, yShiftStatistics + factorStatistics * fontSizeStatistics * i);
            num = kl.get(i).numberRightSolutions;
            str = "" + num;
            if (num < 10) {
                str = "  " + str;
            }
            g2d.drawString(str, 800, yShiftStatistics + factorStatistics * fontSizeStatistics * i);
        }
    }

    private void drawAufgaben(Graphics2D g2d, ColorSheme cs, int yPos, int xPos) {

        int yShift = -60;

        g2d.setColor(cs.fgDark);

        FontMetrics metrics;

        if (beginning) {
            drawDasKleineIstWichtig(g2d, cs, xPos);
            return;
        }

        drawOperations();

        drawNameAndCountDown(g2d, cs, xPos);

        /// draw numbers 1  &  number 2 & result

        g2d.setFont(new Font("Arial", Font.PLAIN, fontSizeNumbers));
        metrics = g2d.getFontMetrics();
        Rectangle2D bounds;
        if (debugMode) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(cs.fgDark);
        }

        CalculationTask task = allTasks.get(taskCounter);

        if (iterationCount % 2 > 0 || iterationCount == 0) {

            if (iterationCount == 0) {
                iterationCount++;
            }

            String aufgabe = "";

            if (operation == Operations.multiply) {
                aufgabe = task.number1 + "・" + task.number2;
            } else if (operation == Operations.divide) {
                aufgabe = task.number1 + " ÷ " + task.number2;
            }

            bounds = metrics.getStringBounds(aufgabe, g2d);
            g2d.drawString(aufgabe, (float) (xPos - (bounds.getWidth() / 2.0)), (float) (yShift + yPos - (bounds.getY() / 2)));

        } else {

            int ergebnis = -1;

            if (operation == Operations.multiply) {
                sOperator = "・";
                ergebnis = task.number1 * task.number2;
            } else if (operation == Operations.divide) {
                sOperator = " ÷ ";
                ergebnis = task.number1 / task.number2;
            }

            String res;
            if (ergebnis < 10) {
                if (ergebnis == 1) {
                    res = task.number1 + sOperator + task.number2;
                } else if( operation == Operations.divide){
                    res = task.number1 + sOperator + task.number2 + " = " + ergebnis;
                } else  {
                    res = task.number1 + sOperator + task.number2 + " =   " + ergebnis;
                }
            } else {
                res = task.number1 + sOperator + task.number2 + " = " + ergebnis;
            }

            bounds = metrics.getStringBounds(res, g2d);
            g2d.drawString(res, (float) (xPos - (bounds.getWidth() / 2.0)), (float) (yShift + yPos - (bounds.getY() / 2)));
        }
    }

    private void drawOperations() {

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
            g2d.setColor(cs.fgDark);
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

    private void drawDasKleineIstWichtig(Graphics2D g2d, ColorSheme cs, int xPos) {

        FontMetrics metrics;
        g2d.setFont(new Font("Arial", Font.PLAIN, 90));
        metrics = g2d.getFontMetrics();

        /// draw Das kleine

        String dasKleine = "Das kleine";
        int sw = metrics.stringWidth(dasKleine);
        g2d.setColor(cs.fgLight);
        g2d.drawString(dasKleine, xPos - (sw / 2), 160);

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
        if( operation == Operations.divide ) str = " ÷ ";
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

        drawKlasseAndNumberTasks(g2d);

        int yShift = -40;

        drawGesamtzeitTeam(g2d, cs, xPos, yShift);

        drawZeitProSchueler(g2d, cs, xPos, yShift);

        drawHighScore(g2d, cs, xPos, yShift);
    }

    private void drawGesamtzeitTeam(Graphics2D g2d, ColorSheme cs, int xPos, int yShift) {

        FontMetrics metrics;
        int sWidth;

        String ttt = "Gesamtzeit für das Team [mm:ss]";
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

        double yPos = e.getY();

        int id = (int) ((yPos - (fontSizeStatistics)) / (factorStatistics * fontSizeStatistics));

        MTools.println("click count: " + e.getClickCount());

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
//                        fileNotFoundException.printStackTrace();
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
            reihen.set(id + 2, !reihen.get(id + 2));
        }

        initAllTasks(false);

        repaint();
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

        MTools.println("");
        MTools.println("key: " + e.getKeyCode() + " shift: " + e.isShiftDown());

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {

            drawStatistics = false;
            drawSettings = false;

            initAllTasks(true);
            resetTimerStart();

            if (showDuration) {
                iterationCount = 0;
                taskCounter = 0;
                beginning = true;
                showDuration = false;
            } else if (beginning) {
                beginning = false;
            }

            try {
                setImageForTask();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

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
                initAllTasks(false);
            }

        } else if (e.getKeyCode() == 47) {

            /// MINUS

            if (e.isShiftDown()) {
                fontSizeNumbers -= 5;
            } else {
                numberTasksProSchueler--;
//                initNames();
                initAllTasks(false);

            }

        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
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
        } else if (e.getKeyCode() == KeyEvent.VK_8) {
            actualKlasse = KlassenId.eight;
            initNames();
            initAllTasks(true);
        } else if (e.getKeyCode() == KeyEvent.VK_9) {
            if (actualKlasse == KlassenId.nineA || actualKlasse == KlassenId.eight) {
                actualKlasse = KlassenId.nineB;
            } else {
                actualKlasse = KlassenId.nineA;
            }
            initNames();
            initAllTasks(true);
        } else if (e.getKeyCode() == KeyEvent.VK_0) {
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
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
            operation = Operations.divide;
            initAllTasks(true);
        } else if (e.getKeyCode() == KeyEvent.VK_L) {

            limitedToSelectedSeries = !limitedToSelectedSeries;
            initAllTasks(true);

        } else if (e.getKeyCode() == KeyEvent.VK_M) {

            operation = Operations.multiply;
            initAllTasks(true);

        } else if (e.getKeyCode() == KeyEvent.VK_P) {
        } else if (e.getKeyCode() == KeyEvent.VK_Q) {
        } else if (e.getKeyCode() == KeyEvent.VK_R) {

            if (e.isShiftDown()) {
                readImages();
            }
            initNames();

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
            Toolkit.getDefaultToolkit().beep();
        } else if (e.getKeyCode() == KeyEvent.VK_W) {
            writeSettings();
        } else if (e.getKeyCode() == KeyEvent.VK_Z) {

            for (int i = 0; i < 40; i++) {
                int num1 = (int) (Math.random() * 8 + 2);
                int num2 = (int) (Math.random() * 8 + 2);
                MTools.println(num1 + " x " + num2 + " = ");
            }
        }
        display();
    }

    private void initBeginning() {
        iterationCount = 0;
        penalty = 0;
        taskCounter = 0;
        beginning = true;
        showDuration = false;
        countDownCounter = -1;
        resetTimerStart();
//        countDown = new MyCountDown(this);
//        setCountDown(MyCountDown.countDownFrom);
    }

    private boolean handleDown() {

        drawSettings = false;
        drawStatistics = false;
        drawHelp = false;

        if (beginning) {

            beginning = false;
            shallWriteHighScore = false;

            if (taskCounter == 0 && !timeStartIsReseted) {

                MTools.println("start timer ... " + taskCounter);
                countDown = new MyCountDown(this);
                timeStartIsReseted = true;
                resetTimerStart();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
//                            MTools.println( "running:" );
                        repaint();
                    }
                }, 0, 500);
            }
            return true;
        }

        if (showDuration) {
            return true;
        }

        iterationCount++;

        if (iterationCount % 2 > 0) {

            taskCounter++;

            if (taskCounter >= alleKlassen.get(actualKlasse).getNumberTasks()) {

                showDuration = true;
                taskCounter = numberTasksProSchueler;
                finalDeltaT = deltaT;

                int numSchueler = alleKlassen.get(actualKlasse).size();
                int numTasks = numberTasksProSchueler * numSchueler;

                long newHighScore = (long) ((double) finalDeltaT / (double) numTasks);

//                MTools.println("numTasks:     " + numTasks + " newHigScore " + newHighScore);
//                MTools.println("highScore:    " + alleKlassen.get(actualKlasse).highScore);
//                MTools.println("newHighScore: " + newHighScore);

                if (!debugMode && pinnedName.length() == 0 && newHighScore < alleKlassen.get(actualKlasse).highScore) {
//                    MTools.println("better high score ...");
                    alleKlassen.get(actualKlasse).highScore = newHighScore;
                }
                shallWriteHighScore = true;

                timer.cancel();
            }

            countDown = new MyCountDown(this);

            try {
                setImageForTask();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

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
            }
            countDownCounter = -1;
        }

        return false;
    }

    private void handleWrong() {

        if (drawStatistics && drawSettings) {
            return;
        }
        penalty += MyCountDown.countDownFrom;
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
        if (operation == 0) {
            if (task.number1 > task.number2) {
                bgImage = ImageIO.read(imagesMatrix[task.number2][task.number1]);
            } else {
                bgImage = ImageIO.read(imagesMatrix[task.number1][task.number2]);
            }
        } else if (operation == 1) {

            int ind1 = task.number1 / task.number2;
            int ind2 = task.number2;

            if (ind1 > ind2) {
                bgImage = ImageIO.read(imagesMatrix[ind2][ind1]);
            } else {
                bgImage = ImageIO.read(imagesMatrix[ind1][ind2]);
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
            bgImage = ImageIO.read(imagesMatrix[7][8]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fireNext() {
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
//            MTools.println( "keyReleased ESC " );
        }
    }

    /// main for testing
    public static void main(String[] args) {

        frame = new JFrame();
        frame.setLayout(new GridLayout());
        frame.setBounds(0, 0, 1280, 800);
        MatheTrainer trainer = new MatheTrainer();
        frame.add(trainer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //frame.setUndecorated(true);
        frame.setVisible(true);
    }

}
