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

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import java.io.IOException;
import java.util.List;

/**
 * Sample usage of the PWM API that changes the PWM pulse width at a fixed interval defined in
 * {@link #INTERVAL_BETWEEN_STEPS_MS}.
 *
 */
public class PwmActivity extends Activity {
    private static final String TAG = PwmActivity.class.getSimpleName();

    // Parameters of the servo PWM
    private static final double MIN_ACTIVE_PULSE_DURATION_MS = 1;
    private static final double MAX_ACTIVE_PULSE_DURATION_MS = 2;
    private static final double PULSE_PERIOD_MS = 20;  // Frequency of 50Hz (1000/20)

    // Parameters for the servo movement over time
    private static final double PULSE_CHANGE_PER_STEP_MS = 0.2;
    private static final int INTERVAL_BETWEEN_STEPS_MS = 1000;


    private String PWM_NAME = "PWM1";
    private Handler mHandler = new Handler();
    private Pwm mPwm;
    private double dutySet = 0;
    private long DEFAULT_ANIMATION_DURATION = 200;
    private ValueAnimator valueAnimator;


    private boolean mIsPulseIncreasing = true;
    private double mActivePulseDuration;
    PeripheralManager manager = PeripheralManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<String> portList = manager.getPwmList();
        if(portList.isEmpty()){
            Log.i(TAG,"No PWM prot available. hihihi");
            return;
        }
        else{
            Log.i(TAG,"List of avalable ports: "+portList);
        }

        try {
            mPwm = manager.openPwm(PWM_NAME);

            mPwm.setPwmFrequencyHz(10000);

            mPwm.setEnabled(true);

            Log.i(TAG,"abczyx");

            for (int i = 100; i >=0; i--) {
                try{Thread.sleep(  20); } catch (InterruptedException e) {
                } ;
                mPwm.setPwmDutyCycle(i);
            }

            mHandler.post(mBlinkRunnable);
        } catch (IOException e) {
            Log.d(TAG,"Start change PWM pulse");
        }


    }

    private void changeValue(){
        try {

            for (int i = 100; i >=0; i--) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                };
                mPwm.setPwmDutyCycle(i);
            }
        }
        catch (IOException e) {
                e.printStackTrace();
            }
    }





    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run () {
            // Exit Runnable if the GPIO is already closed
            if (mPwm == null) {
                Log.w(TAG, "Stopping runnable since mPwm is null");
                return;
            }

            changeValue();


            mHandler.postDelayed(mBlinkRunnable, DEFAULT_ANIMATION_DURATION);


            // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds

        }

    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove pending Runnable from the handler.
        mHandler.removeCallbacks(mBlinkRunnable);
        // Close the PWM port.
        Log.i(TAG, "Closing port");
        try {
            mPwm.close();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mPwm = null;
        }
    }
//
//    private Runnable mChangePWMRunnable = new Runnable() {
//        @Override
//        public void run() {
//            // Exit Runnable if the port is already closed
//            if (mPwm == null) {
//                Log.w(TAG, "Stopping runnable since mPwm is null");
//                return;
//            }
//
//            // Change the duration of the active PWM pulse, but keep it between the minimum and
//            // maximum limits.
//            // The direction of the change depends on the mIsPulseIncreasing variable, so the pulse
//            // will bounce from MIN to MAX.
//            if (mIsPulseIncreasing) {
//                mActivePulseDuration += PULSE_CHANGE_PER_STEP_MS;
//            } else {
//                mActivePulseDuration -= PULSE_CHANGE_PER_STEP_MS;
//            }
//
//            // Bounce mActivePulseDuration back from the limits
//            if (mActivePulseDuration > MAX_ACTIVE_PULSE_DURATION_MS) {
//                mActivePulseDuration = MAX_ACTIVE_PULSE_DURATION_MS;
//                mIsPulseIncreasing = !mIsPulseIncreasing;
//            } else if (mActivePulseDuration < MIN_ACTIVE_PULSE_DURATION_MS) {
//                mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
//                mIsPulseIncreasing = !mIsPulseIncreasing;
//            }
//
//            Log.d(TAG, "Changing PWM active pulse duration to " + mActivePulseDuration + " ms");
//
//            try {
//
//                // Duty cycle is the percentage of active (on) pulse over the total duration of the
//                //                // PWM pulse
//                mPwm.setPwmDutyCycle(1000 * mActivePulseDuration / PULSE_PERIOD_MS);
//
//                // Reschedule the same runnable in {@link #INTERVAL_BETWEEN_STEPS_MS} milliseconds
//                mHandler.postDelayed(this, INTERVAL_BETWEEN_STEPS_MS);
//                Log.i(TAG,"phuc");
//            } catch (IOException e) {
//                Log.e(TAG, "Error on PeripheralIO API", e);
//            }
//        }
//    };

}
