import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

interface SliderPanelListener {

    void event(MouseEvent e);
}

class MSlider extends JPanel {

    private boolean isDouble = false;
    private double value;
    private double max;
    private double min;
    private Ellipse2D.Double nob = new Ellipse2D.Double();
    private Color nobColor = Color.WHITE;
    private double pMin;
    private double pMax;
    private double inset;
    private double increment = 1.0;
    private String label = "";

    public MSlider(int min, int max, int v) {

        set(min, max, v);
        addListeners();
    }

    public MSlider(double min, double max, double v) {
        isDouble = true;
        set(min, max, v);
        addListeners();
    }

    public void setBlockIncrement(double inc) {
        increment = inc;
    }

    public void setLabel(String str) {
        label = str;
    }

    private void addListeners() {

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                if (nob.contains(e.getPoint())) {
                    nobColor = Color.ORANGE;
                } else {
                    nobColor = Color.WHITE;
                }
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                double v = posToVal(e.getX());
                if ((v % increment) < (increment / 20.0)) {
                    value = (int) (v / increment) * increment;
                    informInterested(e);
                }
                if (value > max) {
                    value = max;
                }
                if (value < min) {
                    value = min;
                }
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);

            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
            }
        });
    }

    private void set(double mi, double ma, double v) {
        min = mi;
        max = ma;
        value = v;
    }

    @Override
    public void paint(Graphics g) {

        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        inset = 20.0;
        pMax = getWidth() - 2 * inset;
        pMin = inset;
        double thickness = 1.8;
        double yPos = getHeight() / 2 - thickness;
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fill(new Rectangle2D.Double(pMin, yPos, pMax, 2 * thickness));
        double pValue = valToPos(value);
        g2d.setStroke(new BasicStroke(3));
        nob = new Ellipse2D.Double(pValue - 6 * thickness - 2, yPos - 2 * thickness, 6 * thickness, 6 * thickness);
        g2d.draw(nob);
        g2d.setColor(nobColor);
        g2d.fill(new Ellipse2D.Double(pValue - 6 * thickness - 2, yPos - 2 * thickness, 6 * thickness, 6 * thickness));

        g2d.setColor(Color.GRAY);
        if (isDouble) {
            g2d.drawString("" + min, (float) (pMin - 6 * thickness - 2), (float) yPos + 30.0f);
            g2d.drawString("" + max, (float) pMax, (float) yPos + 30.0f);
            float sw = (float) g2d.getFontMetrics().getStringBounds(label + " [" + value + "]", g2d).getWidth() / 2.0f;
            g2d.drawString(label + " [" + value + "]", (float) (getWidth() / 2.0f - sw), (float) yPos - 15.0f);
        } else {
            g2d.drawString("" + (int) min, (float) (pMin - 6 * thickness - 2), (float) yPos + 30.0f);
            g2d.drawString("" + (int) max, (float) pMax, (float) yPos + 30.0f);
            float sw = (float) g2d.getFontMetrics().getStringBounds(label + " [" + (int) value + "]", g2d).getWidth() / 2.0f;
            g2d.drawString(label + " [" + (int) value + "]", (float) (getWidth() / 2.0f - sw), (float) yPos - 15.0f);
        }
    }

    private double valToPos(double val) {
        double m = (pMax + inset - pMin) / (max - min);
        double n = pMin - m * min;
        return m * val + n;
    }

    private double posToVal(double val) {
        double m = (max - min) / (pMax - pMin);
        double n = min - m * pMin;
        return m * val + n;
    }

    private List<SliderPanelListener> listeners = new ArrayList<>();

    public void addListener(SliderPanelListener toAdd) {
        listeners.add(toAdd);
    }

    public void informInterested(MouseEvent e) {
        for (SliderPanelListener hl : listeners)
            hl.event(e);
    }

    public double getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}