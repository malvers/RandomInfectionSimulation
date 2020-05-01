import java.awt.geom.Rectangle2D;

public class Individual {

    public int getInfectedTime() {
        return infectedTime;
    }

    public void incInfectedTime() {

        infectedTime++;
        if (infectedTime > PlayGround.recoverTime) {
            infectedTime = -1;
        }

        /// quarantine
        if (infectedTime > PlayGround.quarantineTime) {
            if (Math.random() < PlayGround.quarantineProbability) {
                infectedTime = -1;
            }
        }
    }

    private int infectedTime = 0;
    Rectangle2D.Double box = new Rectangle2D.Double();

    public boolean isInfected() {
        return infectedTime > 0;
    }

    public Individual() {
        box.width = PlayGround.infectionRadius;
        box.height = PlayGround.infectionRadius;
    }

    public boolean isImmune() {
        return infectedTime < 0;
    }

    public boolean isSusceptible() {
        return infectedTime == 0;
    }
}
