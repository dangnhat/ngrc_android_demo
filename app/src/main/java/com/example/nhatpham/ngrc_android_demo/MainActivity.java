package com.example.nhatpham.ngrc_android_demo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText editTextDestAddress, editTextPort;
    private Button buttonStartStop;
    private TextView textViewStatus, textViewRx;

    private int port = 52002;
    private String address = "127.0.0.1";
    private int UpdatingPeriod = 1000; //ms
    private boolean startGeneratePacket = false;


    /* Soldiers information */
    class soldierInfo {
        public int sid;
        public int gpsLat, gpsLong;
        public int wjam, gjam;
        public int gstate1, gstate2, gstate3;
    }

    ArrayList<soldierInfo> soldiersList = new ArrayList<soldierInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Find UI elements */
        editTextDestAddress = (EditText) findViewById(R.id.editTextDestAddr);
        editTextPort = (EditText) findViewById(R.id.editTextPort);
        buttonStartStop = (Button) findViewById(R.id.buttonStartStop);
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        textViewStatus.setMovementMethod(new ScrollingMovementMethod());

        /* fill in solders info */
        soldierInfo dummySInfo = new soldierInfo();
        dummySInfo.sid = 1111111113;
        dummySInfo.gpsLat = 59;
        dummySInfo.gpsLong = 80;
        dummySInfo.wjam = 5;
        dummySInfo.gjam = 0;
        dummySInfo.gstate1 = 100;
        dummySInfo.gstate2 = 130;
        dummySInfo.gstate3 = 150;
        soldiersList.add(dummySInfo);

        dummySInfo.sid = 1111111117;
        dummySInfo.gpsLat = 59;
        dummySInfo.gpsLong = 71;
        dummySInfo.wjam = 5;
        dummySInfo.gjam = 0;
        dummySInfo.gstate1 = 100;
        dummySInfo.gstate2 = 130;
        dummySInfo.gstate3 = 150;
        soldiersList.add(dummySInfo);

        dummySInfo.sid = 1301381719;
        dummySInfo.gpsLat = 50;
        dummySInfo.gpsLong = 55;
        dummySInfo.wjam = 5;
        dummySInfo.gjam = 0;
        dummySInfo.gstate1 = 100;
        dummySInfo.gstate2 = 130;
        dummySInfo.gstate3 = 150;
        soldiersList.add(dummySInfo);
    }

    public void buttonStartStopOnClick(View view) throws IOException {
        if (!startGeneratePacket) {
            Log.d("Main", "buttonStartStopOnClick: starting...");
            textViewStatus.append("Starting...\n");
        }


    }


    /********************************* Periodically generate packet and send to UDP server ********/
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            Log.d("Main", "timerRunnable: Generate packet to UDP server");

            generateUdpPacket generateUdpPacketTask = new generateUdpPacket();
            generateUdpPacketTask.execute();
        }
    };

    private class generateUdpPacket extends AsyncTask<String, String, Void> {
        @Override
        protected void onPreExecute() {
            // Progress
            //progressText.setText("Progress: 50%, Updating info.");
        }

        @Override
        protected void onPostExecute(Void result) {

            Log.d("Main", "timerRunnable: post timerRunnable for " + UpdatingPeriod/1000 + "s");
            timerHandler.removeCallbacks(timerRunnable);
            timerHandler.postDelayed(timerRunnable, UpdatingPeriod);

            // Progress
            //progressText.setText("Progress: 100%, Done.");
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // Progress
            // progressText.setText(values[0]);
        }

        @Override
        protected Void doInBackground (String... params) {

            return null;
        }


    }


}
