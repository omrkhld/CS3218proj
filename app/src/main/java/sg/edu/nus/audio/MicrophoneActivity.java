package sg.edu.nus.audio;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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

    public void play_audio(View view) {
        String file = "/sdcard/Audio8k16bitMono.pcm";
        int sampleRate = 8000;
        int bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();

        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

        int i = 0;
        byte[] s = new byte[bufferSize];
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            DataInputStream dis = new DataInputStream(fin);

            at.play();
            while((i = dis.read(s, 0, bufferSize)) > -1){
                at.write(s, 0, i);

            }
            at.stop();
            at.release();
            dis.close();
            fin.close();



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        SensorDBHelper helper = new SensorDBHelper(this);
//        SQLiteDatabase sqlDB  = helper.getReadableDatabase();
//        Cursor cursor = sqlDB.query(SensorsContract.MicrophoneEntry.TABLE_NAME,
//                new String[]{SensorsContract.MicrophoneEntry._ID,
//                        SensorsContract.MicrophoneEntry.COLUMN_TIMESTAMP,
//                        SensorsContract.MicrophoneEntry.COLUMN_AUDIO_SAMPLE},
//                null, null, null, null, null);
//
//
//        mAudioTrack.play();
//
//        try {
//            int count = 0;
//            byte[] twotimesAudio = new byte[bufferSize];
//            while (cursor.moveToNext()) {
//                byte[] audioBuffer =  cursor.getBlob(cursor.getColumnIndex(SensorsContract.MicrophoneEntry.COLUMN_AUDIO_SAMPLE));
//
//                mAudioTrack.write(audioBuffer, 0, audioBuffer.length);
//                count++;
//            }
//        } finally {
//            cursor.close();
//        }

//        FileInputStream os = null;
//        try {
//            os = new FileInputStream(filePath);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            os.read(buffer, 0, bufferSize); //Because 2 bytes in 16 bits format
//            mAudioTrack.write(buffer, 0, buffer.length);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }


}
