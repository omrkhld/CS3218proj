package sg.edu.nus.audio;

/**
 * Created by delvinlow on 11/3/16.
 */

public interface AudioClipListener {
    public double currentVolume = 0;
    /**
     * return true if recording should stop */
    public boolean heard(short[] readBuffer, int sampleRate); }

