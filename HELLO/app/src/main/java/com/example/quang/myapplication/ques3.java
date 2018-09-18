package com.example.quang.myapplication;

import android.app.Activity;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import static android.content.ContentValues.TAG;


public class ques3 extends Activity implements View.OnClickListener{
    // PWM Name
    private static final String PWM_NAME = "PWM0";

    private static final int a = 100;

    private Button back;

    private Pwm mPwm;

    int i = 0;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ques3);
        back = (Button)findViewById(R.id.back);
        // Attempt to access the PWM port
        try {
            PeripheralManager manager = PeripheralManager.getInstance();
            mPwm = manager.openPwm(PWM_NAME);


            handler.post(mBlinkRunnable);


        } catch (IOException e) {
            Log.w(TAG, "Unable to access PWM", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPwm != null) {
            try {
                mPwm.close();
                mPwm = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close PWM", e);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view == back) {
            Intent intent = new Intent(ques3.this,MainActivity.class);
            Toast.makeText(getApplicationContext(),"true",Toast.LENGTH_LONG).show();
            handler.removeCallbacks(mBlinkRunnable);
            startActivity(intent);
            finish();
        }
    }

    /*public void initializePwm(Pwm pwm) throws IOException {

        pwm.setPwmFrequencyHz(1);
        pwm.setPwmDutyCycle(i);
        Log.w(TAG, "value i(1): " + i);

        for (i = 0; i <= 100; i++) {
            Log.w(TAG, "value i: " + i);
            pwm.setPwmDutyCycle(i);
            if (i == 100) {
                i = 0;
                pwm.setPwmDutyCycle(i);
                Log.w(TAG, "Reset value i: " + i);
            }

            // Enable the PWM signal
            pwm.setEnabled(true);
        }
    }*/

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the GPIO is already closed
            if (mPwm == null) {
                return;
            }
            try {
                // Toggle the GPIO state
                i++;
                mPwm.setPwmFrequencyHz(120);
                mPwm.setPwmDutyCycle(i);
                Log.d(TAG, "Value of i: " + i);

                if(i == 100){
                    i=0;
                    mPwm.setPwmFrequencyHz(120);
                    mPwm.setPwmDutyCycle(i);
                    Log.d(TAG, "Reset i to 0: " + i);
                }

                // Reschedule the same runnable in {#INTERVAL_BETWEEN_BLINKS_MS} milliseconds
                handler.postDelayed(mBlinkRunnable, a);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };
}