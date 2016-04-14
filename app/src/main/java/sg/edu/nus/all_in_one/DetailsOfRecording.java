package sg.edu.nus.all_in_one;

/**
 * Created by delvinlow on 14/4/16.
 */

class DetailsOfRecording {
    public Boolean heardBeep = false;
    public boolean successRecording = false;
    public long startTime = 0L;
    public long endTime = 0L;
    public long lag = 0L;

    public long beepDetectedTime = 0L;

    public DetailsOfRecording() {
        this.successRecording = false;
        this.heardBeep = false;
        this.startTime = 0L;
        this.endTime = 0L;
        this.lag = 0L;
    }


}

