package com.sunrun.smartprompt.model;

import android.os.Handler;
import android.widget.ScrollView;

public class Scroller {
    private ScrollView scrollView;

    final private Handler handler = new Handler();
    final private int delay = 5; //milliseconds
    private boolean scrolling;

    public Scroller(ScrollView scrollView) {
        this.scrollView = scrollView;
        scrolling = false;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            scrollView.scrollBy(0,Status.getScroll_speed());
            if (Status.getScroll_speed() != 0) {
                handler.postDelayed(this, delay);
            }
        }
    };
    public void start(){


        scrolling = true;
        handler.postDelayed(runnable, delay);




    }
    public void stop(){

        scrolling = false;

        handler.removeCallbacks(runnable);
    }
}
