package com.sunrun.smartprompt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    ImageView img_connection_status;
    AutoScroller autoScroller;
    ConstraintLayout layout;
    boolean mirrored;
    NearbyCom nearbyCom;

    //Auto Disconnect handler
    private static final long DISCONNECT_END_DELAY = 120000; // 120 seconds
    private Handler disconnectHandler = new Handler(Looper.getMainLooper());
    private Runnable disconnectRunnable = new Runnable() {
        @Override
        public void run() {
            finish(); // Finish the activity
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_prompter);
        layout = findViewById(R.id.prompter_layout);
        mirrored = false;
        txt_script_container = findViewById(R.id.txt_script_container);
        scrl_script_scroller = findViewById(R.id.scrl_prompter_container);
        img_connection_status = findViewById(R.id.img_connection_status);
        img_arrow = findViewById(R.id.img_pointer_arrow);

        //Setup nearby connections
        nearbyCom = new NearbyCom(this);
        nearbyCom.startDiscovery();

        //Setup Observer
        Status.putObserver(this);

        //Start Autoscroll
        autoScroller = new AutoScroller(scrl_script_scroller);
        autoScroller.teleprompterStart();

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

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void update(Observable observable, Object o) {
        Status.PrompterState state = Status.getPrompterState();
        if(state == Status.PrompterState.INCOMING){
            txt_script_container.setText(R.string.incoming_script);
        }
        else if(state == Status.PrompterState.COMPLETE){
            txt_script_container.setText(Status.getScript());
            txt_script_container.setTextSize(TypedValue.COMPLEX_UNIT_DIP,Status.getFont_size());
            autoScroller.calculateMax();
        }else if(state == Status.PrompterState.CONNECTED){
            img_connection_status.setImageDrawable(getDrawable(R.drawable.connected));
            img_connection_status.setAlpha(1.0f);
            Animation animation = AnimationUtils.loadAnimation(this,R.anim.delayed_fade_out);
            img_connection_status.startAnimation(animation);
            if(disconnectHandler!=null){
                disconnectHandler.removeCallbacks(disconnectRunnable);
            }
        } else if (state == Status.PrompterState.DISCONNECTED){
            img_connection_status.setImageDrawable(getDrawable(R.drawable.reconnecting));
            img_connection_status.setAlpha(1.0f);
            startDisconnectTimer();
        }
    }

    // Start the disconnect timer
    private void startDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectRunnable); // Remove any existing callbacks
        disconnectHandler.postDelayed(disconnectRunnable, DISCONNECT_END_DELAY);
    }



    @Override
    public boolean onKeyDown(int KeyCode, KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
            if (action == KeyEvent.ACTION_DOWN) {
                mirrored = !mirrored;
                if (mirrored){
                    layout.setScaleX(-1.0f);
                } else {
                    layout.setScaleX(1.0f);
                }
                return true;
            }
        return false;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(nearbyCom!=null)
            nearbyCom.closeAll();
    }

}