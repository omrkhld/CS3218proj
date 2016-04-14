package sg.edu.nus.all_in_one;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import sg.edu.nus.camera.ThumbnailAdapter;
import sg.edu.nus.data.SensorDBHelperCombinedCam;
import sg.edu.nus.data.SensorsContract;
import sg.edu.nus.oztrafficcamera.R;

public class AllInOneDBLog extends Activity {

    private ListAdapter listAdapter;
    SensorDBHelperCombinedCam helper;
    ListView                  listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_in_one_dblog);
        final Intent intent = new Intent(this, DetailsActivity.class);

        //Find the listView
        listView = (ListView) findViewById(R.id.listview_aio_log);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SQLiteCursor cursor = (SQLiteCursor) listView.getItemAtPosition(position);
                String       uri    = cursor.getString(cursor.getColumnIndex(SensorsContract.CameraEntry.COLUMN_IMAGE_URI));
                String timestampClickedFrame = cursor.getString(cursor.getColumnIndex(SensorsContract.CameraEntry.COLUMN_TIMESTAMP));
                String timestampNextFrame;
                try {
                    SQLiteCursor cursor1 = (SQLiteCursor) listView.getItemAtPosition(position + 1);
                    timestampNextFrame = cursor1.getString(cursor1.getColumnIndex(SensorsContract.CameraEntry.COLUMN_TIMESTAMP));
                } catch (CursorIndexOutOfBoundsException e){
                    // if last item was clicked
                    timestampNextFrame = "INFINITY";
                }
                Log.v("Here", String.valueOf(timestampClickedFrame));
                Log.v("Here", String.valueOf(timestampNextFrame));
                intent.putExtra("cur_frame_timestamp", timestampClickedFrame);
                intent.putExtra("next_frame_timestamp", timestampNextFrame);
                intent.putExtra("image_uri", uri);

                startActivity(intent);
            }
        });

        //Get DBHelper to read from database
        helper = new SensorDBHelperCombinedCam(this);
        SQLiteDatabase sqlDB = helper.getReadableDatabase();
        updateListView(sqlDB);
    }

    private void updateListView(SQLiteDatabase sqlDB) {
        //Query database to get any existing data
        Cursor cursor = sqlDB.query(SensorsContract.CameraEntry.TABLE_NAME,
                new String[]{SensorsContract.CameraEntry._ID,
                        SensorsContract.CameraEntry.COLUMN_TIMESTAMP,
                        SensorsContract.CameraEntry.COLUMN_IMAGE_URI},
                null, null, null, null, null);

        cursor.moveToFirst();

        listAdapter = new ThumbnailAdapter(
                this,
                cursor
        );


        //Create a new TaskAdapter and bind it to ListView
        listView.setAdapter(listAdapter);
    }

    public void clear_database(View view){
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(SensorsContract.CameraEntry.TABLE_NAME, null, null);

        updateListView(db);
    }

}
