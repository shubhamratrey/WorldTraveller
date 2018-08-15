package com.example.shubh.worldtraveller.Worldtraveller.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.BottomNavigationViewHelper;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.FirebaseMethods;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.GetTimeAgo;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.Star_utils;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.UniversalImageLoader;
import com.example.shubh.worldtraveller.Worldtraveller.models.Comment;
import com.example.shubh.worldtraveller.Worldtraveller.models.Notification;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_Post extends AppCompatActivity {

    private static final String TAG = "Activity_Post";
    private ImageView mImageView;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //widgets
    private Context mContext = Activity_Post.this;
    // private SquareImageView mPostImage;
    private CircleImageView mProfileImage;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mBackLabel, mTitle, mUsername, mDescription, mTimestamp, mCommentNumbers, mStarsNumbers, mStaredBy;
    private ImageView mBackArrow, mEllipses, mStarRed, mStarWhite, mComment, mPostImage;
    private RatingBar mStarRatingBar;
    private Point p;

    private GestureDetector gestureDetector;
    //vars
    private Photo mPhoto;
    private int mActivityNumber = 0;
    private String photoUsername = "";
    private UserAccountSettings mUserAccountSettings;
    private Star stars;
    private String profilePhotoUrl = "";
    private GestureDetector mGestureDetector;
    private Boolean mLikedByCurrentUser, mNotification;
    private StringBuilder mUsers;
    private int mStarCount;
    private String mLikesString = "";
    private UserAccountSettings mCurrentUser;
    private Star_utils mStarUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        widgetsInitialozation();

        setupFirebaseAuth();
        initImageLoader();
        //setupBottomNavigationView();
        Log.d(TAG, "onCreate: star count" + mStarCount);
        init();
       // checkNotification();
    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void widgetsInitialozation() {
        mPostImage = (ImageView) findViewById(R.id.post_image);
        mProfileImage = (CircleImageView) findViewById(R.id.postbottom_profile_photo);
        mUsername = (TextView) findViewById(R.id.textNameView);
        mComment = (ImageView) findViewById(R.id.postbottom_comment);
        mCommentNumbers = (TextView) findViewById(R.id.postbottom_comment_numbers);
        mStarRed = (ImageView) findViewById(R.id.postbottom_star_red);
        mStarWhite = (ImageView) findViewById(R.id.postbottom_star);
        mStarsNumbers = (TextView) findViewById(R.id.postbottom_stars_number);
        mTitle = (TextView) findViewById(R.id.viewpost_title);
        mDescription = (TextView) findViewById(R.id.viewpost_description);
        mTimestamp = (TextView) findViewById(R.id.viewpost_time_posted);
        mEllipses = (ImageView) findViewById(R.id.ivEllipses);
        mBackArrow = (ImageView) findViewById(R.id.backArrow);
        mBackLabel = (TextView) findViewById(R.id.tvBackLabel);
        mStaredBy = (TextView) findViewById(R.id.viewpost_staredby);

        ////////////////////////////////////////////////////////////////////////////////////
        final int[] location = new int[2];
        mStarWhite.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.getLocationOnScreen(location);
                p = new Point();
                p.x = location[0];
                p.y = location[1];
                popStarWindow(v, p);
                return true;
            }
        });

