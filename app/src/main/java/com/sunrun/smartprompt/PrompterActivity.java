package com.sunrun.smartprompt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.sunrun.smartprompt.com.NearbyCom;

public class PrompterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompter);

        NearbyCom nearbyCom = new NearbyCom();
        nearbyCom.startDiscovery(this);
    }
}