package sg.edu.nus.data;

import android.provider.BaseColumns;

/**
 * Created by delvinlow on 2/4/16.
 */
public class SensorsContract {

    //Each of xxxEntry corresponds to a table in the database.
    public class AccelerometerEntry implements BaseColumns {
        public static final String TABLE_NAME ="accelerometer";

        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_AX = "ax";
        public static final String COLUMN_AY = "ay";
        public static final String COLUMN_AZ = "az";
    }

    public class MicrophoneEntry implements BaseColumns{
        public static final String TABLE_NAME ="microphone";

        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_AUDIO_SAMPLE = "audiosample";



    }
}
