package sg.edu.nus.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by delvinlow on 2/4/16.
 */
public class SensorDBHelperCombinedCam extends SQLiteOpenHelper{

    static final int DATABASE_VERSION  = 1;
    static final String DATABASE_NAME = "camera_combined.db";

    public SensorDBHelperCombinedCam(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        final String SQL_CREATE_CAMERA_TABLE =
                "CREATE TABLE " + SensorsContract.CameraEntry.TABLE_NAME + " (" +
                        SensorsContract.CameraEntry._ID + " INTEGER PRIMARY KEY," +
                        SensorsContract.CameraEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL," +
                        SensorsContract.CameraEntry.COLUMN_IMAGE_URI + " TEXT NOT NULL" +
                        " );";
        db.execSQL(SQL_CREATE_CAMERA_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SensorsContract.CameraEntry.TABLE_NAME);
        onCreate(db);

    }

}
