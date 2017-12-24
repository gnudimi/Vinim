package com.larmuseau.vinim;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.Random;

import static java.lang.Math.abs;


public class Vinim extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private long lastUpdate;
    private float lastX=0;
    private float lastY=0;
    private float lastZ=0;
    private boolean firstPass=true;
    private boolean firstClosure=true;
    private float maxLumen=0;

    private Context mContext;

    VideoView videoView;
    VideoView videoViewFull;

    Sensor sensorAccelerometer;
    Sensor sensorLightmeter;

    boolean deviceHasBeenLit = false;
    boolean videoShouldStop = false;
    boolean videoFull = false;

    int gradualIncrease=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vinim);

        mContext = getApplicationContext();

        TextView note1 = (TextView)findViewById(R.id.textViewNote1);
        TextView note2 = (TextView)findViewById(R.id.textViewNote2);
        TextView note3 = (TextView)findViewById(R.id.textViewNote3);
        TextView note4 = (TextView)findViewById(R.id.textViewNote4);

        note1.setText(".");
        note2.setText(".");
        note3.setText(".");
        note4.setText(".");

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorLightmeter = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        lastUpdate = System.currentTimeMillis();

        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(100);
        progressBar.setProgress(0);

        TextView frequencyView = (TextView)findViewById(R.id.textViewFrequency);
        frequencyView.setText("pending");

        setScreenBrightness(255);

        // show video as a teaser is not needed anymore
        /*
        videoView = (VideoView) findViewById(R.id.videoViewVinim);
        videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.loop);
        videoView.setOnPreparedListener (new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        */

        // videostatistics are not needed (for now, maybe we need to buffer from the internet again once the final video files are available
        /*
            videoStatistics vStat = new videoStatistics();
            vStat.setBuffer(videoView,progressBar);
        */

        final Tuner tuner = new Tuner(frequencyView,note1,note2,note3,note4,videoView);

        final Button buttonSmiley = findViewById(R.id.setVideoSmiley);
        final Button buttonLoop = findViewById(R.id.setVideoLoop);
        final Button buttonGutter = findViewById(R.id.setVideoGutter);
        final Button buttonMandarinnekes = findViewById(R.id.setVideoMandarinnekes);


        buttonSmiley.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // we do not need to stop the initial video, does not exist
                /*
                videoView.setOnPreparedListener (new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(false);
                    }
                });
                videoView.pause();

                tuner.reset();
                */

                setContentView(R.layout.fullscreen);
                videoFull=true;
                setScreenBrightness(0);
                videoViewFull = (VideoView) findViewById(R.id.fullscreenVideoView);
                videoViewFull.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.smiley_small);
                videoViewFull.setOnPreparedListener (new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);
                    }
                });
            }
        });

        buttonLoop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(R.layout.fullscreen);
                videoFull=true;
                setScreenBrightness(0);
                videoViewFull = (VideoView) findViewById(R.id.fullscreenVideoView);
                videoViewFull.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.loop);
                videoViewFull.setOnPreparedListener (new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);
                    }
                });
            }
        });

        buttonGutter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(R.layout.fullscreen);
                videoFull=true;
                setScreenBrightness(0);
                videoViewFull = (VideoView) findViewById(R.id.fullscreenVideoView);
                videoViewFull.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.gootsteen_small);
                videoViewFull.setOnPreparedListener (new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);
                    }
                });
            }
        });

        buttonMandarinnekes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setContentView(R.layout.fullscreen);
                videoFull=true;
                setScreenBrightness(0);
                videoViewFull = (VideoView) findViewById(R.id.fullscreenVideoView);
                videoViewFull.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.mandarijnen_small);
                videoViewFull.setOnPreparedListener (new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);
                    }
                });
            }
        });

        tuner.start();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                sensorAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);

        sensorManager.registerListener(this,
                sensorLightmeter,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }

        if(event.sensor.getType() == Sensor.TYPE_LIGHT) {
            getLightmeter(event);
        }
    }

    private void getLightmeter(SensorEvent event) {
        long actualTime = event.timestamp;
        if (actualTime - lastUpdate < 200) {
            return;
        }
        lastUpdate = actualTime;

        float[] values = event.values;
        float x = values[0];

        if (x > maxLumen) {
            maxLumen = x;
        }

//      Hide if not in content window
        TextView lightValue = (TextView) findViewById(R.id.textViewLight);
        if(lightValue!=null)
            lightValue.setText("lumen=" + x);

        //calculate volume
        float divider = maxLumen / 15;

/*
        if ((abs(x / divider) < 1) && videoFull && deviceHasBeenLit) {
            System.out.println("light value = "+abs(x/divider));
            if(videoViewFull!=null) {
                if(firstClosure) {
                    videoViewFull.pause();
                    firstClosure=false;
                    }
                else {
                    videoViewFull.stopPlayback();
                    videoShouldStop=true;
                }
                setScreenBrightness(0);
                videoViewFull.setVisibility(View.INVISIBLE);
                System.out.println("playback stopped");
            }
        }
*/

        if(abs(x / divider) <= 8 && abs(x / divider) >= 3) {
           // flikker goes here
        }

        if ((abs(x / divider) > 8) && videoFull) {

            System.out.println("light value = "+abs(x/divider));
            if(videoViewFull!=null) {
                videoViewFull.setVisibility(View.VISIBLE);
                videoViewFull.start();
            }

            Random r = new Random();
            gradualIncrease=gradualIncrease+5;
            int randomBrightness = r.nextInt(5+gradualIncrease) + gradualIncrease;
            setScreenBrightness(randomBrightness);
            deviceHasBeenLit=true;
            System.out.print("brightness " + randomBrightness);
        }

        setVolume(abs(x / divider));
    }

    private void getAccelerometer(SensorEvent event) {
        long actualTime=event.timestamp;
        if (actualTime - lastUpdate < 200) {
            return;
        }
        lastUpdate = actualTime;

        float[] values = event.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];


        /*   -------

        TextView xAxis = (TextView)findViewById(R.id.textViewXaxis);
        TextView yAxis = (TextView)findViewById(R.id.textViewYaxis);
        TextView zAxis = (TextView)findViewById(R.id.textViewZaxis);

        xAxis.setText("x="+x);
        yAxis.setText("y="+y);
        zAxis.setText("z="+z);

        if(firstPass) {
            lastX=x;
            lastY=y;
            lastZ=z;
            firstPass=false;
        } else {
            if(abs(lastX-x)>0.4 || abs(lastY-y)>0.4 || abs(lastZ-z)>0.4) {
                videoView.start();
            }
        }

        */
    }

    private void setVolume(float volume) {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        System.out.println("maxvolume = "+maxVolume);
        System.out.println("currentvolume = "+currentVolume);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, (int)volume, 0);
    }

    public void setScreenBrightness(int brightnessValue){

        // Make sure brightness value between 0 to 255
        if(brightnessValue >= 0 && brightnessValue <= 255){
            Settings.System.putInt(
                    mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue
            );
        }
    }
}
