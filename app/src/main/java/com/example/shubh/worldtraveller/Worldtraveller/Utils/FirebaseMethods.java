package com.example.shubh.worldtraveller.Worldtraveller.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.gallery.NextActivity;
import com.example.shubh.worldtraveller.Worldtraveller.models.Camera;
import com.example.shubh.worldtraveller.Worldtraveller.models.Photo;
import com.example.shubh.worldtraveller.Worldtraveller.models.UserAccountSettings;
import com.example.shubh.worldtraveller.Worldtraveller.profile.Activity_ProfileEdit;
import com.example.shubh.worldtraveller.Worldtraveller.registration.Registration_Details;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Shubh on 18/03/2018.
 */

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userID;//datetime,focal_length,model,iso,shutter_speed,aperture,flash,flash1

    //vars
    private Context mContext;
    private double mPhotoUploadProgress = 0;
    private Camera camera;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = context;

        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public int getImageCount(DataSnapshot dataSnapshot) {
        int count = 0;
        for (DataSnapshot ds : dataSnapshot
                .child(mContext.getString(R.string.dbname_users))
                .child(mContext.getString(R.string.dbname_posts))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getChildren()) {
            count++;
        }
        return count;
    }

    public void uploadNewPhoto(String photoType, final String title, final String description, final int count, final String imgUrl, Bitmap bm) {

        Log.d(TAG, "uploadNewPhoto: attempting to uplaod new photo.");

        FilePaths filePaths = new FilePaths();
        //case1) new photo
        if (photoType.equals(mContext.getString(R.string.new_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading NEW photo.");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count + 1));

            //convert image url to bitmap
            if (bm == null) {
                bm = ImageManager.getBitmap(imgUrl);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 20);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();

                    Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();

                    //add the new photo to 'photos' node and 'user_photos' node
                    addPhotoToDatabase(title, description, firebaseUrl.toString());

                    //navigate to the main feed so the user can see their photo
                    // Intent intent = new Intent(mContext, HomeActivity.class);
                    //mContext.startActivity(intent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });

        }
        //case new profile photo
        else if (photoType.equals(mContext.getString(R.string.profile_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new PROFILE photo");


            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            //convert image url to bitmap
            if (bm == null) {
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 20);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();

                    Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();

                    //insert into 'user_account_settings' node
                    setProfilePhoto(firebaseUrl.toString());

                    //setProgressDetail();

                   /* ((AccountSettingsActivity)mContext).setViewPager(
                            ((AccountSettingsActivity)mContext).pagerAdapter
                                    .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment)));*/

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress) {
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });
        }

    }

    public void addNewUser( String username,
                            String display_name, String description, long clicks,
                            long connections, long stars, String user_id){

        if (username != null) {
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(mContext.getString(R.string.dbname_profile))
                    .child(userID)
                    .child(mContext.getString(R.string.field_username))
                    .setValue(username);
        }
        if (display_name != null) {
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(mContext.getString(R.string.dbname_profile))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(display_name);
        }
        if (description != null) {
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(mContext.getString(R.string.dbname_profile))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }
        if (clicks != 0) {
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(mContext.getString(R.string.dbname_profile))
                    .child(userID)
                    .child(mContext.getString(R.string.field_clicks))
                    .setValue(clicks);
        }
        if (connections != 0) {
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(mContext.getString(R.string.dbname_profile))
                    .child(userID)
                    .child(mContext.getString(R.string.field_connections))
                    .setValue(connections);
        }
        if (stars != 0) {
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(mContext.getString(R.string.dbname_profile))
                    .child(userID)
                    .child(mContext.getString(R.string.field_stars))
                    .setValue(stars);
        }
        if (user_id != null) {
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(mContext.getString(R.string.dbname_profile))
                    .child(userID)
                    .child(mContext.getString(R.string.field_user_id))
                    .setValue(userID);
        }

    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        return sdf.format(new Date());
    }

    public void PhotoDetails(String manufacturer, String model, String iso
            , String focal_length, String shutter_speed,
                             String aperture, String flash) {
        Log.d(TAG, "PhotoDetails: adding photo details to database" + "  Manufacturer=" + manufacturer + "  Model=" + model + "  ISO=" +
                iso + "  Focal Length=" + focal_length + "  Shutter Speed=" + shutter_speed + "  Aperture=" + aperture + "  Flash=" + flash);

        camera = new Camera();
        camera.setManufacturer(manufacturer);
        camera.setCamera_name(model);
        camera.setShutter_speed(shutter_speed);
        camera.setIso(iso);
        camera.setFocal_length(focal_length);
        camera.setAperture(aperture);
        camera.setFlash(flash);

    }

    private void addPhotoToDatabase(String title, String description, String url) {
        Log.d(TAG, "addPhotoToDatabase: adding photo to database.");


        String tags = StringManipulation.getTags(description);
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setTitle(title);
        photo.setDescription(description);
        photo.setDate_created(String.valueOf(ServerValue.TIMESTAMP));
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        //insert into database
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(mContext.getString(R.string.dbname_posts))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newPhotoKey).setValue(photo);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(mContext.getString(R.string.dbname_posts))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newPhotoKey)
                .child(mContext.getString(R.string.field_date_created)).setValue(ServerValue.TIMESTAMP);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(mContext.getString(R.string.dbname_posts))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newPhotoKey)
                .child(mContext.getString(R.string.dbname_camera)).setValue(camera);
    }

    private void setProfilePhoto(String url) {
        Log.d(TAG, "setProfilePhoto: setting new profile image: " + url);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(mContext.getString(R.string.dbname_profile))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }

    public void updateUserAccountSettings(String displayName, String description) {

        Log.d(TAG, "updateUserAccountSettings: updating user account settings.");


        if (displayName != null) {
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(mContext.getString(R.string.dbname_profile))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }

        if (description != null) {
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(mContext.getString(R.string.dbname_profile))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }


    }

    public UserAccountSettings getUserSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserSettings: retrieving user account settings from firebase.");


        UserAccountSettings settings = new UserAccountSettings();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {

            // user_account_settings node
            if (ds.getKey().equals(mContext.getString(R.string.user_account_settings))) {
                Log.d(TAG, "getUserSettings: user account settings node datasnapshot: " + ds);

                try {

                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()
                    );
                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );
                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );
                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()
                    );
                    settings.setClicks(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getClicks()
                    );
                    settings.setConnections(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getConnections()
                    );
                    settings.setStars(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getStars()
                    );

                    Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information: " + settings.toString());
                } catch (NullPointerException e) {
                    Log.e(TAG, "getUserAccountSettings: NullPointerException: " + e.getMessage());
                }
            }


        }
        return settings;

    }
}
