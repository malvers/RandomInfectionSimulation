import mratools.MTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;

public class SliderPanel extends JPanel implements SliderPanelListener {

    private static final String S_NUM_INDIVIDUALS = "number individuals";
    private static final String S_INFECTION_PROBABILITY = "infection probability";
    private static final String S_RECOVER_TIME = "recover time";
    private static final String S_QUARANTINE_PROBABILITY = "quarantine probability";
    private static final String S_QUARANTINE_TIME = "quarantine time";

    private final Plotter plotter;
    private final String storeName;
    private ArrayList<MSlider> allSliders = new ArrayList();
    private String name;

    public SliderPanel(Plotter p) {

        plotter = p;

        setLayout(new GridLayout(5, 1));
        setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        MSlider mSlider = new MSlider(400, 600, 500);
        allSliders.add(mSlider);
        mSlider.setLabel(S_NUM_INDIVIDUALS);
        mSlider.addListener(this);
        mSlider.setBlockIncrement(100);
        add(mSlider);

        mSlider = new MSlider(500, 700, 600);
        allSliders.add(mSlider);
        mSlider.setLabel(S_RECOVER_TIME);
        mSlider.setBlockIncrement(100);
        mSlider.addListener(this);
        add(mSlider);

        mSlider = new MSlider(0.01, 1.0, 1.0);
        allSliders.add(mSlider);
        mSlider.setBlockIncrement(0.5);
        mSlider.setLabel(S_INFECTION_PROBABILITY);
        mSlider.addListener(this);
        add(mSlider);

        mSlider = new MSlider(0.01, 1.0, 1.0);
        allSliders.add(mSlider);
        mSlider.setBlockIncrement(0.5);
        mSlider.setLabel(S_QUARANTINE_PROBABILITY);
        mSlider.addListener(this);
        add(mSlider);

        mSlider = new MSlider(200, 600, 400);
        allSliders.add(mSlider);
        mSlider.setLabel(S_QUARANTINE_TIME);
        mSlider.addListener(this);
        mSlider.setBlockIncrement(200);
        add(mSlider);

        storeName = createFileName();
        try {
            plotter.readData(name);
        } catch (IOException ioException) {
            MTools.println( ":-(   " + name );
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, -1);
    }

    public static void main(String[] args) {

        JFrame f = new JFrame();
        f.add(new SliderPanel(null));
        f.setBounds(10, 10, 500, 500);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    @Override
    public void event(MouseEvent e) {

        String n = createFileName();
        if( !storeName.contains(n) ) {
            name = n;
            try {
                plotter.readData(name);
            } catch (IOException ioException) {
                MTools.println( ":-(   " + name );
            }
        }
    }

    private String createFileName() {

        name = "/Users/malvers/CoronaSimulationData/ns 10 ";

        for (MSlider slider : allSliders) {

            if (slider.getLabel().contains(S_NUM_INDIVIDUALS)) {
                name += "in " + (int)slider.getValue();
            } else if (slider.getLabel().contains(S_RECOVER_TIME)) {
                name += " rt " + (int) slider.getValue();
            } else if (slider.getLabel().contains(S_INFECTION_PROBABILITY)) {
                name += " ip " + Util.myFormatter(slider.getValue() * 100, 5, 2) + "[%] ws 160.0 is 5.0";
            } else if (slider.getLabel().contains(S_QUARANTINE_PROBABILITY)) {
                name += " qp " + Util.myFormatter(slider.getValue() * 100, 5, 2) + "[%]";
            } else if (slider.getLabel().contains(S_QUARANTINE_TIME)) {
                name += " qt " + (int)slider.getValue();
            }
        }
        name += " sf 1.0.simu";

        return name;
    }
}
