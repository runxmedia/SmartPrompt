package com.sunrun.smartprompt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.widget.EditText;


import com.sunrun.smartprompt.com.NearbyCom;
import com.sunrun.smartprompt.model.Status;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class ControlActivity extends AppCompatActivity {

    EditText script_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        script_container = findViewById(R.id.txt_script_entry);
        Intent intent = getIntent();

        //Only extract Text if activity was started from "Send"
        boolean script_import;
        String action = intent.getAction();
        script_import = action != null && action.contains("action.SEND");

        if(script_import) {

            String scriptText;
            scriptText = intent.getStringExtra(Intent.EXTRA_TEXT);

            //App sent plain text in the intent
            if (scriptText != null) {
                script_container.setText(scriptText);
            }
            //Google docs returns a "content://" Uri
            else {
                //Try to extract data from uri content
                InputStream inputStream = null;
                try {
                    Uri uri = (Uri) intent.getExtras().get("android.intent.extra.STREAM");
                    ContentResolver resolver = getContentResolver();
                    inputStream = resolver.openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                String text = convertStreamToString(inputStream);
                Status.setScript(text);
                script_container.setText(text);
            }
        }

        NearbyCom nearbyCom = new NearbyCom();
        nearbyCom.startAdvertising(this);
    }

    private String convertStreamToString(InputStream is) {
        Scanner scanner = new Scanner(is).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }


}