package com.example.shubh.worldtraveller.Worldtraveller.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.GetTimeMessaged;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.MyLeadingMarginSpan;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.UniversalImageLoader;
import com.example.shubh.worldtraveller.Worldtraveller.models.Notification;
import com.example.shubh.worldtraveller.Worldtraveller.models.Star;
import com.example.shubh.worldtraveller.Worldtraveller.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {


    private static final String TAG = "NotificatonAdapter";
    private List<Notification> mMessageList;
    private List<String> mUserPhoto;
    private String mUserID;
    private Context mContext;
    private String likeRandom, likePhotoRandom;

    private String mProfilePhoto;
    private String name;
    private StringBuilder mUsername;
    private StringBuilder mUserProfilePhoto;
    private String user_id, photo_id, comment_id;
    String mClickedUrl;
    String mUrlType;
    private Notification notification;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private String singlePhoto = " ";
    private String firstphoto = " ";
    private String secondphoto = " ";
    private String mLikesString = "";
    private int mStarCount;
    private int count;
    private static final int NOTIFICATION_CONNECTION_LAYOUT = 0, NOTIFICATION_STAR_LAYOUT = 1, NOTIFICATION_COMMENT_LAYOUT = 3;

    public OnProfilePhotoClickListener mProfilePhotoClickListener;
    public OnViewClickListener mViewClickListener;

    public NotificationAdapter(Context context, List<Notification> messagesList) {
        Log.d(TAG, "NotificationAdapter: " + messagesList.size());
        mContext = context;
        this.mMessageList = messagesList;
    }

    @Override
    public int getItemViewType(int position) {
        Notification item = mMessageList.get(position);
        String type = item.getType();
        switch (type) {
            case "connection":
                return NOTIFICATION_CONNECTION_LAYOUT;
            case "star":
                return NOTIFICATION_STAR_LAYOUT;
            case "comment":
                return NOTIFICATION_COMMENT_LAYOUT;
        }
        return position;
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        /*switch (viewType) {
            case NOTIFICATION_CONNECTION_LAYOUT:
                return new NotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notification_connection, parent, false));
            case NOTIFICATION_STAR_LAYOUT:
                return new NotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notification_likes, parent, false));
            case NOTIFICATION_OTHER_LAYOUT:
                return new NotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notification_comment, parent, false));
        }*/

        if (viewType == NOTIFICATION_CONNECTION_LAYOUT) {
            return new NotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notification_connection, parent, false));
        } else if (viewType == NOTIFICATION_STAR_LAYOUT) {
            return new NotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notification_likes, parent, false));
        } else
            return new NotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notification_comment, parent, false));
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public CircleImageView profileImageSingle, profileImageOne, profileImageSecond;
        public TextView username, middleSection, date;
        public ImageView notificationPhoto;
        // public RelativeLayout relativeLayout;

        public NotificationViewHolder(View view) {
            super(view);

            profileImageSingle = (CircleImageView) view.findViewById(R.id.notification_profile_single);
            profileImageOne = (CircleImageView) view.findViewById(R.id.notification_profile_one);
            profileImageSecond = (CircleImageView) view.findViewById(R.id.notification_profile_two);
            username = (TextView) view.findViewById(R.id.notification_username);
            middleSection = (TextView) view.findViewById(R.id.notification_type_middle);
            date = (TextView) view.findViewById(R.id.notification_time);
            //relativeLayout = (RelativeLayout) view.findViewById(R.id.notification_relLayout2);
            notificationPhoto = (ImageView) view.findViewById(R.id.notification_photo);
            //messageImage.setOnClickListener(this);
            // profileImageSingle.setOnClickListener(this);
            // relativeLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getLayoutPosition();

            mClickedUrl = mMessageList.get(position).getFrom();
            mUrlType = mMessageList.get(position).getType();

            if (mUrlType.equals("comment")) {
                mProfilePhotoClickListener.onProfilePhotoClick(v, getAdapterPosition(), mClickedUrl); //OnItemClickListener commenetPhoto;
            } else if (mUrlType.equals("tag")) {
                mProfilePhotoClickListener.onProfilePhotoClick(v, getAdapterPosition(), mClickedUrl); //OnItemClickListener tagPhoto;
            } else if (mUrlType.equals("mention")) {
                mProfilePhotoClickListener.onProfilePhotoClick(v, getAdapterPosition(), mClickedUrl); //OnItemClickListener mItemClickListener;
            }

            //  mViewClickListener.onViewClick(v, getAdapterPosition(), mClickedUrl); //OnItemClickListener mItemClickListener;
        }
    }


    public interface OnProfilePhotoClickListener {
        public void onProfilePhotoClick(View view, int position, String id);
    }

    public void SetOnItemOnNotificationProfilePhoto(final OnProfilePhotoClickListener mProfilePhotoClickListener) {
        this.mProfilePhotoClickListener = mProfilePhotoClickListener;
    }

    public interface OnViewClickListener {
        public void onViewClick(View view, int position, String id);
    }

    public void SetOnItemOnView(final OnViewClickListener mViewClickListener) {
        this.mViewClickListener = mViewClickListener;
    }

    @Override
    public void onBindViewHolder(final NotificationViewHolder viewHolder, int i) {

        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        notification = mMessageList.get(i);
        user_id = notification.getFrom();
        photo_id = notification.getPhoto_id();
        comment_id = notification.getComment_id();
        String notification_type = notification.getType();


        // Log.d(TAG, "onBindViewHolder: ");

        GetTimeMessaged getTimeAgo = new GetTimeMessaged();
        final String lastSeenTime = getTimeAgo.getTimeMessaged(notification.getTime(), mContext);
        viewHolder.date.setText(lastSeenTime);

        if (notification_type.equals("comment")) {
            viewHolder.profileImageSingle.setVisibility(View.VISIBLE);
            viewHolder.profileImageOne.setVisibility(View.GONE);
            viewHolder.profileImageSecond.setVisibility(View.GONE);
            viewHolder.username.setVisibility(View.VISIBLE);
            viewHolder.middleSection.setVisibility(View.VISIBLE);
            viewHolder.date.setVisibility(View.VISIBLE);
            viewHolder.notificationPhoto.setVisibility(View.VISIBLE);


            DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
            mRootRef.child("users")
                    .child("profile")
                    .child(user_id)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            name = dataSnapshot.child("username").getValue().toString();
                            mProfilePhoto = dataSnapshot.child("profile_photo").getValue().toString();
                            UniversalImageLoader.setImage(mProfilePhoto, viewHolder.profileImageSingle, null, "");


                        }


                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            Query notificationPhotoQuery = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child("posts")
                    .child(mCurrent_user_id)
                    .child(photo_id);

            notificationPhotoQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String image_path = dataSnapshot.child("image_path").getValue().toString();
                    UniversalImageLoader.setImage(image_path, viewHolder.notificationPhoto, null, "");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            Query commentQuery = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child("posts")
                    .child(mCurrent_user_id)
                    .child(photo_id)
                    .child("comments")
                    .child(comment_id);

            commentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    try{
                        viewHolder.username.setText(name);
                        viewHolder.username.measure(0, 0);
                        int leftMargin = viewHolder.username.getMeasuredWidth() + 6;
                        //Log.d(TAG, "onDataChange: leftmargin" + leftMargin);
                        String comment = "commented: "
                                + dataSnapshot.child("comment").getValue().toString();
                        SpannableString ss = new SpannableString(comment);
                        ss.setSpan(new MyLeadingMarginSpan(1, leftMargin), 0, ss.length(), 0);

                        viewHolder.middleSection.setText(ss);

                    }catch (NullPointerException e){
                        Log.d(TAG, "onDataChange: "+e);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else if (notification_type.equals("star")) {

            viewHolder.username.setVisibility(View.VISIBLE);
            viewHolder.middleSection.setVisibility(View.GONE);
            viewHolder.date.setVisibility(View.GONE);
            viewHolder.notificationPhoto.setVisibility(View.VISIBLE);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child("users")
                    .child("posts")
                    .child(mCurrent_user_id)
                    .child(photo_id)
                    .child("stars");

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUsername = new StringBuilder();
                    mUserProfilePhoto = new StringBuilder();
                    for (final DataSnapshot PhotoSnapShot : dataSnapshot.getChildren()) {

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        Query query = reference
                                .child("users")
                                .child("profile")
                                .orderByChild("user_id")
                                .equalTo(PhotoSnapShot.getValue(Star.class).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    mUsername.append(singleSnapshot.getValue(UserAccountSettings.class).getUsername());
                                    mUsername.append(",");

                                    mUserProfilePhoto.append(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo());
                                    mUserProfilePhoto.append(",");

                                }
                                String[] splitImage_path = mUserProfilePhoto.toString().split(",");
                                String[] splitUsers = mUsername.toString().split(",");
                                int lengthProfilePhoto = splitImage_path.length;
                                int length = splitUsers.length;

                                if (length == 1) {
                                    viewHolder.profileImageSingle.setVisibility(View.VISIBLE);
                                    viewHolder.profileImageOne.setVisibility(View.GONE);
                                    viewHolder.profileImageSecond.setVisibility(View.GONE);

                                    mLikesString ="<b>"+ splitUsers[0]+"</b>"
                                            + " has given you a star. "
                                            +"<font color='#BDBDBD'>"+lastSeenTime+"</font>";
                                    singlePhoto = splitImage_path[0];

                                } else if (length == 2) {

                                    viewHolder.profileImageSingle.setVisibility(View.GONE);
                                    viewHolder.profileImageOne.setVisibility(View.VISIBLE);
                                    viewHolder.profileImageSecond.setVisibility(View.VISIBLE);
                                    firstphoto = splitImage_path[1];
                                    secondphoto = splitImage_path[0];
                                    //mUserProfilePhoto.setLength(0);

                                    mLikesString = "<b>"+ splitUsers[0]+"</b>" + " and "
                                            +"<b>"+ splitUsers[1]+"</b>"
                                            +" have given you stars. "
                                            +"<font color='#BDBDBD'>"+lastSeenTime+"</font>";

                                } else if (length == 3) {
                                    viewHolder.profileImageSingle.setVisibility(View.GONE);
                                    viewHolder.profileImageOne.setVisibility(View.VISIBLE);
                                    viewHolder.profileImageSecond.setVisibility(View.VISIBLE);
                                    firstphoto = splitImage_path[1];
                                    secondphoto = splitImage_path[0];
                                    //mUserProfilePhoto.setLength(0);
                                    mLikesString = "<b>"+splitUsers[0]+"</b>" + ", "
                                            + "<b>"+splitUsers[1]+"</b>" + " and "
                                            + "<b>"+splitUsers[2]+"</b>"
                                            +" have given you stars. "
                                            +"<font color='#BDBDBD'>"+lastSeenTime+"</font>";

                                } else if (length > 3) {
                                    viewHolder.profileImageSingle.setVisibility(View.GONE);
                                    viewHolder.profileImageOne.setVisibility(View.VISIBLE);
                                    viewHolder.profileImageSecond.setVisibility(View.VISIBLE);
                                    firstphoto = splitImage_path[1];
                                    secondphoto = splitImage_path[0];

                                    String other = String.valueOf(length - 3);
                                    mLikesString = "<b>"+splitUsers[0]+"</b>"
                                            + ", " + "<b>"+splitUsers[1]+"</b>" + ", "
                                            + "<b>"+splitUsers[2] +"</b>"+ " and "+"<b>"
                                            + other + " others</b> have given you stars. "
                                            +"<font color='#BDBDBD'>"+lastSeenTime+"</font>";
                                    // Log.d(TAG, "onDataChange: split =3");
                                    mUsername.setLength(0);
                                    mUserProfilePhoto.setLength(0);
                                }

                                viewHolder.date.setText(lastSeenTime);
                                viewHolder.username.setText(Html.fromHtml(mLikesString));
                                /*viewHolder.username.measure(0, 0);
                                int leftMargin = viewHolder.username.getMeasuredWidth() + 6;
                                String comment = "have given you stars.";
                                SpannableString ss = new SpannableString(comment);
                                ss.setSpan(new MyLeadingMarginSpan(1, leftMargin), 0, ss.length(), 0);
                                viewHolder.middleSection.setText(comment);*/
                                //Log.d(TAG, "onDataChange: "+mLikesString);

                                UniversalImageLoader.setImage(singlePhoto, viewHolder.profileImageSingle, null, "");
                                UniversalImageLoader.setImage(firstphoto, viewHolder.profileImageOne, null, "");
                                UniversalImageLoader.setImage(secondphoto, viewHolder.profileImageSecond, null, "");



                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            Query notificationPhotoQuery = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child("posts")
                    .child(mCurrent_user_id)
                    .child(photo_id);

            notificationPhotoQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    String image_path = dataSnapshot.child("image_path").getValue().toString();
                    UniversalImageLoader.setImage(image_path, viewHolder.notificationPhoto, null, "");


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mMessageList == null ? 0 : mMessageList.size();
    }

}
