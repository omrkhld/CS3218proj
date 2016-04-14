package sg.edu.nus.all_in_one;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import sg.edu.nus.camera.ThumbnailAdapter;
import sg.edu.nus.data.SensorDBHelper;
import sg.edu.nus.data.SensorsContract;
import sg.edu.nus.oztrafficcamera.R;

public class AllInOneDBLog extends Activity {

    private ListAdapter listAdapter;
    SensorDBHelper helper;
    ListView       listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_in_one_dblog);

        //Find the listView
        listView = (ListView) findViewById(R.id.listview_aio_log);

        //Get DBHelper to read from database
        helper = new SensorDBHelper(this);
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
