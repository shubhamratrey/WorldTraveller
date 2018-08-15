package com.example.shubh.worldtraveller.Worldtraveller.profile;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.adapters.CommentListAdapter;
import com.example.shubh.worldtraveller.Worldtraveller.models.Comment;
import com.example.shubh.worldtraveller.Worldtraveller.models.Notification;
import com.example.shubh.worldtraveller.Worldtraveller.models.Photo;
import com.example.shubh.worldtraveller.Worldtraveller.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class Activity_Comment extends AppCompatActivity {
    private static final String TAG = "ViewComments";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    //widgets
    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    private ListView mListView;
    private ProgressBar mProgressBar;
    private RelativeLayout mRelativeLayout;

    //vars
    private Photo mPhoto;
    private ArrayList<Comment> mComments;
    private Context mContext;
    private CommentListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_view);
        mBackArrow = (ImageView) findViewById(R.id.backArrow);
        mCheckMark = (ImageView) findViewById(R.id.ivPostComment);
        mComment = (EditText) findViewById(R.id.comment);
        mListView = (ListView) findViewById(R.id.listView);
        mComments = new ArrayList<>();
        mContext = getApplicationContext();
        mProgressBar = (ProgressBar)findViewById(R.id.layout_comment_progress);
        mRelativeLayout =(RelativeLayout)findViewById(R.id.layout_comment_listviewLayout);


        //setupWidgets();
        try {
            Log.d(TAG, "onCreateView: Bundle");
            mPhoto = getPhotoFromBundle();
            String photo_id = getPhotoFromBundle().getPhoto_id();
            String title = getPhotoFromBundle().getTitle();
            String description =getPhotoFromBundle().getDescription();
            Log.d(TAG, "onCreate: Title = "+title+" Description = "+description);
            Log.d(TAG, "init: gettting photos from bundle" + photo_id + "  " + mPhoto.getComments());
            setupFirebaseAuth();
            setupWidgets();


            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.GONE);
                    mRelativeLayout.setVisibility(View.VISIBLE);
                }
            }, 500);//1000 = one second


        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }


    }

    private void setupWidgets() {

        setupHint();
        adapter = new CommentListAdapter(mContext,
                R.layout.layout_comment, mComments);
        mListView.setAdapter(adapter);

        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mComment.getText().toString().equals("")) {
                    Log.d(TAG, "onClick: attempting to submit new comment.");
                    addNewComment(mComment.getText().toString());

                    mComment.setText("");
                    closeKeyboard();
                } else {
                    Toast.makeText(mContext, "you can't post a blank comment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().popBackStack();
            }
        });
    }



    private void setupHint() {
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
                mComment.setHint("Comment as "+todo.getUsername()+"...");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void closeKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void addNewComment(String newComment) {
        Log.d(TAG, "addNewComment: adding new comment: " + newComment);

        String notifiicationID =myRef.push().getKey();
        String commentID = myRef.push().getKey();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimestamp());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_posts))
                .child(getPhotoFromBundle().getUser_id())
                .child(getPhotoFromBundle().getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

        Map<String, Object> value = new HashMap<>();
        value.put("comment_id", commentID);
        value.put("from",FirebaseAuth.getInstance().getCurrentUser().getUid());
        value.put("time", ServerValue.TIMESTAMP);
        value.put("photo_id",getPhotoFromBundle().getPhoto_id());
        value.put("type","comment");
        value.put("notification_id",notifiicationID);

        myRef.child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_notifications))
                .child(getPhotoFromBundle().getUser_id())
                .child(notifiicationID)
                .setValue(value);
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        return sdf.format(new Date());
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

        mRelativeLayout.setVisibility(View.GONE);


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

        /*if (mComments.size() == 0) {
            mComments.clear();
            Comment firstComment = new Comment();
            firstComment.setComment(mPhoto.getTitle());
            firstComment.setUser_id(mPhoto.getUser_id());
            firstComment.setDate_created(mPhoto.getDate_created());
            mComments.add(firstComment);
            mPhoto.setComments(mComments);
            setupWidgets();
        }*/
        //setupWidgets();
        Query query = myRef
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_posts))
                .child(getPhotoFromBundle().getUser_id())
                .child(getPhotoFromBundle().getPhoto_id())
                .child(getString(R.string.field_comments));

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Comment comment = dataSnapshot.getValue(Comment.class);

                mComments.add(comment);

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