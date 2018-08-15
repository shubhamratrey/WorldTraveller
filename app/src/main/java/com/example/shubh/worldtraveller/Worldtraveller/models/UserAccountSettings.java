package com.example.shubh.worldtraveller.Worldtraveller.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Shubh on 04/04/2018.
 */

public class UserAccountSettings implements Parcelable{

    private String profile_photo;
    private String username;
    private String display_name;
    private String description;
    private long clicks;
    private long connections;
    private long stars;
    private String user_id;
    private long online;
    private String device_token;


    public UserAccountSettings(){

    }

    public UserAccountSettings(String profile_photo, String username, String display_name, String description, long clicks, long connections, long stars, String user_id, long online, String device_token) {
        this.profile_photo = profile_photo;
        this.username = username;
        this.display_name = display_name;
        this.description = description;
        this.clicks = clicks;
        this.connections = connections;
        this.stars = stars;
        this.user_id = user_id;
        this.online = online;
        this.device_token = device_token;
    }

    protected UserAccountSettings(Parcel in) {
        profile_photo = in.readString();
        username = in.readString();
        display_name = in.readString();
        description = in.readString();
        clicks = in.readLong();
        connections = in.readLong();
        stars = in.readLong();
        user_id = in.readString();
        online = in.readLong();
        device_token = in.readString();
    }

    public static final Creator<UserAccountSettings> CREATOR = new Creator<UserAccountSettings>() {
        @Override
        public UserAccountSettings createFromParcel(Parcel in) {
            return new UserAccountSettings(in);
        }

        @Override
        public UserAccountSettings[] newArray(int size) {
            return new UserAccountSettings[size];
        }
    };

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getClicks() {
        return clicks;
    }

    public void setClicks(long clicks) {
        this.clicks = clicks;
    }

    public long getConnections() {
        return connections;
    }

    public void setConnections(long connections) {
        this.connections = connections;
    }

    public long getStars() {
        return stars;
    }

    public void setStars(long stars) {
        this.stars = stars;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public long getOnline() {
        return online;
    }

    public void setOnline(long online) {
        this.online = online;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(profile_photo);
        dest.writeString(username);
        dest.writeString(display_name);
        dest.writeString(description);
        dest.writeLong(clicks);
        dest.writeLong(connections);
        dest.writeLong(stars);
        dest.writeString(user_id);
        dest.writeLong(online);
        dest.writeString(device_token);
    }
}

