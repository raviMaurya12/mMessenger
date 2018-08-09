package com.example.lenovo.mmessenger;

public class chats {

    private Boolean seen;
    private long timestamp;

    public chats(Boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public chats() {
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
}
