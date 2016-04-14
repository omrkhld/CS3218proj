package sg.edu.nus.all_in_one;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import sg.edu.nus.accelerometer.AccelerometerReading;
import sg.edu.nus.audio.AudioClipListener;
import sg.edu.nus.audio.LoudNoiseDetector;
import sg.edu.nus.data.SensorDBHelper;
import sg.edu.nus.data.SensorDBHelperCombinedCam;
import sg.edu.nus.data.SensorsContract;
import sg.edu.nus.oztrafficcamera.R;

public class AllInOneActivity extends Activity implements SensorEventListener {
    private static final String TAG = "AllInOneActivity";

    private Camera      mCamera;
    private SurfaceView mPreview;
    private Button      captureButton;
    private TextView    lagTimeText;
    private TextView    numPhotosText;
    private static SurfaceHolder previewHolder  = null;
    private static boolean       isStarted      = false;
    private static boolean       inPreview      = false;
    private        int           pictureDelay   = 2000;
    private        int           numPhotos      = 0;
    private        long          startTime      = 0;
    private        long          endTime        = 0;
    private static long          mReferenceTime = 0;
    private CaptureThread   thread;
    private RecordAudioTask task;
    AudioRecorder recorder;
    private static SensorDBHelperCombinedCam helper;

    private SensorManager sensorManager;
    private Sensor        mAcc;
    //Accelerometer variables
    private double        minX, maxX, minY, maxY, minZ, maxZ; //for calibration
    int countRounds;
    private Boolean calibratePressed;
    private Boolean isStabilised;
    private Boolean isLargeChanges;
    private Boolean foundShake;
    private double  ax, ay, az;
    long lag_acc = 0L;
    AccelerometerReading reading;
    public static final int THRESHOLD_NUM_AXES_WITH_LARGE_CHANGES = 2;

