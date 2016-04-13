package sg.edu.nus.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by delvinlow on 10/3/16.
 */
public class AudioRecorder {

    private final String TAG = AudioRecorder.class.getSimpleName();

    private AudioRecord       audioRecorder;
    private AudioClipListener audioClipListener;

    // A variable to control whether to record
    private boolean continueRecording;

    // Sample rate guaranteed to work on all device
    public static final int RECORDER_SAMPLE_RATE_CD_QUALITY = 44100;

    // Sample rate that may work on some devices
    public static final int RECORDER_SAMPLE_RATE_8000 = 8000;

    private static final int DEFAULT_BUFFER_INCREASE_FACTOR = 3; //prevent overflow

    private AsyncTask recordTask;
    private boolean   heardCar;

    public double heardVolume = 0;

    public AudioRecorder(AudioClipListener clipListener) {
        this.audioClipListener = clipListener;
        heardCar = false;
        recordTask = null;
    }

    public AudioRecorder(AudioClipListener clipListener, AsyncTask task) {
//        this.clipListener = clipListener;
//        heardCar = false;
        this(clipListener);
        recordTask = task;
    }

    public boolean startRecording() {
        return startRecording(RECORDER_SAMPLE_RATE_8000, AudioFormat.ENCODING_PCM_16BIT);
    }

    /**
     * Records without knowing the exact duration to record
     *
     * @param sampleRate The given sample rate
     * @param encoding   How many bits to use per audio sample
     * @return whether successful
     */
    public boolean startRecording(final int sampleRate, int encoding) {
        int recordBufferSize = computeMinBufferSizeInBytes(sampleRate, encoding);
        int readBufferSize   = recordBufferSize;
        return doRecording(sampleRate, encoding, recordBufferSize, readBufferSize, DEFAULT_BUFFER_INCREASE_FACTOR);
    }

    public int computeMinBufferSizeInBytes(int sampleRate, int encoding) {
        /* Returns the minimum buffer size required for the successful creation of an AudioRecord
        object, in byte units. Note that this size doesn't guarantee a smooth recording under load,
        and higher values should be chosen according to the expected frequency at which the
        AudioRecord instance will be polled for new data */
        int minBufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                encoding);
        return minBufferSize;
    }

    public boolean doRecording(final int sampleRate, int encoding, int recordBufferSize,
                               int readBufferSize,
                               final int factorToIncreaseBuffer) {

        // A failure due to the use of an invalid value or recording parameters are not
        // supported by the hardware
        if (recordBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            return false;
        } else if (recordBufferSize == AudioRecord.ERROR) { // Unable to query the hardware for its input properties
            return false; //stub
        } else {
            int increasedRecordBufferSize = factorToIncreaseBuffer * recordBufferSize;

            // Create AudioRecorder and set up the input parameters
            audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    encoding,
                    increasedRecordBufferSize);

            final short[] readBuffer = new short[readBufferSize]; //TODO: WHY dont need increase read?
            continueRecording = true;
            Log.d(TAG, "Start recording, " + "recordBufferSize : " + increasedRecordBufferSize
                    + " readBufferSize: " + readBufferSize);

            audioRecorder.startRecording(); //TODO: possible IllegalStateException if audio is already recording


            while (continueRecording){
                // Reads audio data from the audio hardware for recording into a byte array. Offset 0, number of requested bytes
                int bufferResult = audioRecorder.read(readBuffer, 0 , readBufferSize);


                if (!continueRecording || recordTask == null && recordTask.isCancelled()){ //Returns true if this task was cancelled before it completed normally, e.g calling cancel()
                    break;
                } else if (bufferResult == AudioRecord.ERROR_INVALID_OPERATION){
                    // Denotes a failure due to the improper use of a method.
                    Log.e(TAG, "Error reading: ERROR_INVALID_OPERATION");
                } else {
                    // No error, start processing
                    heardCar = audioClipListener.heard(readBuffer, sampleRate); // If return true, stop recording.
                    heardVolume = audioClipListener.currentVolume;

                    //Write readBuffer to database
                    
                    if (heardCar) {
                        stopRecording();

                        //Measure time lag between now and start

                        // But dont stop, wait for external to cancel.
                    }
                }
            }
        }
        doneRecording();
        return true;
    }

    public void stopRecording(){
        continueRecording = false;
    }

    public void doneRecording(){
        Log.d(TAG, "Done recording, shutting down recorder");
        if (audioRecorder != null){
            audioRecorder.stop(); //stop recording
            audioRecorder.release(); //Releases the native AudioRecord resources. The object can no longer be used and the reference should be set to null after a call to release()
            audioRecorder = null;
        }
    }

}
