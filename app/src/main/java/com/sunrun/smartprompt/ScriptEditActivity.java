package com.sunrun.smartprompt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.sunrun.smartprompt.com.NearbyCom;
import com.sunrun.smartprompt.model.Status;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

public class ScriptEditActivity extends AppCompatActivity {

    EditText script_container;
    Button btn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_edit);

        script_container = findViewById(R.id.txt_script_entry);
        btn_save = findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Status.setScript(script_container.getText().toString());
                Intent control_intent = new Intent();
                control_intent.setClass(view.getContext(),ControlActivity.class);
                control_intent.putExtra("script",script_container.getText().toString());
                startActivity(control_intent);
                finish();
            }
        });


        Intent intent = getIntent();
        boolean script_import;
        String action = intent.getAction();
        script_import = action != null && action.contains("action.SEND");

        if(script_import) {//Only extract via uri if activity was started from "Send"

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
                script_container.setText(text);
            }
        }else{
            String script = intent.getStringExtra("script");
            if(script != null){
                script_container.setText(script);
            }
        }



    }

    private String convertStreamToString(InputStream is) {
        Scanner scanner = new Scanner(is).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
}