///////////////////////////////////////////////////////////////////////////////////
        final boolean[] showingFirst = {true};

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showingFirst[0] == true) {
                    checkLikeExistence();

                    showingFirst[0] = false;
                } else {
                    checkLikeExistence();

                    showingFirst[0] = true;
                }

            }
        });
    }

    private void popStarWindow(View v, Point p) {
        try {
            LinearLayout viewGroup = v.findViewById(R.id.popup);
            LayoutInflater layoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = layoutInflater.inflate(R.layout.star_popup, viewGroup);
            final Animation blink = AnimationUtils.loadAnimation(this, R.anim.dailog_blink_in);


            layout.startAnimation(blink);

            // Creating the PopupWindow
            final PopupWindow popup = new PopupWindow(mContext);
            popup.setAnimationStyle(R.style.popup_anim);
            popup.setContentView(layout);
            popup.setWidth(400);
            popup.setHeight(100);
            popup.setFocusable(true);
            popup.setTouchable(true);
            popup.setAnimationStyle(R.style.popup_anim);

            int OFFSET_X = 10;
            int OFFSET_Y = -13;

            // Clear the default translucent background
            popup.setBackgroundDrawable(new BitmapDrawable());

            // Displaying the popup at the specified location, + offsets.

            popup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
            /*
            // Displaying the popup at the specified location, + offsets.
            popup.showAtLocation(v, Gravity.CENTER, 0, 0);*/

            RatingBar rb = layout.findViewById(R.id.ratingBar1);
            rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating,
                                            boolean fromUser) {
                    int in = (int) rating;
                    newRating(String.valueOf(in));
                    //Notification();
                    //Toast.makeText(mContext, "You gave " + String.valueOf(in) + " star.", Toast.LENGTH_SHORT).show();
                    popup.setAnimationStyle(R.anim.default_dialog_out);
                    popup.dismiss();

                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void newRating(final String ratings) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_posts))
                .child(getPhotoFromBundle().getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_stars));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Upto here");
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    String keyID = singleSnapshot.getKey();

                    //case1: Then user already liked the photo
                    if (mLikedByCurrentUser &&
                            singleSnapshot.getValue(Star.class).getUser_id()
                                    .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                        Log.d(TAG, "addNewLike: adding new like");

                        Star like = new Star();
                        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        like.setStars(ratings);

                        myRef.child(getString(R.string.dbname_users))
                                .child(getString(R.string.dbname_posts))
                                .child(mPhoto.getUser_id())
                                .child(mPhoto.getPhoto_id())
                                .child(getString(R.string.field_stars))
                                .child(keyID)
                                .setValue(like);

/*
                        String notifiicationID =myRef.push().getKey();

                        Map<String, Object> value = new HashMap<>();
                        value.put("comment_id", "stardoesnotneedone");
                        value.put("from",FirebaseAuth.getInstance().getCurrentUser().getUid());
                        value.put("time", ServerValue.TIMESTAMP);
                        value.put("photo_id",getPhotoFromBundle().getPhoto_id());
                        value.put("type","star");

                        myRef.child(getString(R.string.dbname_users))
                                .child(getString(R.string.dbname_notifications))
                                .child(getPhotoFromBundle().getUser_id())
                                .child(notifiicationID)
                                .setValue(value);*/
                        //mStarUtils.toggleLike();
                        getLikesString();
                    }
                    //case2: The user has not liked the photo
                    else if (!mLikedByCurrentUser) {
                        //add new like
                        //   addNewLike();
                        String newLikeID = myRef.push().getKey();
                        Star like = new Star();
                        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        like.setStars(ratings);

                        myRef.child(getString(R.string.dbname_users))
                                .child(getString(R.string.dbname_posts))
                                .child(mPhoto.getUser_id())
                                .child(mPhoto.getPhoto_id())
                                .child(getString(R.string.field_stars))
                                .child(newLikeID)
                                .setValue(like);

                     /*   String notifiicationID =myRef.push().getKey();

                        Map<String, Object> value = new HashMap<>();
                        value.put("comment_id", "stardoesnotneedone");
                        value.put("from",FirebaseAuth.getInstance().getCurrentUser().getUid());
                        value.put("time", ServerValue.TIMESTAMP);
                        value.put("photo_id",getPhotoFromBundle().getPhoto_id());
                        value.put("type","star");

                        myRef.child(getString(R.string.dbname_users))
                                .child(getString(R.string.dbname_notifications))
                                .child(getPhotoFromBundle().getUser_id())
                                .child(notifiicationID)
                                .setValue(value);*/
                        //mStarUtils.toggleLike();
                        getLikesString();


                        break;
                    }
                }
                if (!dataSnapshot.exists()) {
                    //add new like
                    //   Toast.makeText(mContext, "no data exist", Toast.LENGTH_SHORT).show();
                    String newLikeID = myRef.push().getKey();
                    Star like = new Star();
                    like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    like.setStars(ratings);

                    myRef.child(getString(R.string.dbname_users))
                            .child(getString(R.string.dbname_posts))
                            .child(mPhoto.getUser_id())
                            .child(mPhoto.getPhoto_id())
                            .child(getString(R.string.field_stars))
                            .child(newLikeID)
                            .setValue(like);

                   /* String notifiicationID =myRef.push().getKey();

                    Map<String, Object> value = new HashMap<>();
                    value.put("comment_id", "stardoesnotneedone");
                    value.put("from",FirebaseAuth.getInstance().getCurrentUser().getUid());
                    value.put("time", ServerValue.TIMESTAMP);
                    value.put("photo_id",getPhotoFromBundle().getPhoto_id());
                    value.put("type","star");

                    myRef.child(getString(R.string.dbname_users))
                            .child(getString(R.string.dbname_notifications))
                            .child(getPhotoFromBundle().getUser_id())
                            .child(notifiicationID)
                            .setValue(value);*/

                    //mStarUtils.toggleLike();
                    getLikesString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void checkNotification() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_notifications))
                .child(getPhotoFromBundle().getUser_id())
                .orderByChild("type").equalTo("star");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    String ServerPhoto_id = singleSnapshot.getValue(Notification.class).getPhoto_id();
                    String bundlePhoto_id = getPhotoFromBundle().getPhoto_id();

                    if (ServerPhoto_id.equals(bundlePhoto_id)) {
                        mNotification = true;
                    } else {
                        mNotification = false;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void Notification() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_notifications))
                .child(getPhotoFromBundle().getUser_id())
                .orderByChild("type").equalTo("star");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    String ServerPhoto_id = singleSnapshot.getValue(Notification.class).getPhoto_id();
                    String bundlePhoto_id = getPhotoFromBundle().getPhoto_id();

                    if (mNotification && ServerPhoto_id.equals(bundlePhoto_id)) {

                        String notificationID1 = singleSnapshot.getValue(Notification.class).getNotification_id();

                        Log.d(TAG, "onDataChange: for existing " + notificationID1);
                        Map<String, Object> value = new HashMap<>();
                        value.put("comment_id", "stardoesnotneedone");
                        value.put("from", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        value.put("time", ServerValue.TIMESTAMP);
                        value.put("photo_id", getPhotoFromBundle().getPhoto_id());
                        value.put("type", "star");
                        value.put("notification_id", notificationID1);

                        String user_id = getPhotoFromBundle().getUser_id();
                        myRef.child(getString(R.string.dbname_users))
                                .child(getString(R.string.dbname_notifications))
                                .child(user_id)
                                .child(notificationID1)
                                .setValue(value);

                        checkNotification();
                    } else if (!mNotification) {
                        String notifiicationID = myRef.push().getKey();

                        Log.d(TAG, "onDataChange: for new else" + notifiicationID);
                        Map<String, Object> value = new HashMap<>();
                        value.put("comment_id", "stardoesnotneedone");
                        value.put("from", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        value.put("time", ServerValue.TIMESTAMP);
                        value.put("photo_id", getPhotoFromBundle().getPhoto_id());
                        value.put("type", "star");
                        value.put("notification_id", notifiicationID);

                        String user_id = getPhotoFromBundle().getUser_id();
                        myRef.child(getString(R.string.dbname_users))
                                .child(getString(R.string.dbname_notifications))
                                .child(user_id)
                                .child(notifiicationID)
                                .setValue(value);

                    }break;
                }
                if (!dataSnapshot.exists()) {
                    String notifiicationID = myRef.push().getKey();

                    Log.d(TAG, "onDataChange: for new data not exist" + notifiicationID);
                    Map<String, Object> value = new HashMap<>();
                    value.put("comment_id", "stardoesnotneedone");
                    value.put("from", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    value.put("time", ServerValue.TIMESTAMP);
                    value.put("photo_id", getPhotoFromBundle().getPhoto_id());
                    value.put("type", "star");
                    value.put("notification_id", notifiicationID);

                    String user_id = getPhotoFromBundle().getUser_id();
                    myRef.child(getString(R.string.dbname_users))
                            .child(getString(R.string.dbname_notifications))
                            .child(user_id)
                            .child(notifiicationID)
                            .setValue(value);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void checkLikeExistence() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_posts))
                .child(getPhotoFromBundle().getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_stars));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Upto here");
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                    String keyID = singleSnapshot.getKey();

                    //case1: Then user already liked the photo
                    if (mLikedByCurrentUser &&
                            singleSnapshot.getValue(Star.class).getUser_id()
                                    .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                        myRef.child(getString(R.string.dbname_users))
                                .child(getString(R.string.dbname_posts))
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(mPhoto.getPhoto_id())
                                .child(getString(R.string.field_stars))
                                .child(keyID)
                                .removeValue();

                        // mHeart.toggleLike();
                        getLikesString();
                    }
                    //case2: The user has not liked the photo
                    else if (!mLikedByCurrentUser) {
                        //add new like
                        addNewLike();
                        break;
                    }
                }
                if (!dataSnapshot.exists()) {
                    //add new like
                    addNewLike();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void init() {
        try {
            mPhoto = getPhotoFromBundle();

            /*
            int imageHeight = getResources().getDisplayMetrics().heightPixels;
            int imageWidth = getResources().getDisplayMetrics().widthPixels;
            Log.d(TAG, "init: "+imageHeight+"  "+imageWidth);

            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(imageWidth,imageHeight/2);
            mPostImage.setLayoutParams(parms);
*/
            UniversalImageLoader.setImage(getPhotoFromBundle().getImage_path(), mPostImage, null, "");
            mTitle.setText(getPhotoFromBundle().getTitle());
            mDescription.setText(getPhotoFromBundle().getDescription());
            GetTimeAgo getTimeAgo = new GetTimeAgo();
            long lastTime = Long.parseLong(getPhotoFromBundle().getDate_created()); //String online = dataSnapshot.child("online").getValue().toString();
            String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
            mTimestamp.setText(lastSeenTime);


            mActivityNumber = getActivityNumFromBundle();
            String photo_id = getPhotoFromBundle().getPhoto_id();

            Log.d(TAG, "init: PhotoID" + photo_id + "   " + mPhoto.getComments() + "      " + mPhoto.getUser_id());

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_users))
                    .child(getString(R.string.dbname_posts))
                    .child(getPhotoFromBundle().getUser_id())
                    .orderByChild(getString(R.string.field_photo_id))
                    .equalTo(photo_id);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {

                        Photo newPhoto = new Photo();
                        Log.d(TAG, "onDataChange: what " + singleSnapshot + "   Photos" + newPhoto);
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        try {
                            newPhoto.setTitle(objectMap.get(getString(R.string.field_title)).toString());
                            newPhoto.setDescription(objectMap.get(getString(R.string.field_description)).toString());
                            newPhoto.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                            newPhoto.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                            newPhoto.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                            newPhoto.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                            newPhoto.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                            List<Comment> commentsList = new ArrayList<Comment>();
                            for (DataSnapshot dSnapshot : singleSnapshot
                                    .child(getString(R.string.field_comments)).getChildren()) {
                                Comment comment = new Comment();
                                comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                                commentsList.add(comment);
                            }
                            newPhoto.setComments(commentsList);

                            //Log.d(TAG, "onDataChange: newPhoto comment"+newPhoto.getComments());
                            mPhoto = newPhoto;

                            getCurrentUser();
                            getPhotoDetails();
                            Log.d(TAG, "onDataChange: uptohere" + mPhoto.getComments().size());

                            //getLikesString();
                        } catch (NullPointerException e) {
                            Log.e(TAG, "onDataChange: NullPointerException: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: query cancelled.");
                }
            });

        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }


    }

    private void getLikesString() {
        // Log.d(TAG, "getLikesString: getting likes string");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_posts))
                .child(getPhotoFromBundle().getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_stars));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsers = new StringBuilder();
                mStarCount = 0;
                for (final DataSnapshot PhotoSnapShot : dataSnapshot.getChildren()) {

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(getString(R.string.dbname_users))
                            .child(getString(R.string.dbname_profile))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(PhotoSnapShot.getValue(Star.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                String number = PhotoSnapShot.getValue(Star.class).getStars();
                                int result = Integer.parseInt(number);
                                mStarCount = mStarCount + result;
                                //   Log.d(TAG, "onDataChange: found stared: " +singleSnapshot.getValue(UserAccountSettings.class).getUsername() + " = " + PhotoSnapShot.getValue(Star.class).getStars());

                                mUsers.append(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                                mUsers.append(",");
                            }

                            String[] splitUsers = mUsers.toString().split(",");

                            if (mUsers.toString().contains(mCurrentUser.getUsername() + ",")) {//mitch, mitchell.tabian
                                mLikedByCurrentUser = true;
//                                mNotification = true;
                            } else {
                                mLikedByCurrentUser = false;
                                //  mNotification= false;
                            }

                            int length = splitUsers.length;
                            if (length == 1) {
                                mLikesString = "Rated by " + splitUsers[0];
                            } else if (length == 2) {
                                mLikesString = "Rated by " + splitUsers[0]
                                        + " and " + splitUsers[1];
                            } else if (length == 3) {
                                mLikesString = "Rated by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + " and " + splitUsers[2];

                            } else if (length == 4) {
                                mLikesString = "Rated by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + splitUsers[3];
                            } else if (length > 4) {
                                mLikesString = "Rated by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + (splitUsers.length - 3) + " others";
                            }
                            //  Log.d(TAG, "onDataChange: likes string: " + mLikesString);
                            setupWidgets();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                if (!dataSnapshot.exists()) {
                    mLikesString = "";
                    mLikedByCurrentUser = false;
                    // mNotification = true;
                    setupWidgets();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getCurrentUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_profile))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mCurrentUser = singleSnapshot.getValue(UserAccountSettings.class);
                    //Log.d(TAG, "onDataChange: Current user" + singleSnapshot);
                }
                getLikesString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private void addNewLike() {
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = myRef.push().getKey();
        Star like = new Star();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        like.setStars("5");

        myRef.child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_posts))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_stars))
                .child(newLikeID)
                .setValue(like);


        //mStarUtils.toggleLike();
        getLikesString();
    }

    private void getPhotoDetails() {
        Log.d(TAG, "getPhotoDetails: retrieving photo details." + getPhotoFromBundle().getUser_id());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(getString(R.string.dbname_users))
                .child(getString(R.string.dbname_profile))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(getPhotoFromBundle().getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                    UniversalImageLoader.setImage(mUserAccountSettings.getProfile_photo(), mProfileImage, null, "");
                    mUsername.setText(mUserAccountSettings.getDisplay_name());
                }
                //Log.d(TAG, "onDataChange: " + mUserAccountSettings.getDisplay_name());
                //setupWidgets();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });

    }

    private void setupWidgets() {
        /*String timestampDiff = getTimestampDifference();
        if (!timestampDiff.equals("0")) {
            mTimestamp.setText(timestampDiff + " DAYS AGO");
        } else {
            mTimestamp.setText("TODAY");
        }*/

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Log.d(TAG, "onClick: navigating back");
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                getSupportFragmentManager().popBackStack();
            }
        });

        if (mLikedByCurrentUser) {
            mStarWhite.setVisibility(View.GONE);
            mStarRed.setVisibility(View.VISIBLE);

            final int[] locationScreen = new int[2];
            mStarRed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //checkLikeExistence();
                    v.getLocationOnScreen(locationScreen);
                    p = new Point();
                    p.x = locationScreen[0];
                    p.y = locationScreen[1];

                    popStarWindow(v, p);
                    Log.d(TAG, "onClick: red");
                }
            });

        } else {
            mStarWhite.setVisibility(View.VISIBLE);
            mStarRed.setVisibility(View.GONE);
            final int[] locationScreen = new int[2];
            mStarWhite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //checkLikeExistence();
                    v.getLocationOnScreen(locationScreen);
                    p = new Point();
                    p.x = locationScreen[0];
                    p.y = locationScreen[1];

                    popStarWindow(v, p);
                    Log.d(TAG, "onClick: white");
                }
            });
        }

        //   Log.d(TAG, "setupWidgets: count" + mStarCount + " no of comments " + mPhoto.getComments().size());
        mCommentNumbers.setText(String.valueOf(mPhoto.getComments().size()));
        mStarsNumbers.setText(String.valueOf(mStarCount));

        if (mStarCount > 0) {
            mStaredBy.setVisibility(View.VISIBLE);
        } else {
            mStaredBy.setVisibility(View.GONE);
        }
        mStaredBy.setText(mLikesString);
        mStaredBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent star = new Intent(Activity_Post.this, Activity_Likes.class);
                star.putExtra(getString(R.string.photo), getPhotoFromBundle());
                startActivity(star);

            }
        });
        //     Log.d(TAG, "setupWidgets: comments" + mPhoto.getComments().size());

       /* if(mPhoto.getComments().size() > 0){
            mComments.setText("View all " + mPhoto.getComments().size() + " comments");
        }else{
            mComments.setText("");
        }*/

        /*mComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to comments thread");

                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);

            }
        });*/


        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                Intent intent = new Intent(Activity_Post.this, Activity_Comment.class);
                intent.putExtra(getString(R.string.photo), getPhotoFromBundle());
                startActivity(intent);
            }
        });
    }

    private int getActivityNumFromBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            return bundle.getInt(getString(R.string.activity_number));
        } else {
            return 0;
        }
    }

    private Photo getPhotoFromBundle() {

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            return extras.getParcelable(getString(R.string.photo));
        } else {
            return null;
        }
    }


    /**
     * BottomNavigationView setup
     */

    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation_home);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
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
