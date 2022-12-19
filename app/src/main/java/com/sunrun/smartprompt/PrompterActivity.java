package com.sunrun.smartprompt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sunrun.smartprompt.com.NearbyCom;

public class PrompterActivity extends AppCompatActivity {

    TextView txt_script_container;
    ScrollView scrl_script_scoller;
    ImageView img_arrow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompter);
        txt_script_container = findViewById(R.id.txt_script_container);
        scrl_script_scoller = findViewById(R.id.scrl_prompter_container);
        img_arrow = findViewById(R.id.img_arrow);


        NearbyCom nearbyCom = new NearbyCom(this);
        nearbyCom.startDiscovery();
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

    private void autoScaleScript(){


        //Get Screen dimensions
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float window_width = displayMetrics.widthPixels;
        float window_height = displayMetrics.heightPixels;

        //Calculate Script Scale
        float adjusted_window_width = window_width - img_arrow.getWidth();
        float script_width = scrl_script_scoller.getWidth();
        float script_height = scrl_script_scoller.getHeight();
        float scale = adjusted_window_width / script_width;
        scrl_script_scoller.setScaleY(scale);
        scrl_script_scoller.setScaleX(scale);

        //Auto Position Script
        float script_pos_x = (((script_width - (script_width * scale))/2)-img_arrow.getWidth())*-1;
        float script_pos_y = ((script_height - (script_height * scale))/2) * -1;
        scrl_script_scoller.setX(script_pos_x);
        scrl_script_scoller.setY(script_pos_y);

    }
}