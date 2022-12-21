package com.sunrun.smartprompt.model;

import android.util.Log;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ArrayBlockingQueue;

public class Status extends Observable {
    
    private static final Status instance = new Status();
    
    private String script;
    private int font_size;
    private int scroll_position;
    private int scroll_speed;
    private ArrayBlockingQueue<Integer> scrollPosQueue;

    public Status() {
        this.script = null;
        this.font_size = 0;
        this.scroll_position = 0;
        this.scroll_speed = 3;
        this.scrollPosQueue = new ArrayBlockingQueue<>(60);
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

    public static int getScroll_position() {
        return instance.scroll_position;
    }

    public static void setScroll_position(int scroll_position) {
        instance.scroll_position = scroll_position;
    }

    public static int getScroll_speed() {
        return instance.scroll_speed;
    }

    public static void setScroll_speed(int scroll_speed) {
        instance.scroll_speed = scroll_speed;
    }

    public static Integer pollQueue(){
        return instance.scrollPosQueue.poll();
    }

    public static boolean addToQueue(int i){
        try {
            return instance.scrollPosQueue.add(i);
        }catch (IllegalStateException e){
            Log.d("Queue", "Queue Full");
            return false;
        }
    }

    public static void putObserver(Observer o) {
        instance.addObserver(o);
    }

}
