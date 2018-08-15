package com.example.shubh.worldtraveller.Worldtraveller.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.BottomNavigationViewHelper;
import com.example.shubh.worldtraveller.Worldtraveller.adapters.GridImageAdapter;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.UniversalImageLoader;
import com.example.shubh.worldtraveller.Worldtraveller.profile.Activity_Post;
import com.example.shubh.worldtraveller.Worldtraveller.models.Comment;
import com.example.shubh.worldtraveller.Worldtraveller.models.Photo;
import com.example.shubh.worldtraveller.Worldtraveller.models.Star;
import com.example.shubh.worldtraveller.Worldtraveller.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileFragment extends AppCompatActivity {

    private static final String TAG = "ViewProfileFragment";

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;


    //widgets
    private TextView mProfileName, mProfileDescription, mClicks, mConnections, mStars, mHonor;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private ImageButton mFollow, mUnfollow, mBackButton, mSetting;
    private Context mContext = ViewProfileFragment.this;


    //vars

    private UserAccountSettings mUserAccountSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        Log.d(TAG, "onCreate: starting");

        setupFirebaseAuth();
        setupBottomNavigationView();
        setupInitialization();
        init();
    }


    private void setupInitialization() {
        gridView = (GridView) findViewById(R.id.profile_gridview);
        mProfilePhoto = (CircleImageView) findViewById(R.id.profile_photo);
        mProfileName = (TextView) findViewById(R.id.profile_name);
        mProfileDescription = (TextView) findViewById(R.id.profile_description);
        mClicks = (TextView) findViewById(R.id.profile_clicks);
        mConnections = (TextView) findViewById(R.id.profile_connections);
        mStars = (TextView) findViewById(R.id.profile_stars);
        mHonor = (TextView) findViewById(R.id.profile_honor);
        mBackButton = (ImageButton) findViewById(R.id.profileView_backbtn);
        mSetting = (ImageButton) findViewById(R.id.profileView_setting);
        mFollow = (ImageButton) findViewById(R.id.AddConnection);
        mUnfollow = (ImageButton) findViewById(R.id.RemoveConnection);
        /*profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to account settings.");
                Intent intent = new Intent(mContext, Activity_ProfileEdit.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });*/


        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d(TAG, "onClick: now following: " + mUserAccountSettings.getUsername());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_users))
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(getUserID())
                        .child(getString(R.string.field_user_id))
                        .setValue(getUserID());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_users))
                        .child(getString(R.string.dbname_followers))
                        .child(getUserID())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(getString(R.string.field_user_id))
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                setFollowing();
            }
        });


        mUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: now unfollowing: " + mUserAccountSettings.getUsername());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_users))
                        .child(getString(R.string.dbname_following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(getUserID())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.dbname_users))
                        .child(getString(R.string.dbname_followers))
                        .child(getUserID())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();
                setUnfollowing();
            }
        });
    }

    private void init() {
        //set the profile widgets
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
        Query query = reference1
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_profile))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(getUserID());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue(UserAccountSettings.class).toString());
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);

                    UniversalImageLoader.setImage(mUserAccountSettings.getProfile_photo(), mProfilePhoto, null, "");
                    mProfileName.setText(mUserAccountSettings.getDisplay_name());
                    mProfileDescription.setText(mUserAccountSettings.getDescription());
                    mClicks.setText(String.valueOf(mUserAccountSettings.getClicks()));

                    long connections = mUserAccountSettings.getConnections();
                    long stars = mUserAccountSettings.getStars();
                    getStarCount(stars, connections);
                    setHonor(stars); //beginner//Amature//professional//star

                    isFollowing();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //get the users profile photos

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference();
        Query query2 = reference2
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_posts))
                .child(getUserID());
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    ArrayList<Photo> photos = new ArrayList<Photo>();
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        photo.setTitle(objectMap.get(getString(R.string.field_title)).toString());
                        photo.setDescription(objectMap.get(getString(R.string.field_description)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        ArrayList<Comment> comments = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            comments.add(comment);
                        }

                        photo.setComments(comments);

                        ArrayList<Star> likesList = new ArrayList<Star>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_stars)).getChildren()) {
                            //  Log.d(TAG, "onDataChange: " + dSnapshot);
                            /*Star like = new Star();
                            like.setUser_id(dSnapshot.getValue(Star.class).getUser_id());
                            like.setStars(dSnapshot.getValue(Star.class).getStars());
                            likesList.add(like);*/
                        }
                        photo.setStars(likesList);
                        photos.add(photo);
                    }
                    setupImageGrid(photos);
                }
                else {
                    //add new like
                    Toast.makeText(mContext, "No photos yet.!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private void getStarCount(long stars, long connections) {

        if (stars < 1000) {
            mStars.setText(String.valueOf(stars));//999
        } else if (stars >= 1000) {
            if (stars >= 999949) {
                double r = stars * 1.0 / 1000000.0;
                double r_roundOff = Math.round(r * 10.0) / 10.0;
                mStars.setText(r_roundOff + "m");//9999949

            } else {
                double s = stars * 1.0 / 1000.0;
                double k_roundOff = Math.round(s * 10.0) / 10.0;
                mStars.setText(k_roundOff + "k");///
            }
        }


        if (connections < 1000) {
            mConnections.setText(String.valueOf(connections));//999
        } else if (connections >= 1000) {
            if (connections >= 999949) {
                double r = connections * 1.0 / 1000000.0;
                double r_roundOff = Math.round(r * 10.0) / 10.0;
                mConnections.setText(r_roundOff + "m");//9999949

            } else {
                double s = connections * 1.0 / 1000.0;
                double k_roundOff = Math.round(s * 10.0) / 10.0;
                mConnections.setText(k_roundOff + "k");///
            }
        }


    }

    private void setHonor(long stars) {

        if (stars < 1000) {
            mHonor.setText("Beginner");
        } else {
            if (stars < 100000) {
                mHonor.setText("Amature");
            } else {
                if (stars < 999949) {
                    mHonor.setText("Professional");
                } else {
                    mHonor.setText("Star");
                }
            }
        }
    }

    private void isFollowing() {
        Log.d(TAG, "isFollowing: checking if following this users.");
        setUnfollowing();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.field_user_id)).equalTo(getUserID());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue());

                    setFollowing();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setFollowing() {
        Log.d(TAG, "setFollowing: updating UI for following this user");
        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.VISIBLE);
    }

    private void setUnfollowing() {
        Log.d(TAG, "setFollowing: updating UI for unfollowing this user");
        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(View.GONE);
    }

    private void setupImageGrid(final ArrayList<Photo> photos) {
        //setup our image grid
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        ArrayList<String> imgUrls = new ArrayList<String>();
        for (int i = 0; i < photos.size(); i++) {
            imgUrls.add(photos.get(i).getImage_path());
        }

        GridImageAdapter adapter = new GridImageAdapter(mContext,
                R.layout.layout_storyline_grid_imageview, "", imgUrls);
        Log.d(TAG, "setupImageGrid: " + imgUrls);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int p, long id) {

                //mOnGridImageSelectedListener.onGridImageSelected(photos.get(p), ACTIVITY_NUM);
                Intent intent = new Intent(mContext, Activity_Post.class);
                intent.putExtra("PHOTO", photos.get(p));
                intent.putExtra(mContext.getString(R.string.activity_number), ACTIVITY_NUM);
                //   intent.putExtra("mRandomID",mRandomID);
                        /*final Animation slide_right = AnimationUtils.loadAnimation(mContext, R.anim.dailog_blink_in);
                        view.startAnimation(slide_right);*/
                startActivity(intent);

                //overridePendingTransition(R.anim.slide_right, R.anim.slide_right);

            }
        });

    }

    private UserAccountSettings getUserFromBundle() {

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.intent_user));
        } else {
            return null;
        }
    }

    private int getActivityNumFromBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            return bundle.getInt(getString(R.string.activity_number));
        } else {
            return 0;
        }
    }

    private String getUserID() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            return bundle.getString(getString(R.string.field_user_id));
        } else {
            return null;
        }
    }

    /*
  ------------------------------------ Firebase ---------------------------------------------
   */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigation:setting up bottom navigation view");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation_home);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(getActivityNumFromBundle());
        menuItem.setChecked(true);
    }

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
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
