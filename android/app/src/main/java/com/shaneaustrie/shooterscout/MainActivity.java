package com.shaneaustrie.shooterscout;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;

import com.shaneaustrie.shooterscout.utils.Audio;
import com.shaneaustrie.shooterscout.utils.Firebase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Button play, stop, record;

    private String outputFile;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 29;
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = 12;
    private static final int RECORDER_AUDIO_ENCODING = 2;
    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private Audio audioHandler;

    private final String url ="https://shooterscout-backend.herokuapp.com/upload-audio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

    }

    private void init(final Activity a) {
        bufferSize = AudioRecord.getMinBufferSize(8000,
                2,
                4);

        play = (Button) a.findViewById(R.id.play);
        stop = (Button) a.findViewById(R.id.stop);
        record = (Button) a.findViewById(R.id.record);
        stop.setEnabled(false);
        play.setEnabled(false);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    outputFile = Audio.getFilename();
                    startRecording();
                } catch (IllegalStateException ise) {
                    System.out.println("This the error ********" + ise);
                } catch (Exception e) {
                    System.out.println("This the error ********" + e);
                }
                record.setEnabled(false);
                stop.setEnabled(true);
                Toast.makeText(a, "Recording started", Toast.LENGTH_LONG).show();
            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record.setEnabled(true);
                stop.setEnabled(false);
                play.setEnabled(true);
                stopRecording();
                Toast.makeText(getApplicationContext(), "Audio Recorder stopped", Toast.LENGTH_LONG).show();
//
//          }
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(outputFile);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    // make something
                    System.out.println("This the error ********" + e);
                }
                new LongOperation().execute("");
            }
        });
    }

    private void startRecording(){
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);

        int i = recorder.getState();
        if (i == 1)
            recorder.startRecording();

        Audio.setIsRecording(true);
        audioHandler = new Audio(bufferSize, RECORDER_AUDIO_ENCODING,RECORDER_CHANNELS, RECORDER_SAMPLERATE, recorder);


        recordingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                audioHandler.writeAudioDataToFile();
            }
        },"AudioRecorder Thread");

        recordingThread.start();
    }



    private void stopRecording() {
        if (null != recorder) {
            Audio.setIsRecording(false);

            int i = recorder.getState();
            if (i == 1)
                recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        audioHandler.copyWaveFile(audioHandler.getTempFilename(), outputFile);
        audioHandler.deleteTempFile();
    }



    private void sendAudio2() {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost req = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);;
            File file = new File(outputFile);
            builder.addPart("file", new FileBody(file));
            req.setEntity(builder.build());
            client.execute(req, new ResponseHandler<Object>() {
                public Object handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    HttpEntity respEntity = response.getEntity();
                    String responseString = EntityUtils.toString(respEntity);
                    System.out.println("Output in sendaudio2: " + responseString);
                    final String classification = responseString
                            .split(":")[1]
                            .replaceAll("\"", "")
                            .split("\n")[0]
                            .replaceAll(" ", "");
                    System.out.println("Output in sendaudio2 2: " + classification);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDialog(classification);
                        }
                    });
                    return null;
                }
            });
        } catch(Exception e) {
            System.out.println("Error in sendaudio2: " + e);
        }
    }

    private void showDialog(String classification) {
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("REPORT");
        if (classification.equals("gun")) {
            Firebase.sendReport(outputFile, MainActivity.this);
            alertDialog.setMessage("The sound sent was analyzed and classified as " +
                    "a gunshot and was sent to the authorities. Thank you!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OKAY",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        } else {
            alertDialog.setMessage("The sound sent was analyzed and classified as " +
                    "a non-gunshot do you still want to send the request?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "NO",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "YES",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Firebase.sendReport(outputFile, MainActivity.this);
                            dialog.dismiss();
                        }
                    });
        }
        alertDialog.show();
    }

    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            sendAudio2();
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }


    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || this.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    || this.checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                    || this.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
                    || this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION

                    },
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                init(this);
            }
        } else {
            init(this);
        }
    }
}