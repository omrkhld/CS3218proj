package sg.edu.nus.oztrafficcamera;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class BlankActivity extends Activity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity


                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}
