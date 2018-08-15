package com.example.shubh.worldtraveller.Worldtraveller.models;

import java.util.List;

/**
 * Created by User on 8/22/2017.
 */

public class Comment {

    private String comment;
    private String user_id;
    private List<Star> stars;
    private String date_created;

    public Comment() {

    }

    public Comment(String comment, String user_id, List<Star> stars, String date_created) {
        this.comment = comment;
        this.user_id = user_id;
        this.stars = stars;
        this.date_created = date_created;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<Star> getStars() {
        return stars;
    }

    public void setStars(List<Star> stars) {
        this.stars = stars;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "comment='" + comment + '\'' +
                ", user_id='" + user_id + '\'' +
                ", stars=" + stars +
                ", date_created='" + date_created + '\'' +
                '}';
    }
}
