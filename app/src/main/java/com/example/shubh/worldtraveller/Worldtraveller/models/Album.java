package com.example.shubh.worldtraveller.Worldtraveller.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Album implements Parcelable{

    private String album_title;
    private String album_category;
    private String album_id;
    private ArrayList<Photo> album_photos;

    public Album(){ }

    public Album(String album_title, String album_category, String album_id, ArrayList<Photo> album_photos) {
        this.album_title = album_title;
        this.album_category = album_category;
        this.album_id = album_id;
        this.album_photos = album_photos;
    }

    protected Album(Parcel in) {
        album_title = in.readString();
        album_category = in.readString();
        album_id = in.readString();
        album_photos = in.createTypedArrayList(Photo.CREATOR);
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    public String getAlbum_title() {
        return album_title;
    }

    public void setAlbum_title(String album_title) {
        this.album_title = album_title;
    }

    public String getAlbum_category() {
        return album_category;
    }

    public void setAlbum_category(String album_category) {
        this.album_category = album_category;
    }

    public String getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
    }

    public ArrayList<Photo> getAlbum_photos() {
        return album_photos;
    }

    public void setAlbum_photos(ArrayList<Photo> album_photos) {
        this.album_photos = album_photos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(album_title);
        dest.writeString(album_category);
        dest.writeString(album_id);
        dest.writeTypedList(album_photos);
    }
}
