package com.example.shubh.worldtraveller.Worldtraveller.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Notification implements Parcelable{

    private String photo_id;
    private String from;
    private String type;
    private String comment_id;
    private String notification_id;
    private long time;

    public Notification() {}

    public Notification(String photo_id, String from, String type, String comment_id, String notification_id, long time) {
        this.photo_id = photo_id;
        this.from = from;
        this.type = type;
        this.comment_id = comment_id;
        this.notification_id = notification_id;
        this.time = time;
    }

    protected Notification(Parcel in) {
        photo_id = in.readString();
        from = in.readString();
        type = in.readString();
        comment_id = in.readString();
        notification_id = in.readString();
        time = in.readLong();
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    public String getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(String photo_id) {
        this.photo_id = photo_id;
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

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public String getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(String notification_id) {
        this.notification_id = notification_id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(photo_id);
        dest.writeString(from);
        dest.writeString(type);
        dest.writeString(comment_id);
        dest.writeString(notification_id);
        dest.writeLong(time);
    }
}
