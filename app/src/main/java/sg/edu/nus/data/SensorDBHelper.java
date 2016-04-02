package sg.edu.nus.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by delvinlow on 2/4/16.
 */
public class SensorDBHelper extends SQLiteOpenHelper{

    static final int DATABASE_VERSION  = 3;
    static final String DATABASE_NAME = "sensors.db";

    public SensorDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_TASKS_TABLE =
                "CREATE TABLE " + SensorsContract.AccelerometerEntry.TABLE_NAME + " (" +
                        SensorsContract.AccelerometerEntry._ID + " INTEGER PRIMARY KEY," +
                        SensorsContract.AccelerometerEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL," +
                        SensorsContract.AccelerometerEntry.COLUMN_AX + " REAL NOT NULL," +
                        SensorsContract.AccelerometerEntry.COLUMN_AY + " REAL NOT NULL," +
                        SensorsContract.AccelerometerEntry.COLUMN_AZ + " REAL NOT NULL" +
                        " );";
        db.execSQL(SQL_CREATE_TASKS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SensorsContract.AccelerometerEntry.TABLE_NAME);
        onCreate(db);

    }

    public void copyDbToExternal(Context context) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//data//" + context.getApplicationContext().getPackageName() + "//databases//"
                        + DATABASE_NAME;
                String backupDBPath = DATABASE_NAME;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
