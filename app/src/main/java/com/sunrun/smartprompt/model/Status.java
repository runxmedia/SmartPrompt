package com.sunrun.smartprompt.model;

import java.util.Observable;
import java.util.Observer;

public class Status extends Observable {
    
    private static final Status instance = new Status();
    
    private String script;
    private final StringBuilder incoming_script; //Container for incoming script chunks
    private float font_size;
    private float scroll_position;
    private int scroll_speed;
    public enum PrompterState {INCOMING, COMPLETE, CONNECTED, DISCONNECTED} //Enumerator to store state of prompter
    private int control_clients; //Holds number of clients in controller
    private PrompterState prompterState;

    public Status() {
        this.script = null;
        this.font_size = 125;
        this.scroll_position = 0;
        this.scroll_speed = 3;
        this.control_clients = 0;
        this.incoming_script = new StringBuilder();
        prompterState = PrompterState.COMPLETE;
    }


    public static String getScript() {
        return instance.script;
    }

    public static void setScript(String script) {
        instance.script = new String(script);
        instance.setChanged();
        instance.notifyObservers();
    }

    public static float getFont_size() {
        return instance.font_size;
    }

    public static void setFont_size(float font_size) {
        instance.font_size = font_size;
        instance.setChanged();
        instance.notifyObservers();
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

    public static PrompterState getPrompterState() {
        return instance.prompterState;
    }

    public static void startNewScript(String newScript){
        instance.prompterState = PrompterState.INCOMING;
        instance.setChanged();
        instance.notifyObservers();
        instance.incoming_script.setLength(0);
        instance.incoming_script.append(newScript);
    }

    public static void appendToScript(String newscript){
        instance.incoming_script.append(newscript);
    }

    public static void completeScript(){
        instance.script = instance.incoming_script.toString();
        instance.prompterState = PrompterState.COMPLETE;
        instance.setChanged();
        instance.notifyObservers();
    }

    public static void notifyConnected(){
        instance.prompterState = PrompterState.CONNECTED;
        instance.setChanged();
        instance.notifyObservers();
    }

    public static void notifyDisconnected(){
        instance.prompterState = PrompterState.DISCONNECTED;
        instance.setChanged();
        instance.notifyObservers();
    }

    public static int getControl_clients() {
        return instance.control_clients;
    }

    public static void setControl_clients(int control_clients) {
        instance.control_clients = control_clients;
        instance.setChanged();
        instance.notifyObservers();
    }

    public static void putObserver(Observer o) {
        instance.addObserver(o);
    }

}
