package sg.edu.nus.audio;

import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AudioTaskUtil
{
    private static DateFormat WHEN_FORMATTER = new SimpleDateFormat("hh:mm:ss");

    public static String getNow()
    {
        Calendar now = Calendar.getInstance();
        String when = WHEN_FORMATTER.format(now.getTime());
        return when;
    }

    public static void appendToStartOfLog(TextView log, String appendThis)
    {
        String currentLog = log.getText().toString();
        currentLog = currentLog + "\n" + appendThis;
        log.setText(currentLog);
    }

}
