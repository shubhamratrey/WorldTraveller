package com.example.shubh.worldtraveller.Worldtraveller.models;

/**
 * Created by User on 8/21/2017.
 */

public class Star {

    private String user_id;
    private String stars;

    public Star(){}

    public Star(String user_id, String stars) {
        this.user_id = user_id;
        this.stars = stars;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getStars() {
        return stars;
    }

    public void setStars(String stars) {
        this.stars = stars;
    }

    @Override
    public String toString() {
        return "Star_utils{" +
                "user_id='" + user_id + '\'' +
                '}';
    }
}
