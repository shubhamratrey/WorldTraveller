package com.example.shubh.worldtraveller.Worldtraveller.registration;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.FirebaseMethods;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.UniversalImageLoader;
import com.example.shubh.worldtraveller.Worldtraveller.home.Activity_Home;
import com.example.shubh.worldtraveller.Worldtraveller.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfile extends AppCompatActivity {

    //statics var
    private static final String TAG = "Update_profile";
    private Context mContext = UpdateProfile.this;
    //Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserDatabase;
    private FirebaseMethods mFirebaseMethods;
    //var
    private TextView mUpdatedUsername;
    private EditText mFullname, mDescription;
    private CircleImageView mUpdatedProfilePicture;
    private Button mSaveProfileUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        setupFirebaseAuth();
        initImageLoader();
        updateDetails();
        setupWidgets();

        mSaveProfileUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String displayName = mFullname.getText().toString();
                final String description = mDescription.getText().toString();

                Log.d(TAG, "onClick: Attempting to submit to database: \n");

                //handle the exception if the EditText fields are null
                if (!displayName.equals("") && !description.equals("")) {
                    saveProfileSettings();
                    startActivity(new Intent(mContext, Activity_Home.class));

                } else {
                    Toast.makeText(mContext, "Fill out all the fields", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void saveProfileSettings() {
        final String displayName = mFullname.getText().toString();
        final String description = mDescription.getText().toString();

        if (!displayName.equals("") && !description.equals("")) {

            mFirebaseMethods.addNewUser(null, displayName, description, 0, 0, 0, null);
            //update display_name
           /* mFirebaseMethods.updateUserAccountSettings(displayName, null);
            //update description
            mFirebaseMethods.updateUserAccountSettings(null, description);*/

            Toast.makeText(mContext, "New Information has been saved", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(mContext, "Fill out all the fields", Toast.LENGTH_SHORT).show();

        }


        Log.d(TAG, "onClick: uptohere " + displayName + "  " + description);
    }


    private void setupWidgets() {
        mUpdatedUsername = (TextView) findViewById(R.id.updated_username);
        mUpdatedProfilePicture = (CircleImageView) findViewById(R.id.updated_profile_picture);
        mFullname = (EditText) findViewById(R.id.update_fullname);
        mDescription = (EditText) findViewById(R.id.update_description);
        mSaveProfileUpdate = (Button) findViewById(R.id.updateProfileDetails);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDatabase = mFirebaseDatabase.getReference();
        mFirebaseMethods = new FirebaseMethods(mContext);

    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());

    }

    private void updateDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //retrieve user information from the database
                setProfileWidgets(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setProfileWidgets(DataSnapshot dataSnapshot) {
        UserAccountSettings settings = new UserAccountSettings();

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            UserAccountSettings uInfo = new UserAccountSettings();
            uInfo.setDisplay_name(ds.child("profile").child(userID).getValue(UserAccountSettings.class).getDisplay_name()); //set the name
            uInfo.setDescription(ds.child("profile").child(userID).getValue(UserAccountSettings.class).getDescription());
            uInfo.setClicks(ds.child("profile").child(userID).getValue(UserAccountSettings.class).getClicks());//set the clicks
            uInfo.setConnections(ds.child("profile").child(userID).getValue(UserAccountSettings.class).getConnections());
            uInfo.setStars(ds.child("profile").child(userID).getValue(UserAccountSettings.class).getStars());
            uInfo.setProfile_photo(ds.child("profile").child(userID).getValue(UserAccountSettings.class).getProfile_photo());
            uInfo.setUser_id(ds.child("profile").child(userID).getValue(UserAccountSettings.class).getUser_id());
            uInfo.setUsername(ds.child("profile").child(userID).getValue(UserAccountSettings.class).getUsername());


            //display all the information
            Log.d(TAG, "showData: name: " + uInfo.getDisplay_name());
            Log.d(TAG, "showData: description: " + uInfo.getDescription());
            Log.d(TAG, "showData: clicks: " + uInfo.getClicks());
            Log.d(TAG, "showData: connections: " + uInfo.getConnections());
            Log.d(TAG, "showData: stars: " + uInfo.getStars());
            Log.d(TAG, "showData: userid: " + uInfo.getUser_id());
            Log.d(TAG, "showData: username: " + uInfo.getUsername());


            mUpdatedUsername.setText(uInfo.getUsername());
            //mProfileDescription.setText(uInfo.getDescription());

            String imgURL = uInfo.getProfile_photo();
            Log.d(TAG, "setProfilePhoto: setting up profile photo" + imgURL);
            UniversalImageLoader.setImage(imgURL, mUpdatedProfilePicture, null, "");
        }


    }

    ////////////////////////Firebase//////////////////

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDatabase = mFirebaseDatabase.getReference();

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

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
