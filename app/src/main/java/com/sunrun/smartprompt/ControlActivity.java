package com.sunrun.smartprompt;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sunrun.smartprompt.com.NearbyCom;
import com.sunrun.smartprompt.model.Scroller;
import com.sunrun.smartprompt.model.Status;

import java.io.InputStream;
import java.util.Scanner;

public class ControlActivity extends AppCompatActivity {

    TextView txt_script_container;
    ScrollView scrl_script_scoller;
    ImageButton btn_auto_scroll;
    ImageButton btn_edit_script;
    ImageView btn_reverse;
    ImageView btn_forward;
    SeekBar seek_speed;
    NearbyCom nearbyCom;
    Scroller scroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        setupUI();
        updateScript(getIntent().getStringExtra("script"));

        //Start advertising
        nearbyCom = new NearbyCom(this);
        nearbyCom.startAdvertising();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupUI(){
        txt_script_container = findViewById(R.id.txt_script_container);
        scrl_script_scoller = findViewById(R.id.scrl_prompter_container);
        btn_edit_script = findViewById(R.id.btn_edit_script);
        btn_forward = findViewById(R.id.btn_forward);
        btn_reverse = findViewById(R.id.btn_reverse);
        seek_speed = findViewById(R.id.seek_speed);
        scroller = new Scroller(scrl_script_scoller);

        //Setup speed bar
        seek_speed.setProgress(Status.getScroll_speed());
        seek_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Status.setScroll_speed(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Setup scroll areas
        btn_forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        scroller.start();
                        break;
                    case MotionEvent.ACTION_UP:
                        scroller.stop();
                        break;
                }
                return true;
            }
        });
        btn_reverse.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        Status.setScroll_speed(Status.getScroll_speed()*-1);
                        scroller.start();
                        break;
                    case MotionEvent.ACTION_UP:
                        Status.setScroll_speed(Status.getScroll_speed()*-1);
                        scroller.stop();
                        break;
                }
                return true;
            }
        });

        //Setup Scroll Position Updates
        scrl_script_scoller.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = scrl_script_scoller.getScrollY(); // For ScrollView
                Status.setScroll_position(scrollY);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateScript(intent.getStringExtra("script"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewTreeObserver treeObserver = scrl_script_scoller.getViewTreeObserver();
        treeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                autoScaleScript();
            }
        });
    }

    private void updateScript(String script){
        if(script != null){
            txt_script_container.setText(script);
        }
    }

    private void autoScaleScript(){


        //Get Screen dimensions
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float window_width = displayMetrics.widthPixels;
        float window_height = displayMetrics.heightPixels;

        //Calculate Script Scale
        float script_width = scrl_script_scoller.getWidth();
        float script_height = scrl_script_scoller.getHeight();
        float button_size = btn_auto_scroll.getHeight();
        float scale = window_width / script_width;
        scrl_script_scoller.setScaleY(scale);
        scrl_script_scoller.setScaleX(scale);

        //Auto Position Script
        float script_pos_x = ((script_width - (script_width * scale))/2)*-1;
        float script_pos_y = (((script_height - (script_height * scale))/2)-button_size) * -1;
        scrl_script_scoller.setX(script_pos_x);
        scrl_script_scoller.setY(script_pos_y);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nearbyCom.closeAll();
    }
}