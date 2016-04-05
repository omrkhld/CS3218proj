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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import sg.edu.nus.data.SensorDBHelper;
import sg.edu.nus.data.SensorsContract;
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
    private Sensor mAcc;


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
        mAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Use method getDefaultSensor to get the default sensor for a given type.
        // Note that the returned sensor could be a composite sensor, and its data
        // could be averaged or filtered.
        if (mAcc != null) {
            sensorManager.registerListener(this,
                    mAcc, //Create instance of specific sensor
                    SensorManager.SENSOR_DELAY_UI);
        } else{

        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_accelerometer);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                The first value indicates the number of milliseconds to wait before turning the vibrator on.
                The next value indicates the number of milliseconds for which to keep the vibrator on before
                turning it off. Subsequent values alternate between durations in milliseconds to turn the
                vibrator off or to turn the vibrator on.
                 */
                long [] vibratePattern = {0, 250, 0};
                calibratePressed = true;

                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(vibratePattern, -1); // -1 for not repeating
                lagTime = System.currentTimeMillis();
            }
        });
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

                AccelerometerReading reading = new AccelerometerReading(timestamp, ax, ay, az);

                //Starts asyncTask to write to database
                WriteToDatabaseTask task = new WriteToDatabaseTask(this);
                task.execute(reading);

            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void view_acc_log(View view){
        Intent intent = new Intent(this, AccelerometerDBLog.class);
        startActivity(intent);
    }

    class WriteToDatabaseTask extends AsyncTask<AccelerometerReading, Void, Long>{
        private Context ctx;

        public WriteToDatabaseTask(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected Long doInBackground(AccelerometerReading... params) {
            //Get DBHelper to write to database
            SensorDBHelper helper = new SensorDBHelper(ctx);
            SQLiteDatabase db = helper.getWritableDatabase();

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

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            textview_lag.setText(String.valueOf(aLong));

        }
    }
}
