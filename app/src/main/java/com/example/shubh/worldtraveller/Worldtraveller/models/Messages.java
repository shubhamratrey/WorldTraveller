package com.example.shubh.worldtraveller.Worldtraveller.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Messages implements Parcelable {
    private String message;
    private String from;
    private String type;
    private long  time;
    private boolean seen;

    public Messages(){
    }

    public Messages(String message, String from, String type, long time, boolean seen) {
        this.message = message;
        this.from = from;
        this.type = type;
        this.time = time;
        this.seen = seen;
    }

    protected Messages(Parcel in) {
        message = in.readString();
        from = in.readString();
        type = in.readString();
        time = in.readLong();
        seen = in.readByte() != 0;
    }

    public static final Creator<Messages> CREATOR = new Creator<Messages>() {
        @Override
        public Messages createFromParcel(Parcel in) {
            return new Messages(in);
        }

        @Override
        public Messages[] newArray(int size) {
            return new Messages[size];
        }
    };

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeString(from);
        dest.writeString(type);
        dest.writeLong(time);
        dest.writeByte((byte) (seen ? 1 : 0));
    }
}

