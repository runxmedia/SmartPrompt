package com.sunrun.smartprompt.model;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ScrollView;

public class AutoScroller {
    private ScrollView scrollView;

    final private Handler handler = new Handler();
    final private int delay = 7; //milliseconds
    final private int buffer_delay = 300;

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
    Integer scroll_pos;
    private final Runnable teleprompterRunnable = new Runnable() {
        @Override
        public void run() {
//            scroll_pos = Status.pollQueue();
//            if(scroll_pos != null){
//                scrollView.setScrollY(scroll_pos);
//                handler.postDelayed(this, delay);
//            } else{ //We've reached the bottom of the queue and need to allow time to buffer
//                Log.d("Queue", "Bottomed out on Queue");
//                handler.postDelayed(this, buffer_delay);
//            }
            scrollView.setScrollY(Status.getScroll_position());
            handler.postDelayed(this, delay);
        }
    };
    public void teleprompterStart(){
        handler.postDelayed(teleprompterRunnable, delay);
    }
    public void teleprompterStop(){
        handler.removeCallbacks(teleprompterRunnable);
    }
}
