import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ShimpTest extends JPanel implements MouseListener, KeyListener {

    private int numNums;
    private int frame;

    public ShimpTest() {

        numNums = 6;
        frame = 50;
        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("Pos x: " + e.getX() + " y: " + e.getY());
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
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            System.exit(0);
        else if (e.getKeyCode() == KeyEvent.VK_SPACE)
            repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.fill(new Rectangle(0, 0, getWidth(), getHeight()));
        g2d.setFont(new Font("Arial", Font.PLAIN, 40));
        g2d.setColor(Color.ORANGE);
        for (int i = 1; i <  numNums + 1; i++) {
            int xPos = (int) (Math.random() * (getWidth() - (3.6 * frame))) + frame;
            int yPos = (int) ((int) (Math.random() * (getHeight() - (2 * frame))) + 1.2*frame);
            g2d.drawString("" + i, xPos, yPos);
        }
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setBounds(600, 0, 600, 600);
        f.setContentPane(new ShimpTest());
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
