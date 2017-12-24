package com.larmuseau.vinim;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.VideoView;

import com.larmuseau.vinim.tarsos.PitchDetectionResult;
import com.larmuseau.vinim.tarsos.Yin;
import com.larmuseau.vinim.util.AudioUtils;

/*
 * Copyright 2016 chRyNaN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Created by chRyNaN on 1/14/2016. This class binds the logic between the view and the pitch detection process.
 * This way a custom tuner view can be created and as long as it implements the TunerUpdate interface it can be
 * used instead of the default view.
 */
public class Tuner {
    private static final String TAG = Tuner.class.getSimpleName();
    private TunerUpdate view;
    private int sampleRate;
    private int bufferSize;
    private volatile int readSize;
    private volatile int amountRead;
    private volatile float[] buffer;
    private volatile short[] intermediaryBuffer;
    private AudioRecord audioRecord;
    private volatile Yin yin;
    private volatile Note currentNote;
    private volatile PitchDetectionResult result;
    private volatile boolean isRecording;
    private volatile Handler handler;
    private Thread thread;
    private TextView pitchView;
    private TextView note1;
    private TextView note2;
    private TextView note3;
    private TextView note4;
    private int numberloop = 0;
    private int noteDo=0;
    private int noteRe=0;
    private int noteMi=0;
    private int noteFa=0;
    private VideoView vv;

    //provide the tuner view implementing the TunerUpdate to the constructor
    public Tuner(TextView pitchView,TextView note1,TextView note2,TextView note3,TextView note4,VideoView vv) {
        this.pitchView=pitchView;
        this.note1=note1;
        this.note2=note2;
        this.note3=note3;
        this.note4=note4;
        this.vv=vv;
        init();
    }

    public void init(){
        this.sampleRate = AudioUtils.getSampleRate();
        this.bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
        this.readSize = bufferSize / 4;
        this.buffer = new float[readSize];
        this.intermediaryBuffer = new short[readSize];
        this.isRecording = false;
        this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, sampleRate, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        this.yin = new Yin(sampleRate, readSize);
        this.currentNote = new Note(Note.DEFAULT_FREQUENCY);
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void start(){
        if(audioRecord != null) {
            isRecording = true;
            audioRecord.startRecording();
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //Runs off the UI thread
                    findNote();
                }
            }, "Tuner Thread");
            thread.start();
        }
    }

    public void findNote(){
        while(isRecording){
            amountRead = audioRecord.read(intermediaryBuffer, 0, readSize);
            buffer = shortArrayToFloatArray(intermediaryBuffer);
            result = yin.getPitch(buffer);
            currentNote.changeTo(result.getPitch());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(result.getPitch()>0) {
                        float thisPitch= result.getPitch();
                        pitchView.setText("result = " + thisPitch);
                        numberloop++;

                        if(thisPitch>258 & thisPitch<264) {
                            noteDo++;
                            note1.setText(""+noteDo);
                        }

                        if(thisPitch>288 & thisPitch<297) {
                            noteRe++;
                            note2.setText(""+noteRe);
                        }

                        if(thisPitch>326 & thisPitch<332) {
                            noteMi++;
                            note3.setText(""+noteMi);
                        }

                        if(thisPitch>346 & thisPitch<352) {
                            noteFa++;
                            note4.setText(""+noteFa);
                        }

                        if(noteDo > 20 && noteRe > 20 && noteMi > 20 && noteFa > 20) {
                            vv.start();
                        }
                    }
                }
            });
        }
    }

    public void reset() {
        noteDo=0;
        noteRe=0;
        noteMi=0;
        noteFa=0;
        numberloop=0;
        note1.setText(".");
        note2.setText(".");
        note3.setText(".");
        note4.setText(".");
    }

    private float[] shortArrayToFloatArray(short[] array){
        float[] fArray = new float[array.length];
        for(int i = 0; i < array.length; i++){
            fArray[i] = (float) array[i];
        }
        return fArray;
    }

    public void stop(){
        isRecording = false;
        if(audioRecord != null) {
            audioRecord.stop();
        }
    }

    public void release(){
        isRecording = false;
        if(audioRecord != null) {
            audioRecord.release();
        }
    }

    public boolean isInitialized(){
        if(audioRecord != null){
            return true;
        }
        return false;
    }

}
