import mratools.MTools;

public class MultiplicationTask {

    String name;
    int number1;
    int number2;

    public MultiplicationTask(String nameIn, Reihen reihen, boolean hardCore) {

        /// TODO: consider reihen.size() as limit beyond 9er Reihe

        while (!reihen.isOn(number1 = (int) (Math.random() * 8 + 2)))
            ;

        if (hardCore) {
        while (!reihen.isOn(number2 = (int) (Math.random() * 8 + 2) ))
            ;
        } else {
            number2 = (int) (Math.random() * 8 + 2);
        }

        name = nameIn;
    }

    public void print(int i) {

        MTools.println(i + " name: " + name + " - " + number2 + " x: " + number2);
    }
}
