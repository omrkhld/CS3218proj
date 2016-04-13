package sg.edu.nus.accelerometer;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sg.edu.nus.oztrafficcamera.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class AccelerometerActivityFragment extends Fragment {

    public AccelerometerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accelerometer, container, false);
    }
}
