import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Operations {

    static final short ON = 1;
    static final short OFF = 0;
    static final short multiply = 0;
    static final short divide = 1;
    static final short plus = 2;
    static final short minus = 3;

    static short[] switchedOn = new short[4];

    static void initOperations() {
        for (int i = 0; i < switchedOn.length; i++) {
            switchedOn[i] = 1;
        }
    }

    public static short getRandomOperation() {

        short check;
        int i = 0;
        while (switchedOn[check = (short) (Math.random() * 4)] == 0) {
            ;
        }
        return check;
    }

    static void switchOperationOnOff(short operation, short onOff) {
        switchedOn[operation] = onOff;
    }

    static void toggleOperationOnOff(short operation) {

        if (switchedOn[operation] == 0) {
            switchedOn[operation] = 1;
        } else if (switchedOn[operation] == 1) {
            switchedOn[operation] = 0;
        }
        checkIfOneIsOn();
    }

    private static void checkIfOneIsOn() {

        int count = 0;
        for (int i = 0; i < 4; i++) {
            if( switchedOn[i] == 0 ) count++;
        }
        /// multiplication is on
        if(count == 4 ) switchedOn[0] = 1;
    }

    public static short isOn(short op) {
        return switchedOn[op];
    }

    public static void write(ObjectOutputStream os) throws IOException {
        for (int i = 0; i < 4; i++) {
            os.writeShort(switchedOn[i]);
        }
    }

    public static void read(ObjectInputStream in) throws IOException {
        for (int i = 0; i < 4; i++) {
            switchedOn[i] = in.readShort();
        }
    }
}
