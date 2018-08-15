package com.example.shubh.worldtraveller.Worldtraveller.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.FirebaseMethods;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.UniversalImageLoader;
import com.example.shubh.worldtraveller.Worldtraveller.gallery.Activity_Camera;
import com.example.shubh.worldtraveller.Worldtraveller.models.UserAccountSettings;
import com.example.shubh.worldtraveller.Worldtraveller.registration.Activity_SignIn;
import com.example.shubh.worldtraveller.Worldtraveller.registration.ProfilePhotoGallery;
import com.example.shubh.worldtraveller.Worldtraveller.registration.Registration_Details;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_ProfileEdit extends AppCompatActivity {

    private static final String TAG = "Activity_profileedit";
    public ProgressBar profileedit_progress;
    public String userID, imgUrl;


    //widgets
    public ImageButton mEditProfilebtn, mSaveProfilebtn;
    public EditText mEditDisplayName, mEditDescription;
    public TextView mTextDisplayName, mTextDescription;
    public View mEditView, mTextView , mSignout;
    public CircleImageView mProfilePhoto, mChangeProfilePhoto;
    public Button mBackButton;


    UserAccountSettings settings;
    private FirebaseMethods mFirebaseMethods;
    private Bitmap bitmap;
    private String mAppend = "file:/";
    private UserAccountSettings mUserSettings;
    private Context mContext = Activity_ProfileEdit.this;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile_edit);

        setupFirebaseAuth();
        setupActivityWidgets();
        getIncomingIntent();
        setwidgets();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mFirebaseMethods = new FirebaseMethods(mContext);

        setImage();
    }


    private void setupActivityWidgets() {

        mBackButton = (Button) findViewById(R.id.profile_edit_backbtn);
        mProfilePhoto = (CircleImageView) findViewById(R.id.profileEdit_photo);
        profileedit_progress = (ProgressBar) findViewById(R.id.profileedit_progressbar);
        mChangeProfilePhoto = (CircleImageView) findViewById(R.id.changeProfilePhoto);


        settings = new UserAccountSettings();
        mEditProfilebtn = (ImageButton) findViewById(R.id.ProfileEditBtn1);
        mSaveProfilebtn = (ImageButton) findViewById(R.id.ProfileSaveBtn1);
        mEditDisplayName = (EditText) findViewById(R.id.profileEdit_name);
        mEditDescription = (EditText) findViewById(R.id.profileEdit_description);
        mTextDisplayName = (TextView) findViewById(R.id.profileText_name);
        mTextDescription = (TextView) findViewById(R.id.profileText_description);
        mEditView = findViewById(R.id.visibleEdit);
        mTextView = findViewById(R.id.visibleText);
        mSignout= (TextView) findViewById(R.id.signout);

    }

    private void setwidgets() {

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing this activity ");
                finish();
            }
        });
        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: changing profile photo");
                /*Intent intent = new Intent(mContext, Activity_Camera.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //268435456
                startActivity(intent);
                finish();*/

                Log.d(TAG, "onClick: mphoto");
                ProfilePhotoGallery fragment = new ProfilePhotoGallery();
                FragmentTransaction transaction = Activity_ProfileEdit.this.getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slidebottom, R.anim.fade_out);
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack(getString(R.string.profile_fragment));
                transaction.commit();
            }
        });

        mEditProfilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mEditProfilebtn.setVisibility(View.GONE);
                mSaveProfilebtn.setVisibility(View.VISIBLE);
                mEditDisplayName.setVisibility(View.VISIBLE);
                mEditDescription.setVisibility(View.VISIBLE);
                mTextDisplayName.setVisibility(View.GONE);
                mTextDescription.setVisibility(View.GONE);
                mEditView.setVisibility(View.VISIBLE);
                mTextView.setVisibility(View.GONE);

            }
        });

        mSaveProfilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String displayName = mEditDisplayName.getText().toString();
                final String description = mEditDescription.getText().toString();


                Log.d(TAG, "onClick: Attempting to submit to database: \n");


                //handle the exception if the EditText fields are null
                if (!displayName.equals("") && !description.equals("")) {
                    saveProfileSettings();
                    mEditProfilebtn.setVisibility(View.VISIBLE);
                    mSaveProfilebtn.setVisibility(View.GONE);
                    mEditDisplayName.setVisibility(View.GONE);
                    mEditDescription.setVisibility(View.GONE);
                    mTextDisplayName.setVisibility(View.VISIBLE);
                    mTextDescription.setVisibility(View.VISIBLE);
                    mEditView.setVisibility(View.GONE);
                    mTextView.setVisibility(View.VISIBLE);
                } else {
                    toastMessage("Fill out all the fields");
                }

            }
        });

        mSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                finish();
            }
        });
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

    private void saveProfileSettings() {
        final String displayName = mEditDisplayName.getText().toString();
        final String description = mEditDescription.getText().toString();


        Log.d(TAG, "onClick: Attempting to submit to database: \n");


        //handle the exception if the EditText fields are null
        if (!displayName.equals("") && !description.equals("")) {

            //update display_name
            mFirebaseMethods.updateUserAccountSettings(displayName, null);
            //update description
            mFirebaseMethods.updateUserAccountSettings(null, description);

            toastMessage("New Information has been saved.");
            mTextDisplayName.setText(displayName);
            mTextDescription.setText(description);
        } else {
            toastMessage("Fill out all the fields");
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
                    FirebaseMethods firebaseMethods = new FirebaseMethods(Activity_ProfileEdit.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, null, 0,
                            intent.getStringExtra(getString(R.string.selected_image)), null);
                } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                    //set the new profile picture
                    FirebaseMethods firebaseMethods = new FirebaseMethods(Activity_ProfileEdit.this);
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


    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check user if he is logged in or not

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Log.d(TAG, "onAuthStateChanged: navigating back to login activity");
                    Intent intent = new Intent(Activity_ProfileEdit.this, Activity_SignIn.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                //retrieve user information from the database
                //setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));

                dataSnapshot.getValue();
                //retrieve images for the user in question

               /* UserAccountSettings uInfo = new UserAccountSettings();

                //display all the information
                Log.d(TAG, "showData: name: " + uInfo.getDisplay_name());
                Log.d(TAG, "showData: description: " + uInfo.getDescription());


                mTextDisplayName.setText(uInfo.getDisplay_name());
                mTextDescription.setText(uInfo.getDescription());

                String imgURL = uInfo.getProfile_photo();
                Log.d(TAG, "setProfilePhoto: setting up profile photo" + imgURL);
                UniversalImageLoader.setImage(imgURL, mProfilePhoto, null, "");*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
