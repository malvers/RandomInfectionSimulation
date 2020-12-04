//import mratools.System.out;

import java.util.ArrayList;

public class Reihen extends ArrayList<Boolean> {

    public Reihen(int limit) {
        for (int i = 0; i < limit; i++) {
            add(true);
        }
    }

    public boolean isOn(int i) {
        return get(i);
    }

    @Override
    public Boolean set(int index, Boolean element) {
        return super.set(index, element);
    }
}
