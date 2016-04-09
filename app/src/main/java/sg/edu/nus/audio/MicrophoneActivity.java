package sg.edu.nus.audio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import sg.edu.nus.oztrafficcamera.R;

public class MicrophoneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microphone);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    public void view_mic_log(View view) {
        Intent intent = new Intent(this, MicrophoneDBLog.class);
        startActivity(intent);
    }


}
