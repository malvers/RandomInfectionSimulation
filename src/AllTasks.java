import java.util.ArrayList;

public class AllTasks extends ArrayList<CalculationTask> {

    public AllTasks() {
        super(50);
    }

    public void print() {

        for (int i = 0; i < size(); i++) {
            get(i).print(i);
        }
    }
}
