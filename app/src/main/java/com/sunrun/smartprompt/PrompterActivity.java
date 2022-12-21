package com.sunrun.smartprompt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sunrun.smartprompt.com.NearbyCom;
import com.sunrun.smartprompt.model.AutoScroller;
import com.sunrun.smartprompt.model.Status;

import java.util.Observable;
import java.util.Observer;

public class PrompterActivity extends AppCompatActivity implements Observer {

    TextView txt_script_container;
    ScrollView scrl_script_scroller;
    ImageView img_arrow;
    AutoScroller autoScroller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_prompter);
        txt_script_container = findViewById(R.id.txt_script_container);
        scrl_script_scroller = findViewById(R.id.scrl_prompter_container);
        img_arrow = findViewById(R.id.img_arrow);

        //Setup nearby connections
        NearbyCom nearbyCom = new NearbyCom(this);
        nearbyCom.startDiscovery();

        //Setup Observer
        Status.putObserver(this);

        //Start Autoscroll
        autoScroller = new AutoScroller(scrl_script_scroller);
        autoScroller.teleprompterStart();

        img_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoScroller.calculateMax();
            }
        });
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
    }

    private void autoScaleScript(){


        //Get Screen dimensions
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float window_width = displayMetrics.widthPixels;

        //Calculate Script Scale
        float adjusted_window_width = window_width - img_arrow.getWidth();
        float script_width = scrl_script_scroller.getWidth();
        float script_height = scrl_script_scroller.getHeight();
        float scale = adjusted_window_width / script_width;
        scrl_script_scroller.setScaleY(scale);
        scrl_script_scroller.setScaleX(scale);

        //Auto Position Script
        float script_pos_x = (((script_width - (script_width * scale))/2)-img_arrow.getWidth())*-1;
        float script_pos_y = ((script_height - (script_height * scale))/2) * -1;
        scrl_script_scroller.setX(script_pos_x);
        scrl_script_scroller.setY(script_pos_y);

    }

    @Override
    public void update(Observable observable, Object o) {
        if(Status.getScriptState() == Status.ScriptState.INCOMING){
            txt_script_container.setText(R.string.incoming_script);
            Log.d("script", "Incoming");
        }
        else {
            Log.d("script", "Complete");
            txt_script_container.setText(Status.getScript());
            autoScroller.calculateMax();
        }
    }
}