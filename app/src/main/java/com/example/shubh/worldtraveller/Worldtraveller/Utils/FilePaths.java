package com.example.shubh.worldtraveller.Worldtraveller.Utils;

import android.os.Environment;

/**
 * Created by User on 7/24/2017.
 */

public class FilePaths {


    public String PHOTOS = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();


    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String PICTURES =  ROOT_DIR + "/Pictures";
    public String CAMERA = ROOT_DIR + "/DCIM/camera";
    public String STORIES = ROOT_DIR + "/Stories";

    public String FIREBASE_STORY_STORAGE = "stories/users";
    public String FIREBASE_IMAGE_STORAGE = "photos/users/";






}
