import java.util.Timer;

public class MyCountDown extends Timer {

    public MyCountDown(MatheTrainer matheTrainer, int countDownFromIn) {
        schedule(new CountDownTask(matheTrainer, countDownFromIn), 0, 1000);
    }
}
