package sg.edu.nus.audio;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import sg.edu.nus.audio.AudioClipListener;
import sg.edu.nus.audio.AudioRecorder;
import sg.edu.nus.audio.AudioTaskUtil;
import sg.edu.nus.audio.LoudNoiseDetector;
import sg.edu.nus.oztrafficcamera.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class MicrophoneActivityFragment extends Fragment {

    public MicrophoneActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_microphone, container, false);
        final TextView status = (TextView) rootView.findViewById(R.id.textview_audio_status);
        final TextView log = (TextView) rootView.findViewById(R.id.textview_audio_log);
        setHasOptionsMenu(true);

        Button b = (Button) rootView.findViewById(R.id.button_start_microphone);
        b.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RecordAudioTask task = new RecordAudioTask(getActivity(), status, log, "Microphone");
                        task.execute(new LoudNoiseDetector());
                    }
                }
        );
        return rootView;
    }



    class RecordAudioTask extends AsyncTask<AudioClipListener, Boolean, Boolean> {

        private final String LOG_TAG = RecordAudioTask.class.getSimpleName();

        private TextView textview_status_recording;
        private TextView log;
        private Context  context;
        private String   taskName;

        private static final String TEMP_AUDIO_DIR_NAME = "temp_audio";

        public RecordAudioTask(Context context, TextView status, TextView log, String taskName) {
            this.context = context;
            this.textview_status_recording = status;
            this.log = log;
            this.taskName = taskName;
        }

        @Override
        protected void onPreExecute() {
            // Tell UI that recording is starting
            textview_status_recording.setText(context.getResources().getString(R.string.audio_status_recording));
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(AudioClipListener... listeners) {
            if (listeners.length == 0) {
                return false;
            } else {
                Log.d(LOG_TAG, "Recording");
                AudioClipListener listener = listeners[0];
                AudioRecorder recorder = new AudioRecorder(listener, this);

                boolean heard = false;
                try {
                    heard = recorder.startRecording();
                    publishProgress(heard);
                }
              /*  catch (IOException io){
                    Log.e(LOG_TAG, "Failed to record", io);
                    heard = false;
                } */ catch (IllegalStateException ise) {
                    Log.e(LOG_TAG, "Failed to record, recorder not setup properly", ise);
                    heard = false;
                } catch (RuntimeException se){
                    Log.e(LOG_TAG, "Failed to record, recorder is already being used", se);
                    heard = false;
                }

                return heard;
            }
        }
        @Override
        protected void onProgressUpdate(Boolean... values) {
            //Log.d("MyAsyncTask","onProgressUpdate - " + values[0]);
            if (values[0]) {
                AudioTaskUtil.appendToStartOfLog(log, "Recording");
            } else {
                AudioTaskUtil.appendToStartOfLog(log, "Waiting for sound");
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result){
                AudioTaskUtil.appendToStartOfLog(log, "Heard clap at " +
                        AudioTaskUtil.getNow());
            } else {
                AudioTaskUtil.appendToStartOfLog(log, "Heard no claps");
            }
            setDoneMessage();
            super.onPostExecute(result);
        }

        private void setDoneMessage(){
            textview_status_recording.setText(context.getResources().getString(R.string.audio_status_stopped));
        }

    }
}