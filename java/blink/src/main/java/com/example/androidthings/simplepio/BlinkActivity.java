
package com.example.androidthings.simplepio;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Sample usage of the Gpio API that blinks an LED at a fixed interval defined in
 * {@link #INTERVAL_BETWEEN_BLINKS_MS}.
 *
 * Some boards, like Intel Edison, have onboard LEDs linked to specific GPIO pins.
 * The preferred GPIO pin to use on each board is in the {@link BoardDefaults} class.
 *
 */

public class BlinkActivity extends Activity {
    private static final String TAG = BlinkActivity.class.getSimpleName();
    private static final int INTERVAL_BETWEEN_BLINKS_MS = 0;
    private Handler mHandler = new Handler();
    private Handler mHandler2 = new Handler();
    private Handler mHandler3 = new Handler();
    private Gpio mLedGpio6;
    private Gpio mLedGpio13;
    private Gpio mLedGpio19;
    private boolean mLedState = false;
    private boolean mLedState2 = false;
    private boolean mLedState3 = false;
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
            mHandler2.post(mBlinkRunnable2);
            mHandler3.post(mBlinkRunnable3);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    public void setState(boolean pin1, boolean pin2, boolean pin3) {
        try {
            mLedGpio6.setValue(pin1);
            mLedGpio13.setValue(pin2);
            mLedGpio19.setValue(pin3);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLedGpio6 == null) {// Exit Runnable if the GPIO is already closed
                return;
            }
            // Toggle the GPIO state



                mLedState = !mLedState;


            try {
                mLedGpio6.setValue(mLedState);
            } catch (IOException e) {
                e.printStackTrace();
            }

//            setState(false, false, false);
////            System.out.print("1");
//            Log.i(TAG,"1");
//            setState(false, false, true);
//            Log.i(TAG,"2");
//            setState(false, true, false);
//            Log.i(TAG,"3");
//            setState(false, true, true);
//            Log.i(TAG,"4");
//            setState(true, false, false);
//            Log.i(TAG,"5");
//            setState(true, false, true);
//            Log.i(TAG,"6");
//            setState(true, true, false);
//            Log.i(TAG,"7");
//            setState(true, true, true);
//            Log.i(TAG,"8");



//                setState(false, false, true);

//            setState(true, false, false);


//            setState(true, false, false);

// Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS}

            mHandler.postDelayed(mBlinkRunnable, 1000);
        }
    };

    private Runnable mBlinkRunnable2 = new Runnable() {
        @Override
        public void run() {
            if (mLedGpio13 == null) {// Exit Runnable if the GPIO is already closed
                return;
            }
            // Toggle the GPIO state



            mLedState2 = !mLedState2;
            try {
                mLedGpio13.setValue(mLedState2);

            } catch (IOException e) {
                e.printStackTrace();

            }
//                mLedGpio13.setValue(true);

//            setState(false, false, false);
////            System.out.print("1");
//            Log.i(TAG,"1");
//            setState(false, false, true);
//            Log.i(TAG,"2");
//            setState(false, true, false);
//            Log.i(TAG,"3");
//            setState(false, true, true);
//            Log.i(TAG,"4");
//            setState(true, false, false);
//            Log.i(TAG,"5");
//            setState(true, false, true);
//            Log.i(TAG,"6");
//            setState(true, true, false);
//            Log.i(TAG,"7");
//            setState(true, true, true);
//            Log.i(TAG,"8");



//                setState(false, false, true);

//            setState(true, false, false);


//            setState(true, false, false);

// Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS}

            mHandler2.postDelayed(mBlinkRunnable2, 2000);
        }
    };

    private Runnable mBlinkRunnable3 = new Runnable() {
        @Override
        public void run() {
            if (mLedGpio19 == null) {// Exit Runnable if the GPIO is already closed
                return;
            }
            // Toggle the GPIO state



            mLedState3 = !mLedState3;
            try {
                mLedGpio19.setValue(mLedState3);

            } catch (IOException e) {
                e.printStackTrace();

            }
//                mLedGpio13.setValue(true);

//            setState(false, false, false);
////            System.out.print("1");
//            Log.i(TAG,"1");
//            setState(false, false, true);
//            Log.i(TAG,"2");
//            setState(false, true, false);
//            Log.i(TAG,"3");
//            setState(false, true, true);
//            Log.i(TAG,"4");
//            setState(true, false, false);
//            Log.i(TAG,"5");
//            setState(true, false, true);
//            Log.i(TAG,"6");
//            setState(true, true, false);
//            Log.i(TAG,"7");
//            setState(true, true, true);
//            Log.i(TAG,"8");



//                setState(false, false, true);

//            setState(true, false, false);


//            setState(true, false, false);

// Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS}

            mHandler3.postDelayed(mBlinkRunnable3, 3000);
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
            mLedGpio13.close();
            mLedGpio19.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mLedGpio6 = null;
            mLedGpio13 = null;
            mLedGpio19 = null;
        }
    }
}
