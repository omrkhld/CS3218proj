package sg.edu.nus.oztrafficcamera;

import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
        setHasOptionsMenu(true);

        Button b = (Button) rootView.findViewById(R.id.button_start_microphone);
        b.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RecordAudioTask task = new RecordAudioTask();
                        task.execute("hello");
                    }
                }
        );
        return rootView;
    }



    class RecordAudioTask extends AsyncTask<String, Void, AudioRecord> {

        private final String LOG_TAG = RecordAudioTask.class.getSimpleName();

        @Override
        protected AudioRecord doInBackground(String... params) {
            return null;
        }

        @Override
        protected void onPostExecute(AudioRecord aVoid) {
            super.onPostExecute(aVoid);
        }


    }
}
