package com.sunrun.smartprompt.model;

import android.os.Handler;
import android.util.Log;
import android.widget.ScrollView;

public class AutoScroller {
    private final ScrollView scrollView;

    final private Handler handler = new Handler();
    final private Handler calculateMaxHandler = new Handler();
    final private int delay = 14; //milliseconds
    final private int buffer_delay = 300;
    private int max_scroll;

    public AutoScroller(ScrollView scrollView) {
        this.scrollView = scrollView;
        calculateMax();
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
            //Convert from scroll percentage to absolute scroll
            scrollView.setScrollY((int) (Status.getScroll_position() * max_scroll));
            handler.postDelayed(this, delay);
        }
    };
    public void teleprompterStart(){
        handler.postDelayed(teleprompterRunnable, delay);
    }
    public void teleprompterStop(){
        handler.removeCallbacks(teleprompterRunnable);
    }

    private final Runnable maxScrollCalcRunnable = new Runnable() {
        @Override
        public void run() {
            max_scroll = scrollView.getChildAt(0).getHeight() - scrollView.getHeight();
            Log.d("Scroll", "Max Scroll reset to: " + max_scroll);

        }
    };

    public void calculateMax(){
        calculateMaxHandler.postDelayed(maxScrollCalcRunnable,150);
    }
}
