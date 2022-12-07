package com.sunrun.smartprompt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
        String scriptText = "SD";
        scriptText = intent.getStringExtra(Intent.EXTRA_TEXT);

        //App sent plain text in the intent
        if (scriptText != null) {
            script_container.setText(scriptText);
        }
        //Google docs returns a "content://" Uri
        else{
            //TODO: Get String from the Uri Google Docs gives us
            Uri uri = (Uri) intent.getExtras().get("android.intent.extra.STREAM");
            ContentResolver resolver = getContentResolver();
            InputStream inputStream = null;
            try {
                inputStream = resolver.openInputStream(uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String text = convertStreamToString(inputStream);
            script_container.setText(text);
        }

    }

    private String convertStreamToString(InputStream is) {
        Scanner scanner = new Scanner(is).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }


    private void sendRequest(String url){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        int poop = 83;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int mm = 28;
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}