package com.example.shubh.worldtraveller.Worldtraveller.profile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.adapters.LikeListAdapter;
import com.example.shubh.worldtraveller.Worldtraveller.models.Photo;
import com.example.shubh.worldtraveller.Worldtraveller.models.Star;
import com.example.shubh.worldtraveller.Worldtraveller.models.UserAccountSettings;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.net.UnknownServiceException;
import java.util.ArrayList;

public class Activity_Likes extends AppCompatActivity {

    private static final String TAG = "ViewLikes";
    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    //widgets
    private ImageView mBackArrow;
    private ListView mListView;
    private LikeListAdapter adapter;

    //vars
    private ArrayList<Star> mLikes;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__likes);
        mBackArrow = (ImageView) findViewById(R.id.backArrow);
        mListView = (ListView) findViewById(R.id.listView);
        mLikes = new ArrayList<>();
        mContext = getApplicationContext();
        try {
            Log.d(TAG, "onCreateView: Bundle");
            setupFirebaseAuth();
            setupWidgets();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }


    }

    private void setupWidgets() {

        adapter = new LikeListAdapter(mContext,
                R.layout.layout_likes, mLikes);
        mListView.setAdapter(adapter);

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Backbtn", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().popBackStack();
            }
        });
    }

    private Photo getPhotoFromBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.photo));
        } else {
            return null;
        }
    }

    /*------------------------------------ Firebase --------------------------------------------- */

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

        Query query = myRef
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_posts))
                .child(getPhotoFromBundle().getUser_id())
                .child(getPhotoFromBundle().getPhoto_id())
                .child(getString(R.string.field_stars));

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Star star = dataSnapshot.getValue(Star.class);
                mLikes.add(star);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

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