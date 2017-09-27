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
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    /* GUI */
    private EditText editTextDestAddress, editTextPort;
    private Button buttonStartStop;
    private TextView textViewStatus, textViewRx;
    private RadioGroup devSel;
    private int devUIId, devIndex = -1;

    private int backendPort = 52002;
    private String backendAddress = "127.0.0.1";
    private int UpdatingPeriod = 2000; //ms
    private boolean startGeneratePacket = false;


    /* Soldiers information */
    private int gpsMovRange = 20; // +/- 20 from base value.
    private int gstateRange = 200; // 0 - 200.
    private String udpString = "";
    private String udpStringHeader = "usrp";

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
        devSel = (RadioGroup) findViewById(R.id.radioGroupDevSel);


        /* fill in solders info */
        soldierInfo su1 = new soldierInfo();
        su1.sid = 1111111113;
        su1.gpsLat = 59;
        su1.gpsLong = 80;
        su1.wjam = 5;
        su1.gjam = 0;
        su1.gstate1 = 100;
        su1.gstate2 = 130;
        su1.gstate3 = 150;
        soldiersList.add(su1);

        soldierInfo su2 = new soldierInfo();
        su2.sid = 1111111117;
        su2.gpsLat = 59;
        su2.gpsLong = 71;
        su2.wjam = 5;
        su2.gjam = 0;
        su2.gstate1 = 100;
        su2.gstate2 = 130;
        su2.gstate3 = 150;
        soldiersList.add(su2);

        soldierInfo su3 = new soldierInfo();
        su3.sid = 1301381719;
        su3.gpsLat = 50;
        su3.gpsLong = 55;
        su3.wjam = 5;
        su3.gjam = 0;
        su3.gstate1 = 100;
        su3.gstate2 = 130;
        su3.gstate3 = 150;
        soldiersList.add(su3);

        soldierInfo su4 = new soldierInfo();
        su4.sid = 1302102032;
        su4.gpsLat = 70;
        su4.gpsLong = 50;
        su4.wjam = 5;
        su4.gjam = 0;
        su4.gstate1 = 101;
        su4.gstate2 = 131;
        su4.gstate3 = 151;
        soldiersList.add(su4);
    }

    public void buttonStartStopOnClick(View view) throws IOException {
        if (!startGeneratePacket) {
            textViewStatus.append("\nStarting...\n");

            /* Get data from radio box and editTexts */
            devUIId = devSel.getCheckedRadioButtonId();
            switch (devUIId) {
                case R.id.radioButtonSU1:
                    textViewStatus.append("SU1 device.\n");
                    devIndex = 0;
                    break;

                case R.id.radioButtonSU2:
                    textViewStatus.append("SU2 device.\n");
                    devIndex = 1;
                    break;

                case R.id.radioButtonSUR:
                    textViewStatus.append("SUR device.\n");
                    devIndex = 2;
                    break;

                case R.id.radioButtonSUC:
                    textViewStatus.append("SUC is currently not supported, choose another device type and click start again.\n");
                    devIndex = -1;
                    break;

                default:
                    textViewStatus.append("Unknown device, choose device type and click start again.\n");
                    devIndex = -1;
                    break;
            }

            if (devIndex == -1) {
                return;
            }

            textViewStatus.append("Dev string:\n" + soldiersList.get(devIndex).sid + " "
                    + soldiersList.get(devIndex).gpsLat + " " + soldiersList.get(devIndex).gpsLong
                    + " " + soldiersList.get(devIndex).wjam + " " + soldiersList.get(devIndex).gjam
                    + " " + soldiersList.get(devIndex).gstate1 + " " + soldiersList.get(devIndex).gstate2
                    + " " + soldiersList.get(devIndex).gstate3 + "\n");

            /* add callback to timer handler */
            Log.d("Main", "timerRunnable: post timerRunnable for " + UpdatingPeriod / 1000 + "s");
            timerHandler.removeCallbacks(timerRunnable);
            timerHandler.postDelayed(timerRunnable, UpdatingPeriod);

            /* Set startGeneratePacket flag*/
            startGeneratePacket = true;
        } else {
            /* Remove callback */
            timerHandler.removeCallbacks(timerRunnable);

            /* Set startGeneratePacket flag*/
            startGeneratePacket = false;

            textViewStatus.append("\nStopped.\n");
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
            textViewStatus.append("Generating udp packet to " + backendAddress + ":" + backendPort + "\n");

            /* Create udpString */
            int currentGpsLat, currentGpsLong, currentGState1, currentGState2, currentGState3;
            currentGpsLat = getRandomNumber(soldiersList.get(devIndex).gpsLat - gpsMovRange,
                    soldiersList.get(devIndex).gpsLat + gpsMovRange);
            currentGpsLong = getRandomNumber(soldiersList.get(devIndex).gpsLong - gpsMovRange,
                    soldiersList.get(devIndex).gpsLong + gpsMovRange);
            currentGState1 = getRandomNumber(0, gstateRange);
            currentGState2 = getRandomNumber(0, gstateRange);
            currentGState3 = getRandomNumber(0, gstateRange);

            udpString = udpStringHeader + "&" + soldiersList.get(devIndex).sid + "&" + currentGpsLat
                    + ":" + currentGpsLong + "&" + soldiersList.get(devIndex).wjam + "&"
                    + soldiersList.get(devIndex).gjam + "&" + currentGState1 + ":" + currentGState2
                    + ":" + currentGState2 + ":" + currentGState3 + "&";
            textViewStatus.append(udpString + "\n");
        }

        @Override
        protected void onPostExecute(Void result) {

            Log.d("Main", "timerRunnable: post timerRunnable for " + UpdatingPeriod / 1000 + "s");
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
        protected Void doInBackground(String... params) {
            DatagramSocket ds = null;
            DatagramPacket dp;

            try {
                ds = new DatagramSocket();
                InetAddress serverAddr = InetAddress.getByName(backendAddress);

                dp = new DatagramPacket(udpString.getBytes(), udpString.length(), serverAddr, backendPort);
                ds.send(dp);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (ds != null) {
                    ds.close();
                }

                return null;
            }
        }
    }

    private int getRandomNumber(int min, int max) {
        return (new Random()).nextInt((max - min) + 1) + min;
    }


}
