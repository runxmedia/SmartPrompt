package com.sunrun.smartprompt.model;

import android.util.Log;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ArrayBlockingQueue;

public class Status extends Observable {
    
    private static final Status instance = new Status();
    
    private String script;
    private final StringBuilder incoming_script; //Container for incoming script chunks
    private int font_size;
    private float scroll_position;
    private int scroll_speed;
    public enum ScriptState {INCOMING, COMPLETE} //Enumerator to store state of incoming scripts
    private ScriptState scriptState;

    public Status() {
        this.script = null;
        this.font_size = 0;
        this.scroll_position = 0;
        this.scroll_speed = 3;
        this.incoming_script = new StringBuilder();
        scriptState = ScriptState.COMPLETE;
    }


    public static String getScript() {
        return instance.script;
    }

    public static void setScript(String script) {
        instance.script = new String(script);
        instance.setChanged();
        instance.notifyObservers();
    }

    public static int getFont_size() {
        return instance.font_size;
    }

    public static void setFont_size(int font_size) {
        instance.font_size = font_size;
    }

    public static float getScroll_position() {
        return instance.scroll_position;
    }

    public static void setScroll_position(float scroll_position) {
        instance.scroll_position = scroll_position;
    }

    public static int getScroll_speed() {
        return instance.scroll_speed;
    }

    public static void setScroll_speed(int scroll_speed) {
        instance.scroll_speed = scroll_speed;
    }

    public static ScriptState getScriptState() {
        return instance.scriptState;
    }

    public static void startNewScript(String newScript){
        instance.scriptState = ScriptState.INCOMING;
        instance.setChanged();
        instance.notifyObservers();
        instance.incoming_script.delete(0,instance.incoming_script.length());
        instance.incoming_script.append(newScript);
    }

    public static void appendToScript(String newscript){
        instance.incoming_script.append(newscript);
    }

    public static void completeScript(){
        instance.script = instance.incoming_script.toString();
        instance.scriptState = ScriptState.COMPLETE;
        instance.setChanged();
        instance.notifyObservers();
    }


    public static void putObserver(Observer o) {
        instance.addObserver(o);
    }

}
