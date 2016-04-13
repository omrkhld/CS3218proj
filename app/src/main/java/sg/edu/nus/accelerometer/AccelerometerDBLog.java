package sg.edu.nus.accelerometer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import sg.edu.nus.data.SensorDBHelper;
import sg.edu.nus.data.SensorsContract;
import sg.edu.nus.oztrafficcamera.R;

public class AccelerometerDBLog extends AppCompatActivity {
    private ListAdapter listAdapter;
    SensorDBHelper helper;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer_dblog);

        //Find the listView
        listView = (ListView) findViewById(R.id.listview_acc_log);

        //Get DBHelper to read from database
        helper = new SensorDBHelper(this);
        SQLiteDatabase sqlDB  = helper.getReadableDatabase();
        updateListView(sqlDB);

    }

    private void updateListView(SQLiteDatabase sqlDB) {
        //Query database to get any existing data
        Cursor cursor = sqlDB.query(SensorsContract.AccelerometerEntry.TABLE_NAME,
                new String[]{SensorsContract.AccelerometerEntry._ID,
                        SensorsContract.AccelerometerEntry.COLUMN_TIMESTAMP,
                        SensorsContract.AccelerometerEntry.COLUMN_AX,
                        SensorsContract.AccelerometerEntry.COLUMN_AY,
                        SensorsContract.AccelerometerEntry.COLUMN_AZ},
                null, null, null, null, null);

        cursor.moveToFirst();

        listAdapter = new SimpleCursorAdapter(
                this,
                R.layout.log_acc_view,
                cursor,
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
        listView.setAdapter(listAdapter);
    }

    public void clear_database(View view){
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(SensorsContract.AccelerometerEntry.TABLE_NAME, null, null);

        updateListView(db);
    }
}
