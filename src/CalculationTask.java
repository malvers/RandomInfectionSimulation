import mratools.MTools;

public class CalculationTask {

    String name;
    int number1;
    int number2;
    private String sOperator;

    public CalculationTask(String nameIn, Reihen reihen, boolean hardCore, int operation) {

        if( operation == 0 ) sOperator = " x ";
        if( operation == 1 ) sOperator = " : ";

        /// TODO: consider reihen.size() as limit beyond 9er Reihe

        while (!reihen.isOn(number1 = (int) (Math.random() * 8 + 2)))
            ;

        if (hardCore) {
            while (!reihen.isOn(number2 = (int) (Math.random() * 8 + 2)))
                ;
        } else {
            number2 = (int) (Math.random() * 8 + 2);
        }

        if( operation == 1) {
            number1 = number2 * number1;
        }

        name = nameIn;
    }

    public void print(int i) {

        String space = "";
        if( i < 10 ) space = " ";
        MTools.println(space  + i + " -> " + number1 + sOperator + number2 + " = ");
    }
}
