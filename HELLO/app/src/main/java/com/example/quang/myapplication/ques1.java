package com.example.quang.myapplication;

import android.app.Activity;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;


public class ques1 extends Activity implements View.OnClickListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static int a = 1000;

    private Handler mHandler = new Handler();
    private Gpio mLedGpio;
    private boolean mLedState = true;

    private static final String BTN = "BCM21";
    private Gpio btn;

    private static final String LED1 = "BCM5";
    private static final String LED2 = "BCM13";
    private static final String LED3 = "BCM6";

    private Button button;
    private Gpio led1;
    private Boolean s3 = false;

    private Gpio led2;
    private boolean s2 = false;

    int count =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ques1);
        Log.i(TAG, "Starting BlinkActivity");

        button = (Button)findViewById(R.id.button);

        PeripheralManager manager = PeripheralManager.getInstance();

        try {
            //CONFIG LED1
            mLedGpio = manager.openGpio(LED1);
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            //CONFIG LED2
            led2 = manager.openGpio(LED2);
            led2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            //CONFIG LED3
            led1 = manager.openGpio(LED3);
            led1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);


            Log.i(TAG, "Start blinking LED GPIO pin");
            // Post a Runnable that continuously switch the state of the GPIO, blinking the
            // corresponding LED
            mHandler.post(mBlinkRunnable);
            //mHandler.post(mBlinkRunnable1);
            //mHandler.post(mBlinkRunnable2);

        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == button) {
            Intent intent = new Intent(ques1.this,MainActivity.class);
            Toast.makeText(getApplicationContext(),"true",Toast.LENGTH_LONG).show();
            mHandler.removeCallbacks(mBlinkRunnable);
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove pending blink Runnable from the handler.
        mHandler.removeCallbacks(mBlinkRunnable);
        //mHandler.removeCallbacks(mBlinkRunnable1);
        // Close the Gpio pin.
        Log.i(TAG, "Closing LED GPIO pin");
        try {
            mLedGpio.close();
            led2.close();
            led1.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mLedGpio = null;
            led2 = null;
            led1 = null;
        }
    }


    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the GPIO is already closed
            if (mLedGpio == null ) {
                return;
            }
            try {/*
                // Toggle the GPIO state
                mLedState = !mLedState;
                mLedGpio.setValue(mLedState);
                Log.d(TAG, "State led1 set to " + mLedState);*/

                Log.d(TAG, "Count " + count);
                switch(count){
                    case 0:
                        mLedState = true;
                        s2 = false;
                        s3 = false;

                        count++;
                        break;

                    case 1:
                        mLedState = false;
                        s2 = true;
                        s3 = false;

                        count++;
                        break;

                    case 2:
                        mLedState = false;
                        s2 = false;
                        s3 = true;

                        count = 0;
                        break;
                }



                mLedGpio.setValue(mLedState);
                Log.d(TAG, "State led1 set to " + mLedState);

                led1.setValue(s2);
                Log.d(TAG, "State led2 set to " + s2);

                led2.setValue(s3);
                Log.d(TAG, "State led3 set to " + s3);

                Log.d(TAG, "================================");

                // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds
                mHandler.postDelayed(mBlinkRunnable, a);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

    /*private Runnable mBlinkRunnable1 = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the GPIO is already closed
            if (led2 == null ) {
                return;
            }
            try {
                // Toggle the GPIO state
                s2 = !s2;
                led2.setValue(s2);
                Log.d(TAG, "State led2 set to " + s2);

                // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds
                mHandler.postDelayed(mBlinkRunnable1, a);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

    private Runnable mBlinkRunnable2 = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the GPIO is already closed
            if (led1 == null ) {
                return;
            }
            try {
                // Toggle the GPIO state
                //s3 = !s3;
                //led1.setValue(s3);
                //Log.d(TAG, "State led3 set to " + s3);
                s3 = !s3;
                led1.setValue(s3);
                Log.d(TAG, "State led3 set to " + s3);

                // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds
                mHandler.postDelayed(mBlinkRunnable2, a);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };*/
}
