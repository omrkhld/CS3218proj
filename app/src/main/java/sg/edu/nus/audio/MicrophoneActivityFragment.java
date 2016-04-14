package sg.edu.nus.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import sg.edu.nus.oztrafficcamera.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class MicrophoneActivityFragment extends Fragment {

    private RecordAudioTask task;

    Button        startButton;
    AudioRecorder recorder;

    public MicrophoneActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View           rootView = inflater.inflate(R.layout.fragment_microphone, container, false);
        final TextView status   = (TextView) rootView.findViewById(R.id.textview_audio_status);
        final TextView log      = (TextView) rootView.findViewById(R.id.textview_audio_log);
        setHasOptionsMenu(true);

        startButton = (Button) rootView.findViewById(R.id.button_start_microphone);
        final Button stopButton = (Button) rootView.findViewById(R.id.button_stop_microphone);
        stopButton.setEnabled(false);
        startButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

                        task = new RecordAudioTask(getActivity(), status, log, "Microphone");
                        task.execute(new LoudNoiseDetector());
                        startButton.setText(R.string.RECORDING);
                        startButton.setEnabled(false);
                        stopButton.setEnabled(true);

                    }
                }
        );

        stopButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recorder.stopRecording();
                        startButton.setEnabled(true);
                        stopButton.setEnabled(false);
                    }
                }
        );
        return rootView;
    }

    class RecordAudioTask extends AsyncTask<AudioClipListener, Long, DetailsOfRecording> {

        private final String LOG_TAG = RecordAudioTask.class.getSimpleName();

        private TextView textview_status_recording;
        private TextView log;
        private Context  context;
        private String   taskName;

        private long startTime = 0;

        public RecordAudioTask(Context context, TextView status, TextView log, String taskName) {
            this.context = context;
            this.textview_status_recording = status;
            this.log = log;
            this.taskName = taskName;
        }

        @Override
        protected void onPreExecute() {
            // Tell UI that recording is starting
            textview_status_recording.setText(context.getResources().getString(
                    R.string.audio_status_recording));
            super.onPreExecute();
        }

        @Override
        protected DetailsOfRecording doInBackground(AudioClipListener... listeners) {
            DetailsOfRecording recordingDetails = new DetailsOfRecording();

            if (listeners.length == 0) {
                return recordingDetails;
            } else {
                Log.d(LOG_TAG, "Recording");
                AudioClipListener listener = listeners[0];
                recorder = new AudioRecorder(listener, this, context);

                boolean heard = false;
                try {
                    startTime = System.currentTimeMillis();
                    recordingDetails = recorder.startRecording();
                } catch (IllegalStateException ise) {
                    Log.e(LOG_TAG, "Failed to record, recorder not setup properly", ise);
                } catch (RuntimeException se) {
                    Log.e(LOG_TAG, "Failed to record, recorder is already being used", se);
                }

                return recordingDetails;
            }
        }


        @Override
        protected void onPostExecute(DetailsOfRecording result) {
            if (result.successRecording){
                long lag = result.lag;
                long duration = result.endTime - result.startTime;
                AudioTaskUtil.appendToStartOfLog(log, "Lag was " + lag + ", Recorded duration is " + duration);
            } else {
                AudioTaskUtil.appendToStartOfLog(log, "Heard no beeps");
            }
            setDoneMessage();
            startButton.setText(R.string.START_RECORDING);
            super.onPostExecute(result);
        }

        private void setDoneMessage(){
            textview_status_recording.setText(context.getResources().getString(R.string.audio_status_stopped));
        }

    }
}
