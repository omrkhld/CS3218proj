package sg.edu.nus.accelerometer;

/**
 * Created by delvinlow on 2/4/16.
 */
public class AccelerometerReading {
    private long timestamp;
    private double ax, ay, az;

    public AccelerometerReading() {
        this.timestamp = 0;
        this.ax = 0;
        this.ay = 0;
        this.az = 0;
    }

    public AccelerometerReading(long timestamp, double ax, double ay, double az) {
        this.timestamp = timestamp;
        this.ax = ax;
        this.ay = ay;
        this.az = az;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public  void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getAx() {
        return ax;
    }

    public void setAx(double ax) {
        this.ax = ax;
    }

    public double getAy() {
        return ay;
    }

    public void setAy(double ay) {
        this.ay = ay;
    }

    public double getAz() {
        return az;
    }

    public void setAz(double az) {
        this.az = az;
    }
}
