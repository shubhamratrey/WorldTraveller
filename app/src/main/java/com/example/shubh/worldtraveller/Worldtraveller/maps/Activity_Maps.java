package com.example.shubh.worldtraveller.Worldtraveller.maps;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.BottomNavigationViewHelper;
import com.example.shubh.worldtraveller.Worldtraveller.adapters.StorylineRecycleViewAdapter;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.UniversalImageLoader;
import com.example.shubh.worldtraveller.Worldtraveller.models.Album;
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

public class Activity_Maps extends AppCompatActivity {

    ///BzXm5HgdI0U1QZyfoD7m9ncGc5N2 userID
    private static final String TAG = "Activity_Maps";
    private static final int ACTIVITY_NUM = 1;
    private Context mContext = Activity_Maps.this;

    private TextView mProfileName, mProfileDescription, mClicks, mConnections, mStars, mHonor;
    private CircleImageView profilePhoto;


    final ArrayList<Album> albums = new ArrayList<>();
    final UserAccountSettings profileWidgets = new UserAccountSettings();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Log.d(TAG, "onCreate: starting");
        setupBottomNavigationView();
        setupInitialization();

        mRecyclerView = (RecyclerView) findViewById(R.id.storylineRecycleView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new StorylineRecycleViewAdapter(mContext, albums);
        mRecyclerView.setAdapter(mAdapter);

        initImageLoader();
        setupWidget();
        setupAlbumView();
    }

    private void setupInitialization() {
        profilePhoto = (CircleImageView) findViewById(R.id.profile_photo);
        mProfileName = (TextView) findViewById(R.id.profile_name);
        mProfileDescription = (TextView) findViewById(R.id.profile_description);
        mClicks = (TextView) findViewById(R.id.profile_clicks);
        mConnections = (TextView) findViewById(R.id.profile_connections);
        mStars = (TextView) findViewById(R.id.profile_stars);
        mHonor = (TextView) findViewById(R.id.profile_honor);

    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void setupWidget() {
        Log.d(TAG, "setupWidget: ");
        DatabaseReference ProfileWidget = FirebaseDatabase.getInstance().getReference();
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

    private void getStarCount(long stars) {

        if (stars < 1000) {

            mStars.setText(String.valueOf(stars));

        } else if (stars >= 1000) {

            if (stars >= 999949) {

                double r = stars * 1.0 / 1000000.0;

                Log.d(TAG, "onDataChange:k1 result " + String.valueOf(stars) + "  " + r);
                double r_roundOff = Math.round(r * 10.0) / 10.0;

                Log.d(TAG, "onDataChange: roundoff" + r_roundOff);
                mStars.setText(r_roundOff + "m");

            } else {
                double s = stars * 1.0 / 1000.0;

                Log.d(TAG, "onDataChange:k1 result " + String.valueOf(stars) + "   " + String.valueOf(stars / 1000) + "  " + s);
                double k_roundOff = Math.round(s * 10.0) / 10.0;

                Log.d(TAG, "onDataChange: roundoff" + k_roundOff);
                mStars.setText(k_roundOff + "k");
            }

        }


    }

    private void setupAlbumView() {
        Log.d(TAG, "setupAlbumView: Setting up album");
        DatabaseReference albumReference = FirebaseDatabase.getInstance().getReference();
        Query albumQuery = albumReference
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_posts))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        albumQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Album album = new Album();
                    Map<String, Object> albummap = (HashMap<String, Object>) singleSnapshot.getValue();
                    try {
                        album.setAlbum_title(albummap.get(getString(R.string.field_album_title)).toString());
                        album.setAlbum_category(albummap.get(getString(R.string.field_album_category)).toString());
                        album.setAlbum_id(albummap.get(getString(R.string.field_album_id)).toString());
                        Log.d(TAG, "onDataChange: " + album.getAlbum_title().toString());

                        albums.add(album);

                    } catch (NullPointerException e) {
                        Log.e(TAG, "onDataChange: NullPointerException: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
}
