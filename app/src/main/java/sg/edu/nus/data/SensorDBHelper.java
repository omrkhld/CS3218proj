package sg.edu.nus.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by delvinlow on 2/4/16.
 */
public class SensorDBHelper extends SQLiteOpenHelper{

    static final int DATABASE_VERSION  = 4;
    static final String DATABASE_NAME = "sensors.db";

    public SensorDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ACCELEROMETER_TABLE =
                "CREATE TABLE " + SensorsContract.AccelerometerEntry.TABLE_NAME + " (" +
                        SensorsContract.AccelerometerEntry._ID + " INTEGER PRIMARY KEY," +
                        SensorsContract.AccelerometerEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL," +
                        SensorsContract.AccelerometerEntry.COLUMN_AX + " REAL NOT NULL," +
                        SensorsContract.AccelerometerEntry.COLUMN_AY + " REAL NOT NULL," +
                        SensorsContract.AccelerometerEntry.COLUMN_AZ + " REAL NOT NULL" +
                        " );";


        final String SQL_CREATE_MICROPHONE_TABLE =
                "CREATE TABLE " + SensorsContract.MicrophoneEntry.TABLE_NAME + " (" +
                        SensorsContract.MicrophoneEntry._ID + " INTEGER PRIMARY KEY," +
                        SensorsContract.MicrophoneEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL," +
                        SensorsContract.MicrophoneEntry.COLUMN_AUDIO_SAMPLE + " BLOB NOT NULL" +
                        " );";
        db.execSQL(SQL_CREATE_ACCELEROMETER_TABLE);
        db.execSQL(SQL_CREATE_MICROPHONE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SensorsContract.AccelerometerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SensorsContract.MicrophoneEntry.TABLE_NAME);
        onCreate(db);

    }

}
