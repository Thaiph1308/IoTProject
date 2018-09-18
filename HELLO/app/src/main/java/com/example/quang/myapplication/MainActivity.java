package com.example.quang.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity implements View.OnClickListener {
    // UART Device Name
    private static final String UART_DEVICE_NAME = "UART0";

    private UartDevice mDevice;

    private EditText text;
    private Button btn;
    private Button des;

    private static final int CHUNK_SIZE = 512;

    String a = "h";
    String c = "hi";

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (EditText) findViewById(R.id.text);
        btn = (Button) findViewById(R.id.button);
        des = (Button) findViewById(R.id.des);

        // Attempt to access the UART device
        try {
            PeripheralManager manager = PeripheralManager.getInstance();
            mDevice = manager.openUartDevice(UART_DEVICE_NAME);

            configureUartFrame(mDevice);
            //writeUartData(mDevice);
            //readUartBuffer(mDevice);
            //mDevice.registerUartDeviceCallback(mUartCallback);


        } catch (IOException e) {
            Log.w(TAG, "Unable to access UART device", e);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == btn) {
            input(text.getText().toString());
            /*try {
                //writeUartData(mDevice);
                readUartBuffer(mDevice);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //Log.w(TAG, "Value of text: " + );
        }
        if (view == des) {
            try {
                closeUart();
                finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            readUartBuffer(mDevice);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void input(String b) {
        if (b.equals("1")) {
            a = "1";
            Log.w(TAG, "Value of A: " + a);
        } else if (b.equals("2")) {
            a = "2";
            Log.w(TAG, "Value of A: " + a);
        } else if (b.equals("3")) {
            a = "3";
            Log.w(TAG, "Value of A: " + a);
        }else if (b.equals("5")) {
            a = "5";
            Log.w(TAG, "Value of A: " + a);
        }else {
            a = "0";
            Log.w(TAG, "FAILLLLLLLLLLLLLL");
        }

        try {
            writeUartData(mDevice);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /*@Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDevice != null) {
            try {
                mDevice.close();
                mDevice = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close UART device", e);
            }
        }
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            closeUart();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void configureUartFrame(UartDevice uart) throws IOException {
        // Configure the UART port
        uart.setBaudrate(115200);
        uart.setDataSize(8);
        uart.setParity(UartDevice.PARITY_NONE);
        uart.setStopBits(1);
    }

    public void writeUartData(UartDevice uart) throws IOException {
        byte[] buffer;


        Log.w(TAG, "a: " + a);

        buffer = a.getBytes();

        Log.w(TAG, "length: "+ buffer.length);

        count = uart.write(buffer, buffer.length);

        Log.d(TAG, "Wrote " + count + " bytes to peripheral");
    }

    public void readUartBuffer(UartDevice uart) throws IOException {
        // Maximum amount of data to read at one time
        final int maxCount = 1;
        byte[] buffer = new byte[maxCount];

        Log.w(TAG, "HELLOOOOOOOOOOO");

        int count;
        while ((count = uart.read(buffer, buffer.length)) > 0) {
            String aaa = new String(buffer, "UTF-8");
            Log.d(TAG, "Read " + count + " bytes from peripheral " + aaa);
            Log.w(TAG, "Value of aaa: " + aaa);

            if (aaa.equals("1")) {
                Intent intent = new Intent(MainActivity.this, ques1.class);
                Toast.makeText(getApplicationContext(), "true", Toast.LENGTH_LONG).show();
                startActivity(intent);
                closeUart();

            } else if (aaa.equals("2")) {
                Intent intent = new Intent(MainActivity.this, ques2.class);
                Toast.makeText(getApplicationContext(), "true", Toast.LENGTH_LONG).show();
                startActivity(intent);
                closeUart();
            } else if (aaa.equals("5")) {
                Intent intent = new Intent(MainActivity.this, ques5.class);
                Toast.makeText(getApplicationContext(), "true", Toast.LENGTH_LONG).show();
                startActivity(intent);
                closeUart();
            }  else if (aaa.equals("3")) {
                Intent intent = new Intent(MainActivity.this, ques3.class);
                Toast.makeText(getApplicationContext(), "true", Toast.LENGTH_LONG).show();
                startActivity(intent);
                closeUart();
            }
        }
    }

    private void closeUart() throws IOException {
        if (mDevice != null) {
            //mDevice.unregisterUartDeviceCallback(mUartCallback);
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

   /*@Override
    protected void onStart() {
        super.onStart();
        // Begin listening for interrupt events
        try {
            mDevice.registerUartDeviceCallback(mUartCallback);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Interrupt events no longer necessary
        mDevice.unregisterUartDeviceCallback(mUartCallback);
    }*/

    /*private UartDeviceCallback mUartCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            // Read available data from the UART device
            try {
                readUartBuffer(uart);
            } catch (IOException e) {
                Log.w(TAG, "Unable to access UART device", e);
            }

            // Continue listening for more interrupts
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };*/
}
