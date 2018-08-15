package com.example.shubh.worldtraveller.Worldtraveller.chats;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.OnSwipeTouchListener;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.UniversalImageLoader;



public class ImageFragment extends Fragment {
    private static final String TAG = "Image Fragment";

    public ImageFragment() {
        super();
        setArguments(new Bundle());
    }

    private ImageView mPhoto;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        mPhoto = (ImageView) view.findViewById(R.id.fragment_image);
        UniversalImageLoader.setImage(getPhotoFromBundle(), mPhoto, null, "");
        return view;
    }


    private String getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getString(getString(R.string.photo));
        } else {
            return null;
        }
    }

}
