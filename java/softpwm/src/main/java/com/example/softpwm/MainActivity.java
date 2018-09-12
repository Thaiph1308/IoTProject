package com.example.softpwm;

import android.app.Activity;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Pwm;
import com.google.android.things.pio.SpiDevice;
import com.leinardi.android.things.pio.SoftPwm;

import java.io.IOException;

public class MainActivity extends Activity {
    // GPIO Name
    private static final String GPIO_NAME = "BCM 20";

    private Pwm mPwm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mPwm = SoftPwm.openSoftPwm(GPIO_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initializePwm(Pwm pwm) throws IOException {
        pwm.setPwmFrequencyHz(120);
        pwm.setPwmDutyCycle(25);

        // Enable the PWM signal
        pwm.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPwm != null) {
            try {
                mPwm.close();
                mPwm = null;
            } catch (IOException e) {
                //Log.w(Tag,"Message = XYZ")
            }
        }
    }
}
