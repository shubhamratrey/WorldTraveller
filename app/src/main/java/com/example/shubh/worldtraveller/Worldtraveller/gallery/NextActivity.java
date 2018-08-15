package com.example.shubh.worldtraveller.Worldtraveller.gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.shubh.worldtraveller.Worldtraveller.Utils.StringManipulation;
import com.example.shubh.worldtraveller.Worldtraveller.models.Camera;
import com.example.shubh.worldtraveller.Worldtraveller.models.Photo;
import com.example.shubh.worldtraveller.Worldtraveller.profile.Activity_Profile;
import com.example.shubh.worldtraveller.Worldtraveller.profile.Activity_ProfileEdit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.FirebaseMethods;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.UniversalImageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by User on 7/24/2017.
 */

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";
    private String make, model, iso, focal_length, shutter_speed, aperture, flash, datetime,lens;
    private String final_make, final_model ,final_iso, final_focal_length, final_shutter_speed, final_aperture, final_flash;


    private Context mContext = NextActivity.this;
    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    //widgets
    private EditText mTitle, mDescription;
    private TextView mISO, mMake, mModel, mExposure, mAperture, mFocalLength, mFlash,mLens;
    //vars
    private String mAppend = "file:/";
    private int imageCount = 0;
    private String imgUrl;
    private Bitmap bitmap;
    private Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        mFirebaseMethods = new FirebaseMethods(NextActivity.this);


        setupActivityWidgets();
        setupFirebaseAuth();
        ImageView backArrow = (ImageView) findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the activity");
                finish();
            }
        });


        RelativeLayout share = (RelativeLayout) findViewById(R.id.layout_share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen.");
                //upload the image to firebase
                Toast.makeText(NextActivity.this, "Attempting to upload new photo", Toast.LENGTH_SHORT).show();
                String title = mTitle.getText().toString();
                String description = mDescription.getText().toString();

                if (intent.hasExtra(getString(R.string.selected_image))) {
                    imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), title, description, imageCount, imgUrl, null);

                    //final_make, final_model ,final_iso, final_focal_length, final_shutter_speed, final_aperture, final_flash;
                    mFirebaseMethods.PhotoDetails(make, model, iso, focal_length, shutter_speed, aperture, flash);
                    Intent intent = new Intent(mContext, Activity_Profile.class);
                    startActivity(intent);
                    finish();
                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo), title, description, imageCount, null, bitmap);
                    mFirebaseMethods.PhotoDetails(make, model, iso, focal_length, shutter_speed, aperture, flash);
                }
            }
        });

        setImage();
        Checkdetails();
        setWidgets();

    }


    private void setupActivityWidgets() {
        mTitle = (EditText) findViewById(R.id.camera_title);
        mDescription = (EditText) findViewById(R.id.description);

        mMake = (TextView) findViewById(R.id.camera_manufacturer_name);
        mModel = (TextView) findViewById(R.id.camera_model_name);
        mExposure = (TextView) findViewById(R.id.camera_shutterspeed_textview);
        mAperture = (TextView) findViewById(R.id.camera_aperture_textview);
        mFocalLength = (TextView) findViewById(R.id.camera_focal_length_textview);
        mISO = (TextView) findViewById(R.id.camera_iso_textview);
        mFlash = (TextView) findViewById(R.id.camera_flash_textview);
    }

    /**
     * gets the image url from the incoming intent and displays the chosen image
     */
    public void Checkdetails() {


        intent = getIntent();
        imgUrl = intent.getStringExtra(getString(R.string.selected_image));

        try {
            ExifInterface exifInterface = new ExifInterface(imgUrl);

            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            datetime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            focal_length = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
            model = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
            iso = exifInterface.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS);
            shutter_speed = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            aperture = exifInterface.getAttribute(ExifInterface.TAG_APERTURE);
            flash = exifInterface.getAttribute(ExifInterface.TAG_FLASH);
            make = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
            //lens= exifInterface.getAttribute(ExifInterface.TAG_)


            // Now you can extract any Exif tag you want
            // Assuming the image is a JPEG or supported raw format


            Log.d(TAG, "setImage: orientation must be set=" + orientation + " details=" + datetime +
                    " focal length=" + focal_length + " model=" + model + " iso=" + iso + " shutter speed=" + shutter_speed
                    + " aperture=" + aperture + " flash=" + flash + " flash1=" + make);

            /*double number1 = 10.123456;
            double number2 = (int)Math.round(shutter_speed * 100)/(double)100;
            System.out.println(number2);*/


        } catch (IOException e) {
        }
    }

    private void setWidgets() {
        Log.d(TAG, "setWidgets: set all valures");
        if (shutter_speed != null || focal_length != null || model != null || aperture != null || iso != null || flash != null || make != null) {
            double d = Double.parseDouble(shutter_speed);
            double a = d * 100;
            String total2 = String.valueOf(a);
            mExposure.setText(total2);

            String focal = focal_length + " mm";
            mFocalLength.setText(focal);
            mMake.setText(make);
            mModel.setText(model);
            mAperture.setText("f/" + aperture);
            mISO.setText(iso);
            mFlash.setText(flash);
        } else {
            String focal = "not available";
            mExposure.setText(focal);
            mFocalLength.setText(focal);
            mModel.setText(focal);
            mAperture.setText(focal);
            mISO.setText(focal);
            mFlash.setText(focal);
            mMake.setText("");
            mDescription.setHint(" I dnt knw what to do");
        }

/*
        if (shutter_speed != null) {
            double d = Double.parseDouble(shutter_speed);
            double a = d * 100;
            String total2 = String.valueOf(a);
            mExposure.setText(total2);
        }
        if (focal_length != null) {
            String focal = focal_length + " mm";
            mFocalLength.setText(focal);
        }
        if (model != null) {
            mModel.setText(model);
        }

        if (aperture != null) {
            mAperture.setText("f/" + aperture);
        }

        if (iso != null) {
            mISO.setText(iso);
        }
        if (flash != null) {
            mFlash.setText(flash);
        }*/

    }

    private void setImage() {
        intent = getIntent();
        ImageView image = (ImageView) findViewById(R.id.imageShare);

        if (intent.hasExtra(getString(R.string.selected_image))) {
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: got new image url: " + imgUrl);
            UniversalImageLoader.setImage(imgUrl, image, null, mAppend);
        } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
            bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "setImage: got new bitmap");
            image.setImageBitmap(bitmap);
        }
    }

     /*
     ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        Log.d(TAG, "onDataChange: image count: " + imageCount);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // user is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                imageCount = mFirebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: image count: " + imageCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
