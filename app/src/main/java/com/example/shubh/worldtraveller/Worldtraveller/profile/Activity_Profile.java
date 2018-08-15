package com.example.shubh.worldtraveller.Worldtraveller.profile;

import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.BottomNavigationViewHelper;
import com.example.shubh.worldtraveller.Worldtraveller.adapters.GridImageAdapter;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.UniversalImageLoader;
import com.example.shubh.worldtraveller.Worldtraveller.models.Comment;
import com.example.shubh.worldtraveller.Worldtraveller.models.Photo;
import com.example.shubh.worldtraveller.Worldtraveller.models.Star;
import com.example.shubh.worldtraveller.Worldtraveller.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_Profile extends AppCompatActivity {

    private static final String TAG = "Activity_Profile";
    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    private TextView mProfileName, mProfileDescription, mClicks, mConnections, mStars, mHonor;
    private CircleImageView profilePhoto;
    private GridView gridView;
    private ImageView profileMenu;
    private int mStarCount, mClickCount;
    private Context mContext = Activity_Profile.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: starting");

        setupBottomNavigationView();
        setupInitialization();
        initImageLoader();
        setupWidget();
        setupGridView();


    }

    private void setupInitialization() {
        gridView = (GridView) findViewById(R.id.profile_gridview);
        profilePhoto = (CircleImageView) findViewById(R.id.profile_photo);
        mProfileName = (TextView) findViewById(R.id.profile_name);
        mProfileDescription = (TextView) findViewById(R.id.profile_description);
        mClicks = (TextView) findViewById(R.id.profile_clicks);
        mConnections = (TextView) findViewById(R.id.profile_connections);
        mStars = (TextView) findViewById(R.id.profile_stars);
        mHonor = (TextView) findViewById(R.id.profile_honor);
        profileMenu = (ImageView) findViewById(R.id.profile_setting);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to account settings.");
                Intent intent = new Intent(mContext, Activity_ProfileEdit.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void setupWidget() {
        Log.d(TAG, "setupWidget: ");
        DatabaseReference ProfileWidget = FirebaseDatabase.getInstance().getReference();
        ProfileWidget.keepSynced(true);
        Query profilequery = ProfileWidget
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_profile))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        profilequery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                UserAccountSettings todo = dataSnapshot.getValue(UserAccountSettings.class);
                UniversalImageLoader.setImage(todo.getProfile_photo(), profilePhoto, null, "");
                mProfileName.setText(todo.getDisplay_name());
                mProfileDescription.setText(todo.getDescription());
                Linkify.addLinks(mProfileDescription,Linkify.ALL);//Adding links to description
                mProfileDescription.setLinkTextColor(getResources().getColor(R.color.google_red)); //adding colors to link
                mProfileDescription.setLinksClickable(true);//making it clickable
                mClicks.setText(String.valueOf(todo.getClicks()));
                mConnections.setText(String.valueOf(todo.getConnections()));

                long stars = todo.getStars();
                getStarCount(stars);
                setHonor(stars); //beginner//Amature//professional//star

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    private void setStarCount() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);
        Query query = reference
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_posts))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.field_user_id));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mStarCount = 0;
                mClickCount = 0;
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mClickCount++;
                    for (DataSnapshot dSnapshot : singleSnapshot
                            .child(getString(R.string.field_stars)).getChildren()) {
                        String number = dSnapshot.getValue(Star.class).getStars();
                        int result = Integer.parseInt(number);
                        mStarCount = mStarCount + result;
                        Log.d(TAG, "onDataChange: Inside set Star count " + number);
                    }

                }

                if (mStarCount != 0) {
                    reference.child(mContext.getString(R.string.dbname_users))
                            .child(mContext.getString(R.string.dbname_profile))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(mContext.getString(R.string.field_stars))
                            .setValue(mStarCount);
                }
                if (mClickCount != 0) {
                    reference.child(mContext.getString(R.string.dbname_users))
                            .child(mContext.getString(R.string.dbname_profile))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(mContext.getString(R.string.field_clicks))
                            .setValue(mClickCount);
                }
                Log.d(TAG, "onDataChange: starcount" + mStarCount+" clickcount "+mClickCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getStarCount(long stars) {

        if (stars < 1000) {

            mStars.setText(String.valueOf(stars));//999

        } else if (stars >= 1000) {

            if (stars >= 999949) {

                double r = stars * 1.0 / 1000000.0;

                Log.d(TAG, "onDataChange:k1 result " + String.valueOf(stars) + "  " + r);
                double r_roundOff = Math.round(r * 10.0) / 10.0;

                Log.d(TAG, "onDataChange: roundoff" + r_roundOff);
                mStars.setText(r_roundOff + "m");//9999949

            } else {
                double s = stars * 1.0 / 1000.0;

                Log.d(TAG, "onDataChange:k1 result " + String.valueOf(stars) + "   " + String.valueOf(stars / 1000) + "  " + s);
                double k_roundOff = Math.round(s * 10.0) / 10.0;

                Log.d(TAG, "onDataChange: roundoff" + k_roundOff);
                mStars.setText(k_roundOff + "k");///
            }

        }


    }

    private void setupGridView() {
        // Log.d(TAG, "setupGridView: Setting up image grid.");

        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);
        Query query = reference
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_posts))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).orderByValue();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                    try {

                        Log.d(TAG, "onDataChange: Inside setupgridview");
                        photo.setTitle(objectMap.get(getString(R.string.field_title)).toString());
                        photo.setDescription(objectMap.get(getString(R.string.field_description)).toString());
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
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
                    } catch (NullPointerException e) {
                        Log.e(TAG, "onDataChange: NullPointerException: " + e.getMessage());
                    }
                }

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
                
                gridView.setAdapter(adapter);
                gridView.bringToFront();
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

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }

        });
    }

    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigation:setting up bottom navigation view");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation_home);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setStarCount();
    }
}
