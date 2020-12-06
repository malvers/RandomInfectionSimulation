import mratools.MTools;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class OneNumber {

    public static Color niceGreen = new Color(113, 197, 37);
    String name;
    private Rectangle rect;
    int x;
    int y;
    int width;
    int height;

    public OneNumber(String nameIn, int xIn, int yIn, int heightIn) {

        x = xIn;
        y = yIn;
        height = heightIn;
        width = height;

        name = nameIn;
    }

    public boolean contains(Point p) {
        return rect.contains(p);
    }

    public void draw(Graphics2D g2d) {

        g2d.setFont(new Font("Arial", Font.PLAIN, height));
        g2d.setColor(Color.ORANGE);
        g2d.drawString(name, x, y);
        g2d.setColor(Color.WHITE);

        FontMetrics metrics = g2d.getFontMetrics();
        Rectangle2D bounds = metrics.getStringBounds(name, g2d);

        int boxSize = height + 10;
        int dx = (int) ((bounds.getWidth() - boxSize) / 2.0);
        int dy = (int) ((bounds.getHeight() - boxSize) / 2.0);

//        MTools.println("dx: " + dx + " dy: " + dy + " getHeight: " + bounds.getHeight() + " box: " + boxSize);

        rect = new Rectangle(x + dx, (int) (y - dy - (boxSize / 1.2)), boxSize, boxSize);
        if (ShimpTest.coverOn) {
            g2d.fill(rect);
        } else {
            g2d.draw(rect);
        }
    }

    public int getDist(OneNumber oneNumber) {

        double dx = this.x - oneNumber.x;
        double dy = this.y - oneNumber.y;

        return (int) Math.sqrt(dx * dx + dy * dy);
    }
}
