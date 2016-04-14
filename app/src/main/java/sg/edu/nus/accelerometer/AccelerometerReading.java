package sg.edu.nus.accelerometer;

/**
 * Created by delvinlow on 2/4/16.
 */
public class AccelerometerReading {
    private long   timestampOfSample;
    private double ax, ay, az;
    public long lag_in_ms;

    public AccelerometerReading() {
        this.timestampOfSample = 0;
        this.ax = 0;
        this.ay = 0;
        this.az = 0;
        this.lag_in_ms = 0;
    }

    public AccelerometerReading(long timestamp, double ax, double ay, double az, long lag) {
        this.timestampOfSample = timestamp;
        this.ax = ax;
        this.ay = ay;
        this.az = az;
        this.lag_in_ms = lag;
    }

    public long getTimestampOfSample() {
        return timestampOfSample;
    }

    public  void setTimestampOfSample(long timestampOfSample) {
        this.timestampOfSample = timestampOfSample;
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
