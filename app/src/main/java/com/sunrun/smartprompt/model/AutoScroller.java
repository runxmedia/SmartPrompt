package com.sunrun.smartprompt.model;

import android.os.Handler;
import android.util.Log;
import android.widget.ScrollView;
import android.util.DisplayMetrics;

public class AutoScroller {
    private final ScrollView scrollView;

    final private Handler handler = new Handler();
    final private Handler calculateMaxHandler = new Handler();
    final private int delay = 30; //milliseconds
    final private int buffer_delay = 300;
    private int max_scroll;
    private final float density;
    private boolean teleAuto = false;
    private int teleSpeed = 0;

    public AutoScroller(ScrollView scrollView) {
        this.scrollView = scrollView;
        DisplayMetrics metrics = scrollView.getResources().getDisplayMetrics();
        this.density = metrics.density;
        calculateMax();
    }

    //Functions for Auto Scroller in Controller
    private final Runnable controlRunnable = new Runnable() {
        @Override
        public void run() {
            scrollView.scrollBy(0, Math.round(Status.getScroll_speed() * density));
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
            if(!teleAuto){
                scrollView.setScrollY((int) (Status.getScroll_position() * max_scroll));
            }
//            scrollView.smoothScrollTo(0,(int) (Status.getScroll_position() * max_scroll));
            handler.postDelayed(this, delay);
        }
    };
    public void teleprompterStart(){
        handler.postDelayed(teleprompterRunnable, delay);
    }
    public void teleprompterStop(){
        handler.removeCallbacks(teleprompterRunnable);
    }

    private final Runnable teleAutoRunnable = new Runnable() {
        @Override
        public void run() {
            scrollView.scrollBy(0, Math.round(teleSpeed * density));
            handler.postDelayed(this, delay);
        }
    };

    public void teleAutoStart(int speed, long startTime){
        // Ignore remote start time to avoid jumps when device clocks differ
        // Start scrolling from current position at provided speed
        teleSpeed = speed;
        teleAuto = true;
        handler.postDelayed(teleAutoRunnable, delay);
    }

    public void teleAutoStop(long stopTime){
        // Stop without using remote timestamp to prevent sudden jumps
        handler.removeCallbacks(teleAutoRunnable);
        teleAuto = false;
        // Update stored scroll position so the periodic
        // teleprompter runnable doesn't jump to an old value
        if(max_scroll > 0){
            Status.setScroll_position((float)scrollView.getScrollY() / (float)max_scroll);
        }
    }

    public void teleSmoothTo(float pos){
        scrollView.smoothScrollTo(0, (int)(pos * max_scroll));
    }

    public void teleJumpTo(float pos){
        scrollView.setScrollY((int)(pos * max_scroll));
    }

    public boolean isTeleAutoMode(){
        return teleAuto;
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
