package com.sunrun.smartprompt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sunrun.smartprompt.com.NearbyCom;
import com.sunrun.smartprompt.model.AutoScroller;
import com.sunrun.smartprompt.model.Status;

import java.util.Observable;
import java.util.Observer;

public class ControlActivity extends AppCompatActivity implements Observer {

    TextView txt_script_container;
    TextView txt_num_clients;
    ScrollView scrl_script_scroller;
    ImageButton btn_edit_script;
    ImageButton btn_pairing;
    ImageView btn_reverse;
    ImageView btn_forward;
    SeekBar seek_speed;
    SeekBar seek_font_size;
    NearbyCom nearbyCom;
    AutoScroller autoScroller;
    int max_scroll;
    boolean pairing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        setupUI();
        updateScript(getIntent().getStringExtra("script"));

        //Start advertising
        nearbyCom = new NearbyCom(this);
        nearbyCom.startAdvertising();
        pairing = true;

        //Setup Observer
        Status.putObserver(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupUI(){
        txt_script_container = findViewById(R.id.txt_script_container);
        txt_num_clients = findViewById(R.id.txt_num_connections);
        scrl_script_scroller = findViewById(R.id.scrl_prompter_container);
        btn_edit_script = findViewById(R.id.btn_edit_script);
        btn_pairing = findViewById(R.id.btn_pairing);
        btn_forward = findViewById(R.id.btn_forward);
        btn_reverse = findViewById(R.id.btn_reverse);
        seek_speed = findViewById(R.id.seek_speed);
        seek_font_size = findViewById(R.id.seek_font_size);
        autoScroller = new AutoScroller(scrl_script_scroller);

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

        //Set Correct Font Size
        txt_script_container.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Status.getFont_size());

        seek_font_size.setProgress((int)Status.getFont_size());
        seek_font_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txt_script_container.setTextSize(TypedValue.COMPLEX_UNIT_DIP,i);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Status.setFont_size(seek_font_size.getProgress());
                max_scroll = (scrl_script_scroller.getChildAt(0).getHeight()) - scrl_script_scroller.getHeight();
                float scrollY = scrl_script_scroller.getScrollY();
                Status.setScroll_position(scrollY/max_scroll);
                nearbyCom.updateFontSize();
            }
        });


        btn_edit_script.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(view.getContext(), ScriptEditActivity.class);
                intent.putExtra("script", Status.getScript());
                startActivity(intent);
            }
        });

        btn_pairing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               togglePairingMode();
            }
        });

        //Setup scroll areas
        btn_forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        autoScroller.controlStart();
                        Log.d("size", "satus: " + Status.getFont_size() + " seekbar: " + seek_font_size.getProgress());
                        break;
                    case MotionEvent.ACTION_UP:
                        autoScroller.controlStop();
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
                        autoScroller.controlStart();
                        break;
                    case MotionEvent.ACTION_UP:
                        Status.setScroll_speed(Status.getScroll_speed()*-1);
                        autoScroller.controlStop();
                        break;
                }
                return true;
            }
        });

        //Setup Scroll Position Updates
        scrl_script_scroller.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() { // Set Scroll position as percentage
                Status.setScroll_position((float)scrl_script_scroller.getScrollY()/(float)max_scroll);
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
        ViewTreeObserver treeObserver = scrl_script_scroller.getViewTreeObserver();
        treeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                autoScaleScript();
            }
        });
        nearbyCom.startAdvertising();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Pause","Pause");
        nearbyCom.closeAll();
    }

    @Override
    public void update(Observable observable, Object o) {
        int clients = Status.getControl_clients();
        switch (clients){
            case 0:
                txt_num_clients.setText(R.string.no_prompters);
                if(!pairing){
                    togglePairingMode();
                }
                break;
            case 1:
                txt_num_clients.setText(R.string.one_prompter);
                if(pairing){
                    togglePairingMode();
                }
                break;
            default:
                String client_string = clients + getResources().getString(R.string.many_prompters);
                txt_num_clients.setText(client_string);
                break;
        }
    }

    private void togglePairingMode(){
        pairing = !pairing;
        if(pairing){
            btn_pairing.setBackgroundColor(Color.GREEN);
            btn_pairing.setAlpha(1.0f);
            nearbyCom.startAdvertising();
        }else{
            btn_pairing.setBackgroundColor(Color.TRANSPARENT);
            btn_pairing.setAlpha(0.6f);
            nearbyCom.stopAdvertising();
        }
    }

    private void updateScript(String script){
        if(script != null){
            txt_script_container.setText(script);
            if(nearbyCom != null) {
                nearbyCom.updateScript();
            }
        }
    }

    private void autoScaleScript(){


        //Get Screen dimensions
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float window_width = displayMetrics.widthPixels;

        //Calculate Script Scale
        float script_width = scrl_script_scroller.getWidth();
        float script_height = scrl_script_scroller.getHeight();
        float button_size = btn_edit_script.getHeight();
        float scale = window_width / script_width;
        scrl_script_scroller.setScaleY(scale);
        scrl_script_scroller.setScaleX(scale);

        //Auto Position Script
        float script_pos_x = ((script_width - (script_width * scale))/2)*-1;
        float script_pos_y = (((script_height - (script_height * scale))/2)-button_size) * -1;
        scrl_script_scroller.setX(script_pos_x);
        scrl_script_scroller.setY(script_pos_y);

        max_scroll = (scrl_script_scroller.getChildAt(0).getHeight()) - scrl_script_scroller.getHeight();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nearbyCom.closeAll();
    }

}