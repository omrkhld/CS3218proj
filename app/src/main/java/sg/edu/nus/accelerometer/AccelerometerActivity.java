package sg.edu.nus.accelerometer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import sg.edu.nus.data.SensorDBHelper;
import sg.edu.nus.data.SensorsContract;
import sg.edu.nus.oztrafficcamera.R;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener {

    /*
    1. Create an instance of the sensor service. Provide methods to access and list sensors,
     register and unregister sensor event listeners, and acquire orientation info.
     Also has some constants to report sensor accuracy, set data acquisition rates and calibrate sensor.
    */
    private SensorManager sensorManager;
    private double        ax, ay, az;
    long mLastTime = 0;
    long curTime   = 0;
    long diff      = 0;

    private double minX, maxX, minY, maxY, minZ, maxZ; //for calibration
    int countRounds;

    private TextView textview_ax, textview_ay, textview_az, textview_accelerometer_timestamp;
    private TextView textview_accelerometer_fps;
    private TextView textview_lag;
    private Boolean  calibratePressed;
    private Boolean  isStabilised;
    private Boolean  isLargeChanges;
    private Boolean  foundShake;
    private Sensor   mAcc;

    AccelerometerReading reading;

    long startTime = 0L;
    long lag       = 0L;

    public static final int THRESHOLD_NUM_AXES_WITH_LARGE_CHANGES = 2;

    Button startRecording, stopRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        calibratePressed = false;
        foundShake = false;

        textview_ax = (TextView) findViewById(R.id.textview_ax);
        textview_ay = (TextView) findViewById(R.id.textview_ay);
        textview_az = (TextView) findViewById(R.id.textview_az);
        textview_accelerometer_timestamp = (TextView) findViewById(R.id.textview_accelerometer_timestamp);
        textview_accelerometer_fps = (TextView) findViewById(R.id.textview_accelerometer_fps);
        textview_lag = (TextView) findViewById(R.id.textview_lag);

        startRecording = (Button) findViewById(R.id.btn_start_record_acc);
        stopRecording = (Button) findViewById(R.id.btn_stop_record_acc);
        stopRecording.setEnabled(false);
        // Use with getSystemService to retrieve an android.hardware.SensorManager for accessing sensors.
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Use method getDefaultSensor to get the default sensor for a given type.
        // Note that the returned sensor could be a composite sensor, and its data
        // could be averaged or filtered.
        if (mAcc != null) {
            sensorManager.registerListener(this,
                    mAcc, //Create instance of specific sensor
                    SensorManager.SENSOR_DELAY_UI);
        } else {
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /*
    * Starts the calibration after stabilisation
    * */
    public void start_calibrate_acc(View view) {
        startRecording.setEnabled(false);
        startRecording.setText(R.string.RECORDING);
        stopRecording.setEnabled(true);
        calibratePressed = true;
        foundShake = false;
        countRounds = 3;
        lag = 0;
        maxX = minX = ax;
        maxY = minY = ay;
        maxZ = minZ = az;

        startTime = System.currentTimeMillis();
        long[] vibratePattern = {0, 250, 0};

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        boolean isVibrator = vibrator.hasVibrator();
        if (isVibrator) {
            vibrator.vibrate(vibratePattern, -1); // -1 for not repeating
        } else {
            Toast.makeText(this, "This calibration requires a vibrator to work", Toast.LENGTH_SHORT).show();
        }

    }

    public void stop_calibrate_acc(View view) {
        startRecording.setEnabled(true);
        startRecording.setText(R.string.START_RECORDING);
        stopRecording.setEnabled(false);
        calibratePressed = false;
        foundShake = false;

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, mAcc, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            curTime = System.currentTimeMillis();
            diff = curTime - mLastTime;
            textview_accelerometer_fps.setText(String.format("%.2f", 1.0 / diff * 1000));
            mLastTime = curTime;

            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];

            textview_ax.setText(String.valueOf(ax));
            textview_ay.setText(String.valueOf(ay));
            textview_az.setText(String.valueOf(az));

            textview_accelerometer_timestamp.setText(String.valueOf(curTime));

            //Loop a few rounds to find stable values
            if (countRounds > 0) {
                if (ax < minX) {
                    minX = ax;
                } else {
                    maxX = ax;
                }

                if (ay < minY) {
                    minY = ay;
                } else {
                    maxY = ay;
                }

                if (az < maxZ) {
                    minZ = az;
                } else {
                    maxZ = az;
                }
                countRounds--;
            } else {
                isStabilised = true;
            }

            Log.v("AX min and max:", minX + " " + maxX);
            Log.v("AY min and max:", minY + " " + maxY);
            Log.v("AZ min and max:", minZ + " " + maxZ);

            reading = new AccelerometerReading(curTime, ax, ay, az, lag);
            if (calibratePressed && isStabilised) {
                isLargeChanges = detectLargeChange(ax, ay, az);
                Log.v("Large change", String.valueOf(isLargeChanges));

                if (isLargeChanges) {
                    //Modify the lag
                    lag = curTime - startTime;
                    textview_lag.setText(String.format("%d", lag));
                    reading.lag_in_ms = lag;
                    foundShake = true;
                    isStabilised = false;
                    calibratePressed = false;
                }
            }
            if (foundShake){
                //Starts asyncTask to write to database
                WriteToDatabaseTask task = new WriteToDatabaseTask(this);
                task.execute(reading);
            }

        }

    }

    // Returns true if at least n out of 3 of ax, ay or az deviates from their normal range
    private Boolean detectLargeChange(double curr_ax, double curr_ay, double curr_az) {
        int countLargeChanges = 0;

        if (curr_ax < minX || curr_ax > maxX){
            countLargeChanges++;
        }
        if (curr_ay < minY || curr_ay > maxY){
            countLargeChanges++;
        }
        if (curr_az < minZ || curr_az > maxZ){
            countLargeChanges++;
        }
        return countLargeChanges >= THRESHOLD_NUM_AXES_WITH_LARGE_CHANGES; //
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void view_acc_log(View view) {
        Intent intent = new Intent(this, AccelerometerDBLog.class);
        startActivity(intent);
    }

    class WriteToDatabaseTask extends AsyncTask<AccelerometerReading, Void, Long> {
        private Context ctx;

        public WriteToDatabaseTask(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected Long doInBackground(AccelerometerReading... params) {
            //Get DBHelper to write to database
            SensorDBHelper helper = new SensorDBHelper(ctx);
            SQLiteDatabase db     = helper.getWritableDatabase();

            if (params.length == 0) {
                return null;
            }

            AccelerometerReading reading = params[0];
            //Put in the values within a ContentValues.
            ContentValues values = new ContentValues();
            values.clear();
            values.put(SensorsContract.AccelerometerEntry.COLUMN_TIMESTAMP, reading.getTimestampOfSample() - reading.lag_in_ms);
            values.put(SensorsContract.AccelerometerEntry.COLUMN_AX, reading.getAx());
            values.put(SensorsContract.AccelerometerEntry.COLUMN_AY, reading.getAy());
            values.put(SensorsContract.AccelerometerEntry.COLUMN_AZ, reading.getAz());

            //Insert the values into the Table for Accelerometer
            db.insertWithOnConflict(
                    SensorsContract.AccelerometerEntry.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE);

            return reading.getTimestampOfSample();

        }
    }
}
