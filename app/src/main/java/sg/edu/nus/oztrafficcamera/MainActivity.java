package sg.edu.nus.oztrafficcamera;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import sg.edu.nus.accelerometer.AccelerometerActivity;
import sg.edu.nus.all_in_one.AllInOneActivity;
import sg.edu.nus.audio.MicrophoneActivity;
import sg.edu.nus.camera.CameraActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void all_sensors(View view){
        Intent intent = new Intent(MainActivity.this, AllInOneActivity.class);
        startActivity(intent);


    }

    public void go_to_camera(View view){
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(intent);
    }

    public void go_to_microphone(View view){
        Intent myIntent = new Intent(this, MicrophoneActivity.class);
        startActivity(myIntent);

    }

    public void go_to_accelerometer(View view){
        Intent myIntent = new Intent(this, AccelerometerActivity.class);
        startActivity(myIntent);

    }

    public void go_to_doppler(View view){
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        Intent intent = new Intent(this, BlankActivity.class);
        startActivity(intent);
//        try {
////            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
////            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
//        } catch (Exception e) {
////            e.printStackTrace();
//        }
    }

}
