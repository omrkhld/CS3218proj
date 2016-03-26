package sg.edu.nus.accelerometer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import sg.edu.nus.oztrafficcamera.R;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener{

    /*
    1. Create an instance of the sensor service. Provide methods to access and list sensors,
     register and unregister sensor event listeners, and acquire orientation info.
     Also has some constants to report sensor accuracy, set data acquisition rates and calibrate sensor.
    */
    private SensorManager sensorManager;
    private double ax, ay, az;
    long mLastTime = 0;

    private TextView textview_ax, textview_ay, textview_az, textview_accelerometer_timestamp;
    private TextView  textview_accelerometer_fps;
    private TextView textview_lag;
    private Boolean calibratePressed;

    long lagTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        calibratePressed = false;

        textview_ax = (TextView) findViewById(R.id.textview_ax);
        textview_ay = (TextView) findViewById(R.id.textview_ay);
        textview_az = (TextView) findViewById(R.id.textview_az);

        textview_accelerometer_timestamp = (TextView) findViewById(R.id.textview_accelerometer_timestamp);
        textview_accelerometer_fps = (TextView) findViewById(R.id.textview_accelerometer_fps);
        textview_lag = (TextView) findViewById(R.id.textview_lag);


        // Use with getSystemService to retrieve an android.hardware.SensorManager for accessing sensors.
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Use method getDefaultSensor to get the default sensor for a given type.
        // Note that the returned sensor could be a composite sensor, and its data
        // could be averaged or filtered.
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), //Create instance of specific sensor
                    SensorManager.SENSOR_DELAY_UI);
        } else{

        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                /*
                The first value indicates the number of milliseconds to wait before turning the vibrator on.
                The next value indicates the number of milliseconds for which to keep the vibrator on before
                turning it off. Subsequent values alternate between durations in milliseconds to turn the
                vibrator off or to turn the vibrator on.
                 */
                long [] vibratePattern = {0, 250, 0};
                lagTime = System.currentTimeMillis();
                calibratePressed = true;

                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(vibratePattern, -1);
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){

            long timestamp = System.currentTimeMillis();
            long diff = timestamp - mLastTime;
            textview_accelerometer_fps.setText(String.format("%.2f",
                    1.0/diff*1000) );
            mLastTime = timestamp;

            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];

            textview_ax.setText(String.valueOf(ax));
            textview_ay.setText(String.valueOf(ay));
            textview_az.setText(String.valueOf(az));

            textview_accelerometer_timestamp.setText(String.valueOf(timestamp));

            if (calibratePressed){
                textview_lag.setText(String.format("%d", diff));
                calibratePressed = false;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
