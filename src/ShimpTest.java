//import mratools.MTools;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ShimpTest extends JPanel implements MouseListener, KeyListener {

    private static JFrame theFrame;
    private static Clip clip;
    private BufferedImage image;
    private int numNumbers;
    private int frame;
    private boolean isInit = false;

    private ArrayList<OneNumber> allNumbers = new ArrayList<>();
    private String settingsFileName = "ShimpTest.binary.settings";
    private Timer cover;

    int coverCount = 0;

    static boolean coverOn = false;
    private int sequence = 0;

    public ShimpTest() {

        numNumbers = 10;
        frame = 50;
        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);

        try {
            image = ImageIO.read(new File("images/shimpanse.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        readSettings();
    }

    private static void playBuzzer() {

        AudioInputStream audioInputStream = null;
        try {
            clip = null;
            audioInputStream = AudioSystem.getAudioInputStream(new File("sound/Buzzer.wav").getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
        } catch (UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException ignored) {
        }
        clip.start();
    }

    private static void playFanfare() {

        AudioInputStream audioInputStream = null;
        try {
            clip = null;
            audioInputStream = AudioSystem.getAudioInputStream(new File("sound/Fanfare.wav").getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
        } catch (UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException ignored) {
        }
        clip.start();
    }

    private void initShimpNumbers() {

        System.out.println("initShimpNumbers(): ++++++++++++++++++++++++++++++++++++++ ");

        isInit = true;

        allNumbers.clear();

        int minDistInit = 200;

        int whileCount = 0;
        while (true) {

            whileCount++;

            int sumDist = 0;
            int minDist = minDistInit;

            /// create numbers
            allNumbers.clear();
            for (int i = 0; i < numNumbers; i++) {

                int xPos = (int) (Math.random() * (getWidth() - (3.6 * frame))) + frame;
                int yPos = (int) ((int) (Math.random() * (getHeight() - (2 * frame))) + 1.2 * frame);
                allNumbers.add(new OneNumber("" + (i + 1), xPos, yPos));

            }
            /// check for distance
            int count = 0;
            for (int i = 0; i < allNumbers.size(); i++) {
                for (int j = i + 1; j < allNumbers.size(); j++) {
                    int dist = allNumbers.get(i).getDist(allNumbers.get(j));
//                    MTools.println("i " + i + " j: " + j + " - dist: " + dist + " minDist: " + minDist);
                    count++;
                    sumDist += dist;
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
            }

            if (sumDist > count * minDistInit && minDist > 170) {
                System.out.println("whileCount: " + whileCount + " minDist: " + minDist + " count * minDistInit: " + (count * minDistInit) + " sumDist: " + sumDist);
                break;
            }
        }
        repaint();

        cover = new Timer();
        cover.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("Cover " + coverCount);
                        if (coverCount > 0) {
                            coverOn = true;
                            coverCount = 0;
                            repaint();
                            cancel();
                        } else {
                            repaint();
                            coverOn = false;
                            coverCount++;
                        }

                    }
                }, 0, 1000
        );
    }

    private void writeSettings() {

        FileOutputStream f;
        try {
            System.out.println("writeSettings: " + settingsFileName);
            f = new FileOutputStream(settingsFileName);
            ObjectOutputStream os = new ObjectOutputStream(f);
            os.writeInt(theFrame.getX());
            os.writeInt(theFrame.getY());
            os.writeInt(theFrame.getWidth());
            os.writeInt(theFrame.getHeight());
            os.writeInt(numNumbers);

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
            theFrame.setBounds(x, y, w, h);
            numNumbers = in.readInt();

            Operations.read(in);

            in.close();
            f.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

        if (e.getClickCount() > 1) {
            initShimpNumbers();
            return;
        }
        System.out.println("Pos x: " + e.getX() + " y: " + e.getY());

        int clickedOn = -1;
        for (int i = 0; i < allNumbers.size(); i++) {

            OneNumber cand = allNumbers.get(i);

            if (cand.contains(e.getPoint())) {

                clickedOn = i;
                break;
            }
        }

        if (clickedOn == sequence) {
            System.out.println("correct: " + sequence + " clickedOn: " + clickedOn);
            sequence++;
            if (sequence == allNumbers.size()) {
                playFanfare();
                System.out.println("all correct!");
                sequence = 0;
                coverOn = false;
            }
        } else {
            System.out.println("false:   " + sequence + " clickedOn: " + clickedOn + " game over! ");
            playBuzzer();
            coverOn = false;
        }
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

        System.out.println("key: " + e.getKeyCode());
        switch (e.getKeyCode()) {

            case 521:
            case 93:
                numNumbers++;
                if (numNumbers > 9) {
                    numNumbers = 9;
                }
                initShimpNumbers();
                writeSettings();
                repaint();
                break;

            case 45:
            case 47:
                numNumbers--;
                if (numNumbers < 3) {
                    numNumbers = 3;
                }
                initShimpNumbers();
                writeSettings();
                repaint();
                break;

            case KeyEvent.VK_ESCAPE:
                writeSettings();
                System.exit(0);
                break;

            case KeyEvent.VK_SPACE:
                initShimpNumbers();
                repaint();
                break;

            case KeyEvent.VK_C:
                coverOn = !coverOn;
                repaint();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void paint(Graphics g) {

        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(new Color(0, 0, 80));
        g2d.fill(new Rectangle(0, 0, getWidth(), getHeight()));

        if (!isInit) {

            g2d.setFont(new Font("Arial", Font.PLAIN, 100));
            g2d.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            g2d.setColor(Color.WHITE);
            String str = "Beat the shimpanse!";

            FontMetrics metrics = g2d.getFontMetrics();
            int width = (int) metrics.getStringBounds(str, g2d).getWidth();

            g2d.drawString(str, (getWidth() - width) / 2, getHeight() / 2 - 180);
        }

        for (OneNumber oneNumber : allNumbers) {
            oneNumber.draw(g2d);
        }
    }

    public static void main(String[] args) {
        theFrame = new JFrame("Beat the shimp ...");
        theFrame.setBounds(800, 160, 600, 600);
        theFrame.setContentPane(new ShimpTest());
        theFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        theFrame.setVisible(true);
    }
}
