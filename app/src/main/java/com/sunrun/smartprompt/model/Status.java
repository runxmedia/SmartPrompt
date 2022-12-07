package com.sunrun.smartprompt.model;

public class Status {
    
    private static final Status instance = new Status();
    
    private String script;
    private int font_size;
    private int scroll_position;

    public Status() {
        instance.script = null;
        instance.font_size = 0;
        instance.scroll_position = 0;
    }

    public String getScript() {
        return instance.script;
    }

    public void setScript(String script) {
        instance.script = script;
    }

    public int getFont_size() {
        return instance.font_size;
    }

    public void setFont_size(int font_size) {
        instance.font_size = font_size;
    }

    public int getScroll_position() {
        return instance.scroll_position;
    }

    public void setScroll_position(int scroll_position) {
        instance.scroll_position = scroll_position;
    }
}
