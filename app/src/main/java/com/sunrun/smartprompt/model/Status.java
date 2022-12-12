package com.sunrun.smartprompt.model;

public class Status {
    
    private static final Status instance = new Status();
    
    private String script;
    private int font_size;
    private int scroll_position;

    public Status() {
        this.script = null;
        this.font_size = 0;
        this.scroll_position = 0;
    }


    public static String getScript() {
        return instance.script;
    }

    public static void setScript(String script) {
        instance.script = new String(script);
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
}
