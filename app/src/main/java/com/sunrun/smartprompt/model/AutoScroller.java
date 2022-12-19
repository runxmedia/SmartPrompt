package com.sunrun.smartprompt.model;

import android.os.Handler;
import android.util.Log;
import android.widget.ScrollView;

public class AutoScroller {
    private ScrollView scrollView;

    final private Handler handler = new Handler();
    final private int delay = 5; //milliseconds

    public AutoScroller(ScrollView scrollView) {
        this.scrollView = scrollView;
    }

    //Functions for Auto Scroller in Controller
    private final Runnable controlRunnable = new Runnable() {
        @Override
        public void run() {
            scrollView.scrollBy(0,Status.getScroll_speed());
            if (Status.getScroll_speed() != 0) {
                handler.postDelayed(this, delay);
            }
        }
    };
    public void controlStart(){
        handler.postDelayed(controlRunnable, delay);
    }
    public void controlStop(){
        handler.removeCallbacks(controlRunnable);
    }

    //Functions for Auto Scroller in Teleprompter
    private final Runnable teleprompterRunnable = new Runnable() {
        @Override
        public void run() {
            scrollView.setScrollY(Status.getScroll_position());
            if (Status.getScroll_speed() != 0) {
                handler.postDelayed(this, delay);
            }
        }
    };
    public void teleprompterStart(){
        handler.postDelayed(teleprompterRunnable, delay);
    }
    public void teleprompterStop(){
        handler.removeCallbacks(teleprompterRunnable);
    }
}