    long mLastTime = 0;
    long curTime   = 0;
    long diff      = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_in_one);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = mPreview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // Use with getSystemService to retrieve an android.hardware.SensorManager for accessing sensors.
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        calibratePressed = false;
        foundShake = false;

        // Use method getDefaultSensor to get the default sensor for a given type.
        // Note that the returned sensor could be a composite sensor, and its data
        // could be averaged or filtered.
        if (mAcc != null) {
            sensorManager.registerListener(this,
                    mAcc, //Create instance of specific sensor
                    SensorManager.SENSOR_DELAY_UI);
        } else {
        }

        lagTimeText = (TextView) findViewById(R.id.lag_time);
        numPhotosText = (TextView) findViewById(R.id.num_photos);

        // Add a listener to the Capture button
        captureButton = (Button) findViewById(R.id.capture_button);
        final Context ctx = this;
        helper = new SensorDBHelperCombinedCam(ctx);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStarted) {
                    captureButton.setText("Capture");
                    isStarted = false;
                    recorder.stopRecording();
                } else {

                    isStarted = true;
                    captureButton.setText("Stop");

                    start_mic_recording();
                    start_calibrate_acc();
                }

            }
        });
    }

    public void start_mic_recording() {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

        SensorDBHelper sensorDBHelper = new SensorDBHelper(this);
        task = new RecordAudioTask(this, "Microphone", sensorDBHelper);
        task.execute(new LoudNoiseDetector());
    }

    public void view_aio_log(View view) {
        isStarted = false;
        calibratePressed = false;
        foundShake = false;
        Intent intent = new Intent(this, AllInOneDBLog.class);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();

        mCamera.setPreviewCallback(null);
        if (inPreview) mCamera.stopPreview();
        inPreview = false;
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        releaseCameraAndPreview();
        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void start_calibrate_acc() {
        calibratePressed = true;
        foundShake = false;
        countRounds = 3;
        lag_acc = 0;
        maxX = minX = ax;
        maxY = minY = ay;
        maxZ = minZ = az;

        startTime = System.currentTimeMillis();
        long[] vibratePattern = {0, 250, 0};

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(vibratePattern, -1); // -1 for not repeating

//        start_mic_recording();
    }

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            if (data == null) return;
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) return;

            if (isStarted) {
                long now = System.currentTimeMillis();
                if (now > (mReferenceTime + pictureDelay)) {
                    mReferenceTime = now;
                    thread = new CaptureThread(getApplicationContext(), helper);
                    thread.start();
                }
            }
        }
    };

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(previewHolder);
                mCamera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("surfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size       size       = getBestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            inPreview = true;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    private static Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) result = size;
                }
            }
        }

        return result;
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            curTime = System.currentTimeMillis();
            diff = curTime - mLastTime;
            mLastTime = curTime;

            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];

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

            reading = new AccelerometerReading(curTime, ax, ay, az, lag_acc);
            if (calibratePressed && isStabilised) {
                isLargeChanges = detectLargeChange(ax, ay, az);
                Log.v("Large change", String.valueOf(isLargeChanges));

                if (isLargeChanges) {
                    //Modify the lag
                    lag_acc = curTime - startTime;
                    reading.lag_in_ms = lag_acc;
                    foundShake = true;
                    isStabilised = false;
                    calibratePressed = false;
                }
            }
            if (foundShake) {
                //Starts asyncTask to write to database
                WriteToDatabaseTask task = new WriteToDatabaseTask(this);
                task.execute(reading);
            }

        }

    }

    // Returns true if at least n out of 3 of ax, ay or az deviates from their normal range
    private Boolean detectLargeChange(double curr_ax, double curr_ay, double curr_az) {
        int countLargeChanges = 0;

        if (curr_ax < minX || curr_ax > maxX) {
            countLargeChanges++;
        }
        if (curr_ay < minY || curr_ay > maxY) {
            countLargeChanges++;
        }
        if (curr_az < minZ || curr_az > maxZ) {
            countLargeChanges++;
        }
        return countLargeChanges >= THRESHOLD_NUM_AXES_WITH_LARGE_CHANGES; //
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class CaptureThread extends Thread {
        Context                   ctx;
        SensorDBHelperCombinedCam helper;

        public CaptureThread(Context ctx, SensorDBHelperCombinedCam helper) {
            this.ctx = ctx;
            this.helper = helper;
        }

        @Override
        public void run() {
            Camera.PictureCallback mPicture = new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Long timestamp = System.currentTimeMillis();
                    new MediaSaver(ctx, timestamp, helper).execute(data);
                    mCamera.startPreview();
                }
            };

            startTime = System.currentTimeMillis();
            mCamera.takePicture(null, null, mPicture);
            numPhotos++;
            endTime = System.currentTimeMillis();
            Log.d(TAG, "Num photos taken = " + numPhotos);
            Log.d(TAG, "Lag = " + (endTime - startTime));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lagTimeText.setText(String.valueOf(endTime - startTime));
                    numPhotosText.setText(String.valueOf(numPhotos));
                }
            });
        }
    }

    class RecordAudioTask extends AsyncTask<AudioClipListener, Long, DetailsOfRecording> {
        private final String LOG_TAG = RecordAudioTask.class.getSimpleName();

        private Context context;
        private String  taskName;

        private long startTime = 0;
        SensorDBHelper helper;

        public RecordAudioTask(Context context, String taskName, SensorDBHelper helper) {
            this.context = context;
            this.taskName = taskName;
            this.helper = helper;
        }

        @Override
        protected void onPreExecute() {
            // Tell UI that recording is starting
            super.onPreExecute();
        }

        @Override
        protected DetailsOfRecording doInBackground(AudioClipListener... listeners) {
            DetailsOfRecording recordingDetails = new DetailsOfRecording();

            if (listeners.length == 0) {
                return recordingDetails;
            } else {
                Log.d(LOG_TAG, "Recording");
                AudioClipListener listener = listeners[0];
                recorder = new AudioRecorder(listener, this, context, helper);

                boolean heard = false;
                try {
                    startTime = System.currentTimeMillis();
                    Log.v("START RECORDING", "START RECORDING");
                    recordingDetails = recorder.startRecording();
                } catch (IllegalStateException ise) {
                    Log.e(LOG_TAG, "Failed to record, recorder not setup properly", ise);
                } catch (RuntimeException se) {
                    Log.e(LOG_TAG, "Failed to record, recorder is already being used", se);
                }
//
//                return recordingDetails;
            }
                return recordingDetails;
        }

        @Override
        protected void onPostExecute(DetailsOfRecording result) {
            if (result.successRecording) {
                long lag = result.lag;
                long duration = result.endTime - result.startTime;
            } else {
            }
            setDoneMessage();
            super.onPostExecute(result);
        }

        private void setDoneMessage() {
        }

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
            values.put(SensorsContract.AccelerometerEntry.COLUMN_TIMESTAMP, reading.getTimestampOfSample());
            values.put(SensorsContract.AccelerometerEntry.COLUMN_AX, reading.getAx());
            values.put(SensorsContract.AccelerometerEntry.COLUMN_AY, reading.getAy());
            values.put(SensorsContract.AccelerometerEntry.COLUMN_AZ, reading.getAz());

            //Insert the values into the Table for Accelerometer
            db.insertWithOnConflict(
                    SensorsContract.AccelerometerEntry.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE);

            db.close();
            return reading.getTimestampOfSample();

        }
    }
}
