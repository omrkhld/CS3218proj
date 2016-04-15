package sg.edu.nus.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import sg.edu.nus.oztrafficcamera.R;

public class CameraActivity extends Activity {
    private static final String TAG = "CameraActivity";

    private Camera mCamera;
    private SurfaceView mPreview;
    private Button captureButton;
    private TextView lagTimeText;
    private TextView numPhotosText;
    private static SurfaceHolder previewHolder = null;
    private static boolean isStarted = false;
    private static boolean inPreview = false;
    private int pictureDelay = 2000;
    private int numPhotos = 0;
    private long startTime = 0;
    private long endTime = 0;
    private static long mReferenceTime = 0;
    private CaptureThread thread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = mPreview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        lagTimeText = (TextView) findViewById(R.id.lag_time);
        numPhotosText = (TextView) findViewById(R.id.num_photos);

        // Add a listener to the Capture button
        captureButton = (Button) findViewById(R.id.capture_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStarted) {
                    captureButton.setText("Capture");
                    isStarted = false;
                } else {
                    isStarted = true;
                    captureButton.setText("Stop");
                }
            }
        });
    }

    public void view_cam_log(View view){
        Intent intent = new Intent(this, CameraDBLog.class);
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
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void releaseCameraAndPreview(){
       if (mCamera!=null){
           mCamera.release();
           mCamera = null;
       }
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
                    CaptureThread thread = new CaptureThread(getApplicationContext());
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
            Camera.Size size = getBestPreviewSize(width, height, parameters);
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

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private class CaptureThread extends Thread {
        Context ctx;
        Long camera_lag = 0L;

        public CaptureThread(Context ctx){
            this.ctx = ctx;
        }
        @Override
        public void run() {
            Camera.PictureCallback mPicture = new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Long timestamp = System.currentTimeMillis();
                    camera_lag = timestamp - startTime;
                    Log.v("Lag in camera", String.valueOf(camera_lag));
                    lagTimeText.setText(String.valueOf(camera_lag));
                    new MediaSaver(ctx, timestamp, camera_lag).execute(data);
                    mCamera.startPreview();
                }
            };

            startTime = System.currentTimeMillis();
            mCamera.takePicture(null, null, mPicture);
            numPhotos++;
            Log.d(TAG, "Num photos taken = " + numPhotos);
//            Log.d(TAG, "Lag = " + (camera_lag));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    lagTimeText.setText(String.valueOf(camera_lag));
                    numPhotosText.setText(String.valueOf(numPhotos));
                }
            });
        }
    }
}
