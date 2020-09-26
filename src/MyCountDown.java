import java.util.Timer;

public class MyCountDown extends Timer {

    static int countDownFrom = 5;

    public MyCountDown(MatheTrainer matheTrainer) {
        super();
        schedule(new CountDownTask(matheTrainer, countDownFrom), 0, 1000);
    }
}
