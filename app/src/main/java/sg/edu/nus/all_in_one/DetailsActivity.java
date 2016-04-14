package sg.edu.nus.all_in_one;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

import sg.edu.nus.data.SensorDBHelper;
import sg.edu.nus.data.SensorsContract;
import sg.edu.nus.oztrafficcamera.R;

public class DetailsActivity extends Activity {

    TextView       curFrameTimestampTextview;
    TextView       nextFrameTimestampTextview;
    ImageView      imageThumbnail;
    ListView       listViewMic;
    ListView       listViewAcc;
    SensorDBHelper helper;
    private ListAdapter listAdapter;

    String curFrameTimestamp;
    String nextFrameTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        curFrameTimestampTextview = (TextView) findViewById(R.id.cur_frame_timestamp);
        nextFrameTimestampTextview = (TextView) findViewById(R.id.next_frame_timestamp);


        imageThumbnail = (ImageView) findViewById(R.id.image_thumbnail_details);
        //Find the listView
        listViewMic = (ListView) findViewById(R.id.listview_details_mic);
        listViewAcc = (ListView) findViewById(R.id.listview_details_acc);

        Intent callingIntent = getIntent();
        if (callingIntent!= null){
            curFrameTimestamp = callingIntent.getStringExtra("cur_frame_timestamp");
            nextFrameTimestamp = callingIntent.getStringExtra("next_frame_timestamp");
            curFrameTimestampTextview.append(curFrameTimestamp);
            nextFrameTimestampTextview.append(nextFrameTimestamp);
            if (nextFrameTimestamp.equals("INFINITY")){
                nextFrameTimestamp = String.valueOf(Long.MAX_VALUE);
            }
            String uri = callingIntent.getStringExtra("image_uri");


            File imgFile = new  File(uri);
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageThumbnail.setImageBitmap(myBitmap);
            }
        }

        //Get DBHelper to read from database
        helper = new SensorDBHelper(this);
//        SQLiteDatabase sqlDB  = helper.getWritableDatabase();

//        ContentValues values = new ContentValues();
//        values.clear();
//        values.put(SensorsContract.AccelerometerEntry.COLUMN_TIMESTAMP, "1460628843414");
//        values.put(SensorsContract.AccelerometerEntry.COLUMN_AX, "100");
//        values.put(SensorsContract.AccelerometerEntry.COLUMN_AY, "100");
//        values.put(SensorsContract.AccelerometerEntry.COLUMN_AZ, "100");
//
//        //Insert the values into the Table for Accelerometer
//        sqlDB.insertWithOnConflict(
//                SensorsContract.AccelerometerEntry.TABLE_NAME,
//                null,
//                values,
//                SQLiteDatabase.CONFLICT_IGNORE);
//


        SQLiteDatabase sqlDB  = helper.getReadableDatabase();
        updateListView(sqlDB);
    }

    private void updateListView(SQLiteDatabase sqlDB) {
        //Query database to get any existing data
        Cursor cursorAcc = sqlDB.query(SensorsContract.AccelerometerEntry.TABLE_NAME,
                new String[]{SensorsContract.AccelerometerEntry._ID,
                        SensorsContract.AccelerometerEntry.COLUMN_TIMESTAMP,
                        SensorsContract.AccelerometerEntry.COLUMN_AX,
                        SensorsContract.AccelerometerEntry.COLUMN_AY,
                        SensorsContract.AccelerometerEntry.COLUMN_AZ},
                SensorsContract.AccelerometerEntry.COLUMN_TIMESTAMP + " BETWEEN " + curFrameTimestamp + " AND "
                        + nextFrameTimestamp, null, null, null, null);

//        rawQuery("SELECT timestamp, name FROM people WHERE name = ? AND id = ?", new String[] {"David", "2"});

        cursorAcc.moveToFirst();

        listAdapter = new SimpleCursorAdapter(
                this,
                R.layout.log_acc_view,
                cursorAcc,
                new String[]{SensorsContract.AccelerometerEntry.COLUMN_TIMESTAMP,
                        SensorsContract.AccelerometerEntry.COLUMN_AX,
                        SensorsContract.AccelerometerEntry.COLUMN_AY,
                        SensorsContract.AccelerometerEntry.COLUMN_AZ},
                new int[]{R.id.log_acc_textview_timestamp,
                        R.id.log_acc_textview_x,
                        R.id.log_acc_textview_y,
                        R.id.log_acc_textview_z},
                0
        );

        //Create a new TaskAdapter and bind it to ListView
        listViewAcc.setAdapter(listAdapter);

        //Query database to get any existing data
        Cursor cursor = sqlDB.query(SensorsContract.MicrophoneEntry.TABLE_NAME,
                new String[]{SensorsContract.MicrophoneEntry._ID,
                        SensorsContract.MicrophoneEntry.COLUMN_TIMESTAMP,
                        SensorsContract.MicrophoneEntry.COLUMN_AUDIO_SAMPLE},
                SensorsContract.MicrophoneEntry.COLUMN_TIMESTAMP + " BETWEEN " + curFrameTimestamp + " AND "
                        + nextFrameTimestamp, null, null, null, null);

        cursor.moveToFirst();

        listAdapter = new SimpleCursorAdapter(
                this,
                R.layout.log_mic_view,
                cursor,
                new String[]{SensorsContract.MicrophoneEntry.COLUMN_TIMESTAMP,
                        SensorsContract.MicrophoneEntry._ID},
                new int[]{R.id.log_mic_textview_timestamp,
                        R.id.log_mic_id},
                0
        );

        //Create a new TaskAdapter and bind it to ListView
        listViewMic.setAdapter(listAdapter);
    }

}
