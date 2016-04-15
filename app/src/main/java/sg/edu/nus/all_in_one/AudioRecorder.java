package sg.edu.nus.all_in_one;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import sg.edu.nus.audio.AudioClipListener;
import sg.edu.nus.data.SensorDBHelper;
import sg.edu.nus.data.SensorsContract;

/**
 * Created by delvinlow on 10/3/16.
 */
public class AudioRecorder {

    private final String TAG = AudioRecorder.class.getSimpleName();

    private AudioRecord       audioRecorder;
    private AudioClipListener audioClipListener;
    String filePath = "/sdcard/Audio8k16bitMono.pcm";

    // A variable to control whether to record
    private boolean continueRecording;

    // Sample rate guaranteed to work on all device
    public static final int RECORDER_SAMPLE_RATE_CD_QUALITY = 44100;

    // Sample rate that may work on some devices
    public static final int RECORDER_SAMPLE_RATE_8000 = 8000;

    private static final int DEFAULT_BUFFER_INCREASE_FACTOR = 3; //prevent overflow

    Context context;

    private AllInOneActivity.RecordAudioTask recordTask;
    private boolean                          heardBeep;

    AudioTrack mAudioTrack;

    public double heardVolume = 0;
    static SensorDBHelper helper;

    public AudioRecorder(AudioClipListener clipListener) {
        this.audioClipListener = clipListener;
        heardBeep = false;
        recordTask = null;
    }

    public AudioRecorder(AudioClipListener clipListener, AllInOneActivity.RecordAudioTask task, Context ctx, SensorDBHelper helper) {
        this(clipListener);
        recordTask = task;
        this.context = ctx;
        this.helper = helper;
    }

    public sg.edu.nus.all_in_one.DetailsOfRecording startRecording() {
        return startRecording(RECORDER_SAMPLE_RATE_8000, AudioFormat.ENCODING_PCM_16BIT);
    }

    /**
     * Records without knowing the exact duration to record
     *
     * @param sampleRate The given sample rate
     * @param encoding   How many bits to use per audio sample
     * @return whether successful
     */
    public sg.edu.nus.all_in_one.DetailsOfRecording startRecording(final int sampleRate, int encoding) {
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

    public sg.edu.nus.all_in_one.DetailsOfRecording doRecording(final int sampleRate, int encoding, int recordBufferSize,
                                                                int readBufferSize,
                                                                final int factorToIncreaseBuffer) {
        sg.edu.nus.all_in_one.DetailsOfRecording details = new sg.edu.nus.all_in_one.DetailsOfRecording();

        // A failure due to the use of an invalid value or recording parameters are not
        // supported by the hardware
        if (recordBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            details.successRecording = false;
            return details;

        } else if (recordBufferSize == AudioRecord.ERROR) { // Unable to query the hardware for its input properties
            details.successRecording = false;
            return details;
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

            //Create file to save to

            FileOutputStream os = null;
            try {
                os = new FileOutputStream(filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            details.startTime = System.currentTimeMillis();
            audioRecorder.startRecording();

            SQLiteDatabase db = helper.getWritableDatabase();

            while (continueRecording) {
                //AudioRecord clashes with Camera if uncomment lines below
//                // Reads audio data from the audio hardware for recording into a byte array. Offset 0, number of requested bytes
//                int bufferResult = audioRecorder.read(readBuffer, 0, readBufferSize);
//
//                if (!continueRecording || recordTask == null && recordTask.isCancelled()) { //Returns true if this task was cancelled before it completed normally, e.g calling cancel()
//                    break;
//                } else if (bufferResult == AudioRecord.ERROR_INVALID_OPERATION) {
//                    // Denotes a failure due to the improper use of a method.
//                    Log.e(TAG, "Error reading: ERROR_INVALID_OPERATION");
//                } else {
                    // No error, start processing
                    heardBeep = audioClipListener.heard(readBuffer, sampleRate); // If return true, compute time lag.

                    if (heardBeep && details.beepDetectedTime == 0L) {
                        details.beepDetectedTime = System.currentTimeMillis();
                        //Measure time lag between now and start
                        long lag = details.beepDetectedTime - details.startTime;
                        details.lag = lag;
                        Log.v("Time lag is ", String.valueOf(lag));
                        // But dont stop, wait for external to cancel.
                        // stopRecording();
                    }
//
                    try {
                        // writes the data to file from buffer stores the voice buffer
                        byte[] bData = short2byte(readBuffer);

                        ContentValues values = new ContentValues();
                        values.clear();
                        values.put(SensorsContract.MicrophoneEntry.COLUMN_TIMESTAMP, System.currentTimeMillis()-details.lag);
                        values.put(SensorsContract.MicrophoneEntry.COLUMN_AUDIO_SAMPLE, bData);

                        //Insert the values into the Table for Tasks
                        db.insertWithOnConflict(
                                SensorsContract.MicrophoneEntry.TABLE_NAME,
                                null,
                                values,
                                SQLiteDatabase.CONFLICT_IGNORE);

                        os.write(bData, 0, readBufferSize * 2); //Because 2 bytes each for 16 bits format
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
////                    db.close();
//                }
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        details.endTime = System.currentTimeMillis();
        details.successRecording = true;
        doneRecording();
        return details;
    }

    public void stopRecording() {
        continueRecording = false;
    }

    public void doneRecording() {
        Log.d(TAG, "Done recording, shutting down recorder");
        if (audioRecorder != null) {
            audioRecorder.stop(); //stop recording
            audioRecorder.release(); //Releases the native AudioRecord resources. The object can no longer be used and the reference should be set to null after a call to release()
            audioRecorder = null;
        }
    }

    //Conversion of short to byte to write to database
    private byte[] short2byte(short[] sData) {
        int    shortArrSize = sData.length;
        byte[] bytes        = new byte[shortArrSize * 2];

        for (int i = 0; i < shortArrSize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

}
