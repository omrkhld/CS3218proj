package sg.edu.nus.camera;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import sg.edu.nus.data.SensorDBHelper;
import sg.edu.nus.data.SensorsContract;

/**
 * Created by Omar on 13/4/2016.
 */
public class MediaSaver extends AsyncTask<byte[], String, String> {
    private static final String TAG = "MediaSaver";
    static  String  timeStamp;
    private Context ctx;
    long timestampPictureTaken = 0L;
    Long laginms = 0L;

    public MediaSaver(Context ctx, Long timestamp, Long laginms) {
        this.ctx = ctx;
        this.timestampPictureTaken = timestamp;
        this.laginms = laginms;
    }

    @Override
    protected String doInBackground(byte[]... data) {

        File picFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (picFile == null) {
            Log.e(TAG, "Error creating media file; are storage permissions correct?");
            return null;
        }
        try {
            Uri uri = Uri.fromFile(picFile);
            Log.v("Uri", uri.getPath());

            SensorDBHelper helper = new SensorDBHelper(ctx);
            SQLiteDatabase db = helper.getWritableDatabase();

            db.setLockingEnabled(true);
            //Put in the values within a ContentValues.
            ContentValues values = new ContentValues();
            values.clear();
            values.put(SensorsContract.CameraEntry.COLUMN_TIMESTAMP, timestampPictureTaken - laginms);
            values.put(SensorsContract.CameraEntry.COLUMN_IMAGE_URI, uri.getPath());

            //Insert the values into the Table for Tasks
            db.insertWithOnConflict(
                    SensorsContract.CameraEntry.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE);
            db.setLockingEnabled(false);
            FileOutputStream fos = new FileOutputStream(picFile);
            fos.write(data[0]);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.getMessage());
            e.getStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "I/O error with file: " + e.getMessage());
            e.getStackTrace();
        }

        return null;
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    @SuppressLint("SimpleDateFormat")
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CS3218");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MediaSaver", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "CS3218_" + timeStamp + ".jpg");
            Log.e("MediaSaver", "Saved photo " + mediaFile.getName() + " in " + mediaFile.getPath());
        } else {
            return null;
        }

        return mediaFile;
    }

}