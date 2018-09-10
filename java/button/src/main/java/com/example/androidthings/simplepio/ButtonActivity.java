/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.simplepio;

import android.app.Activity;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

/**
 * Sample usage of the Gpio API that logs when a button is pressed.
 *
 */
public class ButtonActivity extends Activity {
    private static final String TAG = ButtonActivity.class.getSimpleName();
    private Gpio mLedGpio6;
    private Gpio mButtonGpio;
    private Gpio mLed;
    private boolean ledState = false;
    private int time = 2000;
    private Handler mHandler = new Handler();
    private int i = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting ButtonActivity");

        try {
            String pinName6 = "BCM6"; // BoardDefaults.getGPIOForLED();
            mLedGpio6 = PeripheralManager.getInstance().openGpio(pinName6);
            mLedGpio6.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpio6.setValue(ledState);
            String pinName = BoardDefaults.getGPIOForButton();
            mHandler.post(ledRunable);
            Log.i(TAG,"time: "+time);

            mButtonGpio = PeripheralManager.getInstance().openGpio(pinName);
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButtonGpio.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    Log.i(TAG, "GPIO changed, button pressed");
                    // Return true to continue listening to events
                    if (time <= 500 ) {
                        time = 2000;
                        mHandler.post(ledRunable);
                        Log.i(TAG,"time: "+time);

                    }
                    else {
                        time = time/2;
                        mHandler.post(ledRunable);
                        Log.i(TAG,"time: "+time);
                    }
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    //Log.i(TAG,"RETURN TRUE: "+time);
                    return true;
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    private Runnable ledRunable = new Runnable() {
        @Override
        public void run() {
            if (mLedGpio6 == null ){
                return ;
            }
            ledState = !ledState;


            try {
                mLedGpio6.setValue(ledState);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mHandler.postDelayed(ledRunable, time);
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mButtonGpio != null || mLedGpio6 != null) {
            // Close the Gpio pin
            Log.i(TAG, "Closing Button GPIO pin");
            try {
                mButtonGpio.close();
                mLedGpio6.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            } finally {
                mButtonGpio = null;
                mLedGpio6=null;
            }
        }
    }
}
