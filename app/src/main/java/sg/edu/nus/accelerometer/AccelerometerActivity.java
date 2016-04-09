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
import android.widget.TextView;

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
    long timestamp = 0;
    long diff      = 0;

    private double minX, maxX, minY, maxY, minZ, maxZ; //for calibration
    int countRounds;

    private TextView textview_ax, textview_ay, textview_az, textview_accelerometer_timestamp;
    private TextView textview_accelerometer_fps;
    private TextView textview_lag;
    private Boolean  calibratePressed;
    private Boolean  isStabilised;
    private Boolean  isLargeChanges;
    private Sensor   mAcc;

    long lagTime;

    public static  final int THRESHOLD_NUM_AXES_WITH_LARGE_CHANGES = 2;

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
    public void calibrate_acc(View view) {
        calibratePressed = true;
        countRounds = 5;
        maxX = minX = ax;
        maxY = minY = ay;
        maxZ = minZ = az;

        lagTime = System.currentTimeMillis();
        long[] vibratePattern = {0, 200, 0};

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(vibratePattern, -1); // -1 for not repeating
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

            timestamp = System.currentTimeMillis();
            diff = timestamp - mLastTime;
            textview_accelerometer_fps.setText(String.format("%.2f", 1.0 / diff * 1000));
            mLastTime = timestamp;

            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];

            textview_ax.setText(String.valueOf(ax));
            textview_ay.setText(String.valueOf(ay));
            textview_az.setText(String.valueOf(az));

            textview_accelerometer_timestamp.setText(String.valueOf(timestamp));

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

            if (calibratePressed && isStabilised) {
                isLargeChanges = detectLargeChange(ax, ay, az);
                Log.v("Large change", String.valueOf(isLargeChanges));

                if (isLargeChanges) {
                    textview_lag.setText(String.format("%d", diff));

                    isStabilised = false;
                    calibratePressed = false;
                    AccelerometerReading reading = new AccelerometerReading(timestamp, ax, ay, az);
                    //Starts asyncTask to write to database
                    WriteToDatabaseTask task = new WriteToDatabaseTask(this);
                    task.execute(reading);
                }

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
            values.put(SensorsContract.AccelerometerEntry.COLUMN_TIMESTAMP, reading.getTimestamp());
            values.put(SensorsContract.AccelerometerEntry.COLUMN_AX, reading.getAx());
            values.put(SensorsContract.AccelerometerEntry.COLUMN_AY, reading.getAy());
            values.put(SensorsContract.AccelerometerEntry.COLUMN_AZ, reading.getAz());

            //Insert the values into the Table for Tasks
            db.insertWithOnConflict(
                    SensorsContract.AccelerometerEntry.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE);

            return reading.getTimestamp();

        }

//        @Override
//        protected void onPostExecute(Long aLong) {
//            super.onPostExecute(aLong);
//            textview_lag.setText(String.valueOf(aLong));
//
//        }
    }
}
