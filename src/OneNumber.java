import java.awt.*;

public class OneNumber extends Rectangle {

    public static Color niceGreen = new Color(113, 197, 37);
    String name;

    public OneNumber(String nameIn, int xIn, int yIn) {

        height = 60;
        x = xIn;
        y = yIn;
        width = 60;

        name = nameIn;
    }

    @Override
    public boolean contains(Point p) {
        p.y += height - 8;
        p.x += 13;
        return super.contains(p);
    }

    public void draw(Graphics2D g2d) {

        g2d.setFont(new Font("Arial", Font.PLAIN, 60));
        g2d.setColor(Color.ORANGE);
        g2d.drawString(name, x, y);
        g2d.setColor(Color.WHITE);
        if (ShimpTest.coverOn) {
            g2d.fill(new Rectangle(x - 13, y + 8 - height, width, height));
        } else {
            g2d.draw(new Rectangle(x - 13, y + 8 - height, width, height));
        }
    }

    public int getDist(OneNumber oneNumber) {

        double dx = this.x - oneNumber.x;
        double dy = this.y - oneNumber.y;

        return (int) Math.sqrt(dx * dx + dy * dy);
    }
}
