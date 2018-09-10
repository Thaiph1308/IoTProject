package com.example.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class EightStateMain extends Activity {
    private static final String TAG = EightStateMain.class.getSimpleName();
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 0;
    private Handler mHandler = new Handler();
    private Gpio mLedGpio6;
    private Gpio mLedGpio13;
    private Gpio mLedGpio19;
    private boolean mLedState = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting BlinkActivity");
        try {
            String pinName6 = "BCM6"; // BoardDefaults.getGPIOForLED();
            String pinName13 = "BCM13";
            String pinName19 = "BCM19";
            mLedGpio6 = PeripheralManager.getInstance().openGpio(pinName6);
            mLedGpio6.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            mLedGpio13 = PeripheralManager.getInstance().openGpio(pinName13);
            mLedGpio13.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            mLedGpio19 = PeripheralManager.getInstance().openGpio(pinName19);
            mLedGpio19.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            Log.i(TAG, "Start blinking LED GPIO pin");
            mHandler.post(mBlinkRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    public void setState(boolean pin1, boolean pin2, boolean pin3){
        try {
            mLedGpio6.setValue(pin1);
            mLedGpio13.setValue(pin2);
            mLedGpio19.setValue(pin3);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{Thread.sleep(  1000); } catch (InterruptedException e) {
        } ;
    }


    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLedGpio6 == null || mLedGpio13 == null || mLedGpio19 == null) {// Exit Runnable if the GPIO is already closed
                return;
            }
            // Toggle the GPIO state
//                mLedState = !mLedState;


//                mLedGpio6.setValue(false);
//                mLedGpio13.setValue(true);

            setState(false, false, false);
//            System.out.print("1");
            Log.i(TAG,"1");
            setState(false, false, true);
            Log.i(TAG,"2");
            setState(false, true, false);
            Log.i(TAG,"3");
            setState(false, true, true);
            Log.i(TAG,"4");
            setState(true, false, false);
            Log.i(TAG,"5");
            setState(true, false, true);
            Log.i(TAG,"6");
            setState(true, true, false);
            Log.i(TAG,"7");
            setState(true, true, true);
            Log.i(TAG,"8");



//                setState(false, false, true);

//            setState(true, false, false);


//            setState(true, false, false);

// Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS}

            mHandler.postDelayed(mBlinkRunnable, INTERVAL_BETWEEN_BLINKS_MS);
        }
    };



    protected void onDestroy() {
        super.onDestroy();
// Remove pending blink Runnable from the handler.
        mHandler.removeCallbacks(mBlinkRunnable);
// Close the Gpio pin.
        Log.i(TAG, "Closing LED GPIO pin");
        try {
            mLedGpio6.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mLedGpio6 = null;
        }
    }
}

