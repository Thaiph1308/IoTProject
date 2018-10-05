package com.example.ledblinky;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ledblinky.MVVM.VM.NPNHomeViewModel;
import com.example.ledblinky.MVVM.View.NPNHomeView;
import com.example.ledblinky.Network.ApiResponseListener;
import com.example.ledblinky.Network.VolleyRemoteApiClient;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;
import java.security.Key;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;
import android.widget.Toast;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class MainActivity extends Activity implements View.OnClickListener, NPNHomeView, OnInitListener {

    private double dblSlope = 16.3;
    private double dblIntercept = 0;

    private static final String TAG = "NPNIoTs";
    private int DATA_CHECKING = 0;
    private TextToSpeech niceTTS;


    //GPIO Configuration Parameters
    private static final String LED_PIN_NAME = "BCM26"; // GPIO port wired to the LED
    private Gpio mLedGpio;

    //SPI Configuration Parameters
    private static final String SPI_DEVICE_NAME = "SPI0.1";
    private SpiDevice mSPIDevice;
    private static final String CS_PIN_NAME = "BCM12"; // GPIO port wired to the LED
    private Gpio mCS;


    // UART Configuration Parameters
    private static final int BAUD_RATE = 9600;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
//    private UartDevice mUartDevice;

    byte[] test_data = new byte[]{0, (byte) 0x8b, 0, 0};


    public enum ADC_STATE {
        ADC0, ADC1, ADC2, ADC3, WAIT_ADC0, WAIT_ADC1, WAIT_ADC2, WAIT_ADC3
    }

    ADC_STATE adc_state;

    private static final int CHUNK_SIZE = 512;

    NPNHomeViewModel mHomeViewModel; //Request server object
    Timer mBlinkyTimer;             //Timer
    Timer mDeletePress;


    private TextView txtClock;
    private TextView txtIPAddress;
    private EditText txtConsole;

    private ImageView imgLogo;
    private ImageView imgWifi;


    long lastDown, lastDuration;
    boolean isButtonDeletePress = false;

    int testCounter = 0;
    String name = "KSD";


    private Rc522 mRc522;
    RfidTask mRfidTask;
    private TextView mTagDetectedView;
    private TextView mTagUidView;
    private TextView mTagResultsView;
    //private Button button;
    private Gpio mLedGpio6;
    private Gpio mLedGpio13;
    private Gpio mLedGpio19;

    private SpiDevice spiDevice;
    private Gpio gpioReset;

    private static final String SPI_PORT = "SPI0.0";
    private static final String PIN_RESET = "BCM25";
    private boolean mLedState6 = false;
    private boolean mLedState13 = false;
    private boolean mLedState19 = false;
    String resultsText = "";
    private Handler mHandler = new Handler();

    String UIDString = "";


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //do they have the data
        if (requestCode == DATA_CHECKING) {
            //yep - go ahead and instantiate
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
                niceTTS = new TextToSpeech(this, this);
                //no data, prompt to install it
            else {
                Intent promptInstall = new Intent();
                promptInstall.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(promptInstall);
            }
        }
    }

    private class RfidTask extends AsyncTask<Object, Object, Boolean> {
        private static final String TAG = "RfidTask";
        private Rc522 rc522;

        RfidTask(Rc522 rc522) {
            this.rc522 = rc522;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Object... params) {
            rc522.stopCrypto();
            while (true) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
                //Check if a RFID tag has been found
                if (!rc522.request()) {
                    continue;
                }
                //Check for collision errors
                if (!rc522.antiCollisionDetect()) {
                    continue;
                }
                byte[] uuid = rc522.getUid();
                return rc522.selectTag(uuid);
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {

                return;
            }
            // Try to avoid doing any non RC522 operations until you're done communicating with it.
            byte address = Rc522.getBlockAddress(5, 6);
            // Mifare's card default key A and key B, the key may have been changed previously
            byte[] key = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            // Each sector holds 16 bytes
            // Data that will be written to sector 2, block 1
            byte[] newData = {0x47, 0x49, 0x41, 0x20, 0x50, 0x48, 0x55, 0x43, 0x20, 0x31, 0x34, 0x35, 0x30, 0x31, 0x31, 0x36};

            //0x0F,0x0E,0x0D,0x0C,0x0B,0x0A,0x09,0x08,0x07,0x06,0x05,0x04,0x03,0x02,0x01,0x00
            //0x56,0x49,0x4e,0x31,0x32,0x33,0x46,0x4f,0x52,0x44,0x54,0x52,0x55,0x43,0x4b,0x00


            //phuc: 0x47,0x49,0x41,0x20,0x50,0x48,0x55,0x43,0x20,0x31,0x34,0x35,0x30,0x31,0x31,0x36
            // In this case, Rc522.AUTH_A or Rc522.AUTH_B can be used
            try {
                //We need to authenticate the card, each sector can have a different key
                boolean result = rc522.authenticateCard(Rc522.AUTH_A, address, key);
                if (!result) {

                    return;
                }
//                result = rc522.writeBlock(address, newData);
//                if(!result){
//                    mTagResultsView.setText(R.string.write_error);
//                    Log.i(TAG,"phuctesssssst");
//                    return;
//                }
//                resultsText += "Sector written successfully";

                byte[] buffer = new byte[16];
                //Since we're still using the same block, we don't need to authenticate again
                result = rc522.readBlock(address, buffer);
//                Log.i(TAG,"buffer: "+buffer);
                if (!result) {

                    Log.i(TAG, "fail!!!!!!!");
                    return;
                }
                resultsText += "\nSector read successfully: " + Rc522.dataToHexString(buffer);
//                Log.i(TAG,"buffer: "+Rc522.dataToHexString(buffer));


                String s = Rc522.dataToHexString(buffer);//"0x56 0x49 0x4e 0x31 0x32 0x33 0x46 0x4f 0x52 0x44 0x54 0x52 0x55 0x43 0x4b 0x00 0x38";
                Log.i(TAG, "s: " + s);

                if (s.compareToIgnoreCase("47 49 41 20 50 48 55 43 20 31 34 35 30 31 31 36 ") == 0) {
                    try {
                        mLedGpio13.setValue(true);
                        mLedGpio19.setValue(false);
                        mLedGpio6.setValue(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        mLedGpio13.setValue(false);
                        mLedGpio6.setValue(false);
                        for (int i = 0; i < 5; i++) {
                            mLedState19 = !mLedState19;
                            mLedGpio19.setValue(mLedState19);
                            try {
                                Thread.sleep(400);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
//                            Log.i(TAG,"i : "+i);
                        }
                        mLedGpio19.setValue(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                StringBuilder sb = new StringBuilder();
                String[] components = s.split(" ");

//Log.i(TAG,"COMPONENTS: "+components[0]);
                for (String component : components) {
                    int ival = Integer.parseInt(component.replace("0x", ""), 16);
                    sb.append((char) ival);

                }
                String string = sb.toString();

                Log.i(TAG, "Sector read successfully: " + string);
                Log.i(TAG, "UID: " + rc522.getUidString());
                UIDString = rc522.getUidString();

                processReceivedData();


                rc522.stopCrypto();

            } finally {


            }
        }
    }

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            niceTTS.setLanguage(Locale.forLanguageTag("VI"));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        imgWifi = findViewById(R.id.imgWifi);

        txtClock = findViewById(R.id.txtClock);
        txtConsole = findViewById(R.id.txtConsole);
        txtIPAddress = findViewById(R.id.txtIPAddress);


        mHomeViewModel = new NPNHomeViewModel();
        mHomeViewModel.attach(this, this);

        //override the focus cursor to hide the software keyboard
        txtConsole.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        button = (Button)findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mRfidTask = new RfidTask(mRc522);
//                mRfidTask.execute();
//                ((Button)v).setText(R.string.reading);
//            }
//        });
        PeripheralManager pioService = PeripheralManager.getInstance();
        try {
            spiDevice = pioService.openSpiDevice(SPI_PORT);
            gpioReset = pioService.openGpio(PIN_RESET);
            mRc522 = new Rc522(spiDevice, gpioReset);
            mRc522.setDebugging(true);

            String pinName6 = "BCM6";
            String pinName13 = "BCM13";
            String pinName19 = "BCM19";

            mLedGpio6 = PeripheralManager.getInstance().openGpio(pinName6);
            mLedGpio6.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpio13 = PeripheralManager.getInstance().openGpio(pinName13);
            mLedGpio13.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            mLedGpio19 = PeripheralManager.getInstance().openGpio(pinName19);
            mLedGpio19.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

            mLedGpio6.setValue(true);
            mLedGpio13.setValue(mLedState13);
            mLedGpio19.setValue(mLedState19);

        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        mRfidTask = new RfidTask(mRc522);
//        mRfidTask.execute();

        mHandler.post(mIdRunnable);


//        initGPIO();
//        initUart();

//        setupBlinkyTimer();
//        setupButtonClickEvent();
        //txtConsole.requestFocus();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //create an Intent
        Intent checkData = new Intent();
        //set it up to check for tts data
        checkData.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        //start it so that it returns the result
        startActivityForResult(checkData, DATA_CHECKING);
    }

    private void setupButtonClickEvent() {

    }

    private Runnable mIdRunnable = new Runnable() {
        @Override
        public void run() {


            mRfidTask = new RfidTask(mRc522);
            mRfidTask.execute();

            mHandler.postDelayed(mIdRunnable, 3000);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("NPNIoTs", "Key code is: " + keyCode);

        return true;
        //return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSuccessUpdateServer(String message) {
        Log.d(TAG, "Request server is successful " + message);
//
//        writeUartData(message);
//        String speakWords = "Xin vui lòng đến ô số " + message;
//        niceTTS.speak(speakWords, TextToSpeech.QUEUE_FLUSH, null);


    }

    @Override
    public void onErrorUpdateServer(String message) {
        //txtConsole.setText("Request server is fail");
        Log.d(TAG, "Request server is fail");
    }

    @Override
    public void onClick(View view) {


    }


//    private void setupBlinkyTimer() {
//        mBlinkyTimer = new Timer();
//        TimerTask blinkyTask = new TimerTask() {
//            @Override
//            public void run() {
//
//                MainActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String display = "IP Address: " + Ultis.getWifiIPAddress(MainActivity.this);
//
//                        if (Ultis.checkWifiConnected(MainActivity.this) == true) {
//                            display += " Wifi connected";
//
//                            imgWifi.setImageDrawable(getDrawable(R.drawable.wifi_icon));
//
//                            Log.i(" ", display + " Wifi connected");
//                        } else if (Ultis.checkLanConnected(MainActivity.this) == true) {
//                            display += " Ethernet connected";
//                            imgWifi.setImageDrawable(getDrawable(R.drawable.wifi_icon));
//                            Log.i(" ", display + " Ethernet connected");
//                        } else {
//                            display += " No connection";
//                            imgWifi.setImageDrawable(getDrawable(R.drawable.no_wifi_icon));
//                            Log.i(" ", display + " No connection");
//                        }
//                        txtIPAddress.setText(display);
//
//                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
//                        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
//                        txtClock.setText(sdf.format(new Date()));
//
//                    }
//                });
//
//                MainActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            mLedGpio.setValue(!mLedGpio.getValue());
//
//                        } catch (Throwable t) {
//                            Log.d(TAG, "Error in Blinky LED " + t.getMessage());
//                        }
//                    }
//                });
//            }
//        };
//        mBlinkyTimer.schedule(blinkyTask, 10000, 5000);
//    }

//    public void writeUartData(String message) {
//        try {
//            byte[] buffer = {'W',' ',' '};
//            buffer[2] =  (byte)(Integer.parseInt(message));
//            int count = mUartDevice.write(buffer, buffer.length);
//            Log.d(TAG, "Wrote " + count + " bytes to peripheral  "  + buffer[2]);
//        }catch (IOException e)
//        {
//            Log.d(TAG, "Error on UART");
//        }
//    }


    private void initGPIO() {
        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            mLedGpio = manager.openGpio(LED_PIN_NAME);
            // Step 2. Configure as an output.
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            mCS = manager.openGpio(CS_PIN_NAME);
            mCS.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);


        } catch (IOException e) {
            Log.d(TAG, "Error on PeripheralIO API");
        }
    }

//    private void initUart() {
//        try {
//            openUart("UART0", BAUD_RATE);
//        }catch (IOException e) {
//            Log.d(TAG, "Error on UART API");
//        }
//    }
    /**
     * Callback invoked when UART receives new incoming data.
     */
    String strProcessData = "";
//    private UartDeviceCallback mCallback = new UartDeviceCallback() {
//        @Override
//        public boolean onUartDeviceDataAvailable(UartDevice uart) {
//           //read data from Rx buffer
//            try {
//                byte[] buffer = new byte[CHUNK_SIZE];
//                int noBytes = -1;
//                String strRecv = "";
//                while ((noBytes = mUartDevice.read(buffer, buffer.length)) > 0) {
//                    strRecv += new String(buffer,0,noBytes, "UTF-8");
//
//                }
//                Log.d(TAG,"Received buffer is:  " + strRecv);
//                strProcessData += strRecv;
//                if(strRecv.indexOf("]") >=0){
//                    txtConsole.setText(txtConsole.getText() + strRecv + "\r\n" );
//                    processReceivedData();
//                }
//                else
//                {
//                    txtConsole.setText(txtConsole.getText() + strRecv);
//                }
//
//                txtConsole.setSelection(txtConsole.getText().length());
//
//            } catch (IOException e) {
//                Log.w(TAG, "Unable to transfer data over UART", e);
//            }
//            return true;
//        }
//
//        @Override
//        public void onUartDeviceError(UartDevice uart, int error) {
//            Log.w(TAG, uart + ": Error event " + error);
//        }
//    };

    String getUIDString(String Uid){
        String tmpUID = "";
        tmpUID=Uid.replace("-","");
        return tmpUID;
    }

    private void processReceivedData() {


        String urlSalinity = "";

        urlSalinity = "http://demo1.chipfc.com/SensorValue/update?sensorid=7&sensorvalue=";

        urlSalinity += getUIDString(UIDString);

        mHomeViewModel.updateToServer(urlSalinity);
        UIDString = "";
    }



//    private void openUart(String name, int baudRate) throws IOException {
//        mUartDevice = PeripheralManager.getInstance().openUartDevice(name);
//        // Configure the UART
//        mUartDevice.setBaudrate(baudRate);
//        mUartDevice.setDataSize(DATA_BITS);
//        mUartDevice.setParity(UartDevice.PARITY_NONE);
//        mUartDevice.setStopBits(STOP_BITS);
//
//        mUartDevice.registerUartDeviceCallback(mCallback);
//
//        Log.d(TAG, "UART: OK...");
//    }

//    private void closeUart() throws IOException {
//        if (mUartDevice != null) {
//            mUartDevice.unregisterUartDeviceCallback(mCallback);
//            try {
//                mUartDevice.close();
//            } finally {
//                mUartDevice = null;
//            }
//        }
//    }

    private void closeSPI() throws IOException {
        if(mSPIDevice != null)
        {
           try {
               mSPIDevice.close();
           }finally {
               mSPIDevice = null;
           }

        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        // Attempt to close the UART device
//        try {
//            closeUart();
//            mUartDevice.unregisterUartDeviceCallback(mCallback);
//            closeSPI();
//        } catch (IOException e) {
//            Log.e(TAG, "Error closing UART device:", e);
//        }
//    }

    protected void onDestroy() {
        super.onDestroy();
        try{
            if(spiDevice != null){
                spiDevice.close();
            }
            if(gpioReset != null){
                gpioReset.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

