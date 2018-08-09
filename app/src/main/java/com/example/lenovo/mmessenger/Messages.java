package com.example.lenovo.mmessenger;

public class Messages  {
    private String message;
    private Boolean seen;
    private long timestamp;
    private String type;
    private String from;

    public Messages() {
    }

    public Messages(String message, Boolean seen, long timestamp, String type , String from) {
        this.message = message;
        this.seen = seen;
        this.timestamp = timestamp;
        this.type = type;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
