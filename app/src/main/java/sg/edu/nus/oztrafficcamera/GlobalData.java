package sg.edu.nus.oztrafficcamera;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Omar on 11/4/2016.
 */
public abstract class GlobalData {

    private GlobalData() {
    };

    private static final AtomicBoolean phoneInMotion = new AtomicBoolean(false);

    public static boolean isPhoneInMotion() {
        return phoneInMotion.get();
    }

    public static void setPhoneInMotion(boolean bool) {
        phoneInMotion.set(bool);
    }
}
