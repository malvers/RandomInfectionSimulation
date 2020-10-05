import java.util.Timer;

public class MyCountDown extends Timer {

    static int countDownFrom = 9;

    public MyCountDown(MatheTrainer matheTrainer) {
        schedule(new CountDownTask(matheTrainer, countDownFrom), 0, 1000);
    }
}
