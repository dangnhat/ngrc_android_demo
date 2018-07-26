package com.example.nhatpham.ngrc_android_demo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
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
    private String ControlCenterURL = "http://192.168.0.2:8080/drmweb";
    private Button buttonStartStop;
    private TextView textViewStatus;
    private RadioGroup devSel;
    private int devUIId, devIndex = -1;
    private WebView ccWebView;

    private int backendPort = 52002;
    private String backendAddress = "127.0.0.1";
    private int UpdatingPeriod = 2000; //ms
    private boolean startGeneratePacket = false;


    /* Soldiers information */
    private int gpsMovRange = 10; // +/- 20 from base value.
    private int gstateRange = 200; // 0 - 200.
    private String udpString = "";
    private String sucAddress = "12.34 ";
    private String udpStringHeader = sucAddress + "usrp";

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
        buttonStartStop = (Button) findViewById(R.id.buttonStartStop);
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        textViewStatus.setMovementMethod(new ScrollingMovementMethod());
        devSel = (RadioGroup) findViewById(R.id.radioGroupDevSel);
        ccWebView = (WebView) findViewById(R.id.ccWebView);

        ccWebView.getSettings().setJavaScriptEnabled(true);
        ccWebView.getSettings().setBuiltInZoomControls(true);
        ccWebView.getSettings().setLoadWithOverviewMode(true);
        ccWebView.getSettings().setUseWideViewPort(true);
        ccWebView.setWebChromeClient(new WebChromeClient());
        ccWebView.setWebViewClient(new WebViewClient());
        ccWebView.loadUrl(ControlCenterURL);

        /* fill in solders info */
        soldierInfo sur1 = new soldierInfo();
        sur1.sid = 1111111111;
        sur1.gpsLat = 59;
        sur1.gpsLong = 80;
        sur1.wjam = 5;
        sur1.gjam = 0;
        sur1.gstate1 = 100;
        sur1.gstate2 = 130;
        sur1.gstate3 = 150;
        soldiersList.add(sur1);

        soldierInfo sur2 = new soldierInfo();
        sur2.sid = 1111111112;
        sur2.gpsLat = 59;
        sur2.gpsLong = 71;
        sur2.wjam = 5;
        sur2.gjam = 0;
        sur2.gstate1 = 100;
        sur2.gstate2 = 130;
        sur2.gstate3 = 150;
        soldiersList.add(sur2);

        soldierInfo sur3 = new soldierInfo();
        sur3.sid = 1111111113;
        sur3.gpsLat = 50;
        sur3.gpsLong = 55;
        sur3.wjam = 5;
        sur3.gjam = 0;
        sur3.gstate1 = 100;
        sur3.gstate2 = 130;
        sur3.gstate3 = 150;
        soldiersList.add(sur3);

        soldierInfo su1 = new soldierInfo();
        su1.sid = 1111111114;
        su1.gpsLat = 70;
        su1.gpsLong = 50;
        su1.wjam = 5;
        su1.gjam = 0;
        su1.gstate1 = 101;
        su1.gstate2 = 131;
        su1.gstate3 = 151;
        soldiersList.add(su1);

        soldierInfo su2 = new soldierInfo();
        su2.sid = 1111111115;
        su2.gpsLat = 55;
        su2.gpsLong = 45;
        su2.wjam = 5;
        su2.gjam = 0;
        su2.gstate1 = 101;
        su2.gstate2 = 131;
        su2.gstate3 = 151;
        soldiersList.add(su2);

        soldierInfo su3 = new soldierInfo();
        su3.sid = 1111111116;
        su3.gpsLat = 60;
        su3.gpsLong = 40;
        su3.wjam = 5;
        su3.gjam = 0;
        su3.gstate1 = 101;
        su3.gstate2 = 131;
        su3.gstate3 = 151;
        soldiersList.add(su3);
    }

    public void buttonStartStopOnClick(View view) throws IOException {
        if (!startGeneratePacket) {
            textViewStatus.append("\nStarting...\n");

            /* Get data from radio box and editTexts */
            devUIId = devSel.getCheckedRadioButtonId();
            switch (devUIId) {
                case R.id.radioButtonSUR1:
                    textViewStatus.append("SUR1 device.\n");
                    devIndex = 0;
                    break;

                case R.id.radioButtonSUR2:
                    textViewStatus.append("SUR2 device.\n");
                    devIndex = 1;
                    break;

                case R.id.radioButtonSUR3:
                    textViewStatus.append("SUR3 device.\n");
                    devIndex = 2;
                    break;

                case R.id.radioButtonSU1:
                    textViewStatus.append("SU1 device.\n");
                    devIndex = 3;
                    break;

                case R.id.radioButtonSU2:
                    textViewStatus.append("SU2 device.\n");
                    devIndex = 4;
                    break;

                case R.id.radioButtonSU3:
                    textViewStatus.append("SU3 device.\n");
                    devIndex = 5;
                    break;

                  default:
                    textViewStatus.append("Unknown device, choose device type and click start again.\n");
                    devIndex = -1;
                    break;
            }

            if (devIndex == -1) {
                return;
            }

            textViewStatus.append("Dev string:\n" + sucAddress + " " + soldiersList.get(devIndex).sid + " "
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
            textViewStatus.append("Generating udp packet...\n");

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
                    + ":" + currentGState3 + "&";
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
