package com.larmuseau.vinim;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.widget.ProgressBar;
import android.widget.VideoView;

/**
 * Created by dimitril on 25/10/2017.
 */

public class videoStatistics {

    void setBuffer(VideoView vw, ProgressBar pBar) {
        final Handler handler = new Handler();
        final VideoView thisVw = vw;
        final ProgressBar thisPb = pBar;
        final Runnable r = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void run() {
                thisPb.setProgress(thisVw.getBufferPercentage());
                if(thisPb.getProgress()==100) {
                    thisPb.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                }
                handler.postDelayed(this, 1000);
            }
        };

        handler.postDelayed(r, 1000);
    }
}
