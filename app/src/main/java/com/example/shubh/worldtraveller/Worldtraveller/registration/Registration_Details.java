package com.example.shubh.worldtraveller.Worldtraveller.registration;


import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.FirebaseMethods;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.Permissions;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.StringManipulation;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.UniversalImageLoader;
import com.example.shubh.worldtraveller.Worldtraveller.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class Registration_Details extends AppCompatActivity {

    //statics var
    private static final String TAG = "Registration_Details";
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    public ProgressBar mProgressbar;
    private Context mContext;
    //Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserDatabase;
    private FirebaseMethods firebaseMethods;
    //vars
    private String userID, imgUrl;
    private CircleImageView mPhotoUpload;
    private Button mNextbtn;
    private EditText mUsername;
    private Bitmap bitmap;
    private String mAppend = "file:/";
    private String append = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_details);
        setupFirebaseAuth();
        setupWidgets();
        if (checkPermissionsArray(Permissions.PERMISSIONS)) {

        } else {
            verifyPermissions(Permissions.PERMISSIONS);
        }
        getIncomingIntent();

        mNextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString();
                checkIfUsernameExists(username);

                Log.d(TAG, "onClick: nextbtn" + username);
                Intent intent = new Intent(mContext, UpdateProfile.class);
                startActivity(intent);
                finish();
            }
        });
        mPhotoUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Progressbar();
                Log.d(TAG, "onClick: mphoto");
                ProfilePhotoGallery fragment = new ProfilePhotoGallery();
                FragmentTransaction transaction = Registration_Details.this.getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slidebottom, R.anim.fade_out);
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack(getString(R.string.profile_fragment));
                transaction.commit();


            }
        });

        setImage();
    }

    private void setupWidgets() {
        mContext = Registration_Details.this;
        firebaseMethods = new FirebaseMethods(mContext);
        mPhotoUpload = (CircleImageView) findViewById(R.id.changeProfilePhoto1);
        mNextbtn = (Button) findViewById(R.id.nextbtn_registration);
        mUsername = (EditText) findViewById(R.id.register_username);
        mProgressbar = (ProgressBar) findViewById(R.id.regisration_progressbar);
    }

    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: Checking if  " + username + " already exists.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(mContext.getString(R.string.dbname_users))
                .child(mContext.getString(R.string.dbname_profile))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                       /// mUsername.setError("Already Exists");
                        append = mUserDatabase.push().getKey().substring(3, 6);//   " \"ROM\" "
                        Toast.makeText(mContext, "The '"+username +"' was not available so we added "+username+" + "+append, Toast.LENGTH_LONG).show();

                        Log.d(TAG, "onDataChange: username already exists. Appending random string to name: " + append);
                    }
                }

                String mUsername = "";
                mUsername = username + append;

                //add new user to the database
                /*String username, String display_name, String description, long clicks,
                            long connections, long stars, String user_id*/
                firebaseMethods.addNewUser(StringManipulation.condenseUsername(mUsername), null, null,0,0,0,FirebaseAuth.getInstance().getCurrentUser().getUid().toString());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void Progressbar() {
        mProgressbar = (ProgressBar) findViewById(R.id.regisration_progressbar);
        mProgressbar.setVisibility(View.VISIBLE);

    }

    private void setImage() {
        Intent intent = getIntent();
        ImageView image = (ImageView) findViewById(R.id.profileEdit_photo);

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

    private void getIncomingIntent() {
        Intent intent = getIntent();

        if (intent.hasExtra(getString(R.string.selected_image))
                || intent.hasExtra(getString(R.string.selected_bitmap))) {

            //if there is an imageUrl attached as an extra, then it was chosen from the gallery/photo fragment
            Log.d(TAG, "getIncomingIntent: New incoming imgUrl");
            if (intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile_fragment))) {

                if (intent.hasExtra(getString(R.string.selected_image))) {
                    //set the new profile picture

                    /*String photoType, final String title,final String description,final int count, final String imgUrl, Bitmap bm){*/
                    FirebaseMethods firebaseMethods = new FirebaseMethods(Registration_Details.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, null, 0,
                            intent.getStringExtra(getString(R.string.selected_image)), null);
                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    //set the new profile picture
                    FirebaseMethods firebaseMethods = new FirebaseMethods(Registration_Details.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, null, 0,
                            null, (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap)));
                }

            }

        }

        if (intent.hasExtra(getString(R.string.calling_activity))) {
            Log.d(TAG, "getIncomingIntent: received incoming intent from " + getString(R.string.profile_activity));
            //  setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
        }
    }

    ////////////////////////////Permissions////////////////

    public void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(
                Registration_Details.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    public boolean checkPermissionsArray(String[] permissions) {
        Log.d(TAG, "checkPermissionsArray: checking permissions array.");

        for (int i = 0; i < permissions.length; i++) {
            String check = permissions[i];
            if (!checkPermissions(check)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkPermissions(String permission) {
        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(Registration_Details.this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: \n Permission was not granted for: " + permission);
            return false;
        } else {
            Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
            return true;
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
