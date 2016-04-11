package sg.edu.nus.audio;

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

public class MicrophoneDBLog extends AppCompatActivity {
    private ListAdapter listAdapter;
    SensorDBHelper helper;
    ListView       listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microphone_dblog);

        //Find the listView
        listView = (ListView) findViewById(R.id.listview_audio_log);

        //Get DBHelper to read from database
        helper = new SensorDBHelper(this);
        SQLiteDatabase sqlDB  = helper.getReadableDatabase();
        updateListView(sqlDB);
    }

    private void updateListView(SQLiteDatabase sqlDB) {
        //Query database to get any existing data
        Cursor cursor = sqlDB.query(SensorsContract.MicrophoneEntry.TABLE_NAME,
                new String[]{SensorsContract.MicrophoneEntry._ID,
                        SensorsContract.MicrophoneEntry.COLUMN_TIMESTAMP,
                        SensorsContract.MicrophoneEntry.COLUMN_AUDIO_SAMPLE},
                null, null, null, null, null);

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
        listView.setAdapter(listAdapter);
    }

    public void clear_database(View view){
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(SensorsContract.MicrophoneEntry.TABLE_NAME, null, null);

        updateListView(db);
    }
}
