import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Random;

public class CoronaWorld extends JPanel implements IRunner {


    private ArrayList<Individual> individuals = new ArrayList();
    static Color niceGreen = new Color(140,200,0);

    public ArrayList<Distribution> getDistributions() {
        return distributions;
    }

    private ArrayList<Distribution> distributions = new ArrayList<>();

    private PlayGround coronaPlayGround = null;
    private long running = 0;
    private Thread thread;
    private int generation = 0;
    private boolean active = false;
    private int maxInfected = 0;
    private Random randomGenerator;

    /// constructor
    public  CoronaWorld() {
        randomGenerator = new Random(System.currentTimeMillis());
        initIndividuals();
    }

    /// constructor
    public CoronaWorld(PlayGround cpg) {

        randomGenerator = new Random(System.currentTimeMillis());
        coronaPlayGround = cpg;
        setFocusable(true);
        setBackground(Color.BLACK);

        initIndividuals();

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    oneInfectionStep();
                } else if (e.getKeyCode() == KeyEvent.VK_A) {
                    coronaPlayGround.statistics("00:00:00");
                } else if (e.getKeyCode() == KeyEvent.VK_I) {
                    initIndividuals();
                } else if (e.getKeyCode() == KeyEvent.VK_S) {
                    startStop();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                requestFocus();
                coronaPlayGround.deactivateAll();
                active = true;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (SwingUtilities.isRightMouseButton(e)) {
                    coronaPlayGround.resetAll();
                } else {
                    coronaPlayGround.startStopAll();
                }
            }
        });
    }

    @Override
    public void run() {

        while (running-- > 0) {
//            MUtilityTools.pauseMillis(delay);
            oneInfectionStep();
            running = allImmune();
            if (running == 0) {
                coronaPlayGround.done();
            }
            repaint();
        }
    }

    @Override
    public void start(long l) {
//        System.out.println( "[" + getClass() + "]->start " + l);
        if (l <= 0) {
            return;
        }
        running = l;
        thread = new Thread(this);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        repaint();
    }

    @Override
    public void stop() {
//        System.out.println( "[" + getClass() + "]->stop ");
        if (thread != null) {
            running = 0;
            // wait for completion
            while (thread.isAlive()) {
                // the show must go on ...
            }
        }
    }

    @Override
    public void paint(Graphics g) {

        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        drawIndividuals(g2d);
        drawLegende(g2d);
        drawCurves(g2d);
    }

    private void drawIndividuals(Graphics2D g2d) {

        for (Individual ind : individuals) {

            Rectangle2D.Double is;
            is = new Rectangle.Double(ind.box.x - 3, ind.box.y - 3, ind.box.width, ind.box.height);
            if (ind.isInfected()) {
                g2d.setColor(Color.RED);
            } else if (ind.isImmune()) {
                g2d.setColor(niceGreen);
            } else {
                g2d.setColor(Color.WHITE);
            }
            g2d.fill(is);
//            g2d.draw(is);
        }
    }

    private void drawCurves(Graphics2D g2d) {

        double worldSize = PlayGround.worldSize;
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fill(new Rectangle2D.Double(worldSize, 0, worldSize, worldSize));

        int numSteps = distributions.size();
        double w = worldSize;
        double wBar = w / numSteps;

        for (int i = 0; i < distributions.size(); i++) {

            Distribution d = distributions.get(i);
            double x = i * wBar + w;

            g2d.setColor(Color.RED);
            double infected = worldSize * d.infected;
            double y = worldSize - infected;
            g2d.fill(new Rectangle2D.Double(x, y, wBar, infected));

            g2d.setColor(Color.WHITE);
            double susceptible = worldSize * d.susceptible;
            y -= susceptible;
            g2d.fill(new Rectangle2D.Double(x, y, wBar, susceptible));

            g2d.setColor(niceGreen);
            double immune = worldSize * d.immune;
            y -= immune;
            g2d.fill(new Rectangle2D.Double(x, y, wBar, immune));


        }
    }

    private void drawLegende(Graphics2D g2d) {

        if (running > 0) {
            g2d.setColor(Color.BLACK);
        } else {
            g2d.setColor(Color.GRAY);
        }
        g2d.fill(new Rectangle2D.Double(0, 0, 50, 90));
        g2d.setColor(Color.GRAY);
        int yPos = 20;
        int yInc = 20;
        if (active) {
            g2d.setColor(Color.ORANGE);
        } else {
            g2d.setColor(Color.LIGHT_GRAY);
        }
        g2d.drawString("" + generation, 2, yPos);
//        yPos += yInc;
//        g2d.setColor(Color.LIGHT_GRAY);
//        g2d.drawString("delay " + delay, 10, yPos);
        g2d.setColor(Color.WHITE);
        yPos += yInc;
        g2d.drawString("" + getNumberSusceptible(), 2, yPos);
        g2d.setColor(Color.RED);
//        yPos += yInc;
//        g2d.drawString("" + getNumberInfected(), 2, yPos);
        yPos += yInc;
        g2d.drawString("" + maxInfected, 2, yPos);
        g2d.setColor(niceGreen);
        yPos += yInc;
        g2d.drawString("" + getNumberImmune(), 2, yPos);
//        g2d.setColor(Color.ORANGE);
//        yPos += yInc;
//        int sum = getNumberSusceptible() + getNumberInfected() + getNumberImmune() - numIndividuals;
//        g2d.drawString("check " + sum, 10, yPos);
//        g2d.setColor(Color.CYAN);
//        yPos += yInc;
//        g2d.drawString("" + (int) (infectionPropability * 100) + " %", 2, yPos);

        /// percent
//        g2d.setColor(Color.LIGHT_GRAY);
//        yPos += yInc;
//        g2d.drawString("healthy [%] " + (int) (100 * getNumberSusceptiblePercent()), 10, yPos);
//        g2d.setColor(Color.RED);
//        yPos += yInc;
//        g2d.drawString("infected [%] " + (int) (100 * getNumberInfectedPercent()), 10, yPos);
//        g2d.setColor(Color.GREEN);
//        yPos += yInc;
//        g2d.drawString("immune [%] " + (int) (100 * getNumberImmunePercent()), 10, yPos);
    }

    long allImmune() {
        if (getNumberImmune() + getNumberSusceptible() == PlayGround.numIndividuals) {
            return 0;
        }
        return Long.MAX_VALUE;
    }

    private void updateInfected() {

        for (Individual ind : individuals) {
            if (ind.isInfected()) {
                ind.incInfectedTime();
            }
        }
        if (getNumberInfected() > maxInfected) {
            maxInfected = getNumberInfected();
        }
    }

    public void startStop() {
        if (running > 0) {
            stop();
        } else {
            start(Long.MAX_VALUE);
        }
    }

    public void setActive(boolean b) {
        active = b;
        repaint();
    }

    private int getNumberSusceptible() {
        int count = 0;
        for (Individual ind : individuals)
            if (ind.isSusceptible()) {
                count++;
            }
        return count;
    }

    private double getNumberSusceptiblePercent() {
        return getNumberSusceptible() / (double) PlayGround.numIndividuals;
    }

    private int getNumberImmune() {
        int count = 0;
        for (Individual ind : individuals)
            if (ind.isImmune()) {
                count++;
            }
        return count;
    }

    private double getNumberImmunePercent() {
        return getNumberImmune() / (double) PlayGround.numIndividuals;
    }

    private int getNumberInfected() {
        int count = 0;
        for (Individual ind : individuals)
            if (ind.isInfected()) {
                count++;
            }
        return count;
    }

    private double getNumberInfectedPercent() {
        return getNumberInfected() / (double) PlayGround.numIndividuals;
    }

    public int getMaxInfected() {
        return maxInfected;
    }

    public int getMaxGeneration() {
        return generation;
    }

    /// ///////////////////////////////////////////////////////////////////////////////
    /// following are the important methods

    void oneInfectionStep() {
        move();
        infect();
        updateInfected();
    }

    void initIndividuals() {

        double worldSize = PlayGround.worldSize;
        maxInfected = 0;
        generation = 0;
        distributions.clear();
        individuals.clear();
        Individual ind = new Individual();
        ind.box.x = worldSize / 2;
        ind.box.y = worldSize / 2;
        ind.incInfectedTime();
        individuals.add(ind);

        for (int i = 0; i < PlayGround.numIndividuals - 1; i++) {

            ind = new Individual();
            ind.box.x = Math.random() * worldSize;
            ind.box.y = Math.random() * worldSize;
            individuals.add(ind);
        }

        repaint();
    }

    private void move() {

        generation++;
        double worldSize = PlayGround.worldSize;
        for (Individual ind : individuals) {

//            ind.box.x += Math.random() * CoronaPlayGround.move - CoronaPlayGround.moveHalf;
//            ind.box.y += Math.random() * CoronaPlayGround.move - CoronaPlayGround.moveHalf;

            ind.box.x += randomGenerator.nextDouble() * PlayGround.move - PlayGround.moveHalf;
            ind.box.y += randomGenerator.nextDouble() * PlayGround.move - PlayGround.moveHalf;

            if (ind.box.x > worldSize) {
                ind.box.x = 0;
            }
            if (ind.box.y > worldSize) {
                ind.box.y = 0;
            }
            if (ind.box.x < 0) {
                ind.box.x = worldSize;
            }
            if (ind.box.y < 0) {
                ind.box.y = worldSize;
            }
        }
    }

    private void infect() {

        for (int i = 0; i < individuals.size(); i++) {
            Individual ind1 = individuals.get(i);
            for (int j = i + 1; j < individuals.size(); j++) {
                Individual ind2 = individuals.get(j);
                if (ind2.isImmune()) {
                    continue;
                }
                if (!ind1.isInfected()) {
                    continue;
                }
                infect(ind1, ind2);
            }
        }

        Distribution d = new Distribution();
        d.susceptible = getNumberSusceptiblePercent();
        d.infected = getNumberInfectedPercent();
        d.immune = getNumberImmunePercent();
        distributions.add(d);
    }

    /// quadratur des Kreises
    private static final double maxDist = Math.sqrt(1.0 / Math.PI) * PlayGround.infectionRadius;
    private void infect(Individual ind1, Individual ind2) {

        boolean distance = false;

        if (distance) {

            double dx = ind1.box.x - ind2.box.x;
            double dy = ind1.box.y - ind2.box.y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > maxDist) {
                return;
            }
        } else {

            if (!ind1.box.contains(ind2.box.x, ind2.box.y)) {
                return;
            }
        }

        if (!(Math.random() < PlayGround.infectionProbability)) {
            return;
        }

        ind2.incInfectedTime();
    }
}
