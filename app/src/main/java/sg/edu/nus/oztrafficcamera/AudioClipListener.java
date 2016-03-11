package sg.edu.nus.oztrafficcamera;

/**
 * Created by delvinlow on 11/3/16.
 */

public interface AudioClipListener {
    /**
     * return true if recording should stop */
    public boolean heard(short[] readBuffer, int sampleRate); }

