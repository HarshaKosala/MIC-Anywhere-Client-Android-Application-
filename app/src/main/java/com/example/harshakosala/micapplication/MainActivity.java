package com.example.harshakosala.micapplication;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    EditText txtIP;
    EditText txtPort;
    Button btnConnect;
    Button btnDisconnect;
    private static String IP;
    private static String portS;
    private static int port;

    private int SAMPLE_RATE = 44100;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private AudioManager audioManager = null;
    private DatagramPacket datagramPacket;
    private DatagramSocket datagramSocket;


    private static String TAG = "AudioClient";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtIP = (EditText) findViewById(R.id.txtIP);
        txtPort = (EditText) findViewById(R.id.txtPort);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);

        btnConnect.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Thread(){
                            @Override
                            public void run() {
                                format();
                                IP = txtIP.getText().toString();
                                portS = txtPort.getText().toString();
                                port = Integer.parseInt(portS);
                                recordAndStream();
                            }
                        }.start();
                    }
                }
        );

        btnDisconnect.setOnClickListener(
                new Button.OnClickListener(){
                    @Override
                    public void onClick(View view) {

                                txtIP.setText("");
                                txtPort.setText("");
                                audioRecord.stop();
                                System.exit(0);

                    }
                }
        );

    }


    private void format() {
        int min = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, min);

        int maxJitter = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);

    }

    private void recordAndStream() {
        try {
            datagramSocket = new DatagramSocket();
            InetAddress inetAddress = InetAddress.getByName(IP);
            byte[] lin = new byte[1024];
            int num = 0;
            audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioRecord.startRecording();
            //audioTrack.play();
            while (true) {
                num = audioRecord.read(lin, 0, 1024);
                audioTrack.write(lin, 0, num);
                datagramPacket = new DatagramPacket(lin,lin.length,inetAddress,port);
                datagramSocket.send(datagramPacket);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }




}
