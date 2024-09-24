package com.example.jots;

import com.google.firebase.Timestamp;

import java.sql.Time;

public class Note {
    String title;
    String contents;
    Timestamp timeStamp;

    public Note() {

    }

    public String getTitle() {
        return title;
    }

    public String getContents() {
        return contents;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }
}
