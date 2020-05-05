import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class SliderPanel extends JPanel {

    public SliderPanel() {

        setLayout(new GridLayout(5, 1));
        setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        MSlider ipSlider = new MSlider(0.0, 1.0, 0.5);
        ipSlider.setBlockIncrement(0.5);
        ipSlider.setLabel("infection probability");
        add(ipSlider);

        MSlider qtSlider = new MSlider(400, 700, 500);
        qtSlider.setBlockIncrement(100);
        add(qtSlider);

        add(new MSlider(400, 800, 800));
        add(new MSlider(400, 700, 400));
        add(new MSlider(400, 700, 450));
    }

    public static void main(String[] args) {

        JFrame f = new JFrame();
        f.add(new SliderPanel());
        f.setBounds(10, 10, 500, 500);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
