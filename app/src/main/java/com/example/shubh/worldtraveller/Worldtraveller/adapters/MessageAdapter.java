package com.example.shubh.worldtraveller.Worldtraveller.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.UniversalImageLoader;
import com.example.shubh.worldtraveller.Worldtraveller.models.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by AkshayeJH on 24/07/17.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {


    private static final String TAG = "message adapter";
    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mRootRef;
    private String mUserID;
    private String mProfilePhoto;
    private String from_user;
    String mClickedUrl;
    String mUrlType;
    Messages c;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private static final int MY_MESSAGE = 0, OTHER_MESSAGE = 1;
    OnItemClickListener mItemClickListener;

    public MessageAdapter(List<Messages> messagesList) {

        this.mMessageList = messagesList;

    }

    @Override
    public int getItemViewType(int position) {
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        Messages item = mMessageList.get(position);
        String yo = item.getFrom();

        Log.d(TAG, "getItemViewType: " + yo);
        if (mCurrent_user_id.equals(yo))
            return MY_MESSAGE;
        else
            return OTHER_MESSAGE;
        //return super.getItemViewType(position);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == MY_MESSAGE) {
            return new MessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_message_sent, parent, false));
        } else {
            return new MessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false));

        }
      /*  View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
*/
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public ImageView messageImage;

        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            // displayName = (TextView) view.findViewById(R.id.name_text_layout);
            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);

            messageImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position =getLayoutPosition();

            mClickedUrl=mMessageList.get(position).getMessage();
            mUrlType=mMessageList.get(position).getType();

            if (mUrlType.equals("image")){
                mItemClickListener.onItemClick(v, getAdapterPosition(), mClickedUrl); //OnItemClickListener mItemClickListener;
            }

        }
    }



    public interface OnItemClickListener {
        public void onItemClick(View view, int position, String id);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        c = mMessageList.get(i);

        from_user = c.getFrom();
        String message_type = c.getType();


        Log.d(TAG, "onBindViewHolder: " + from_user);
        mRootRef = FirebaseDatabase.getInstance().getReference().child("users");
        mRootRef.child("profile").child(from_user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // String name = dataSnapshot.child("username").getValue().toString();
                //String image = dataSnapshot.child("image").getValue().toString();
                mUserID = dataSnapshot.child("user_id").getValue().toString();
                mProfilePhoto = dataSnapshot.child("profile_photo").getValue().toString();

                //  viewHolder.displayName.setText(name);

                UniversalImageLoader.setImage(mProfilePhoto, viewHolder.profileImage, null, "");


            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (message_type.equals("text")) {
            viewHolder.messageText.setVisibility(View.VISIBLE);
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageImage.setVisibility(View.GONE);


        } else {

            viewHolder.messageText.setVisibility(View.GONE);
            viewHolder.messageImage.setVisibility(View.VISIBLE);
            UniversalImageLoader.setImage(c.getMessage(), viewHolder.messageImage, null, "");
        }

    }

    @Override
    public int getItemCount() {
        return mMessageList == null ? 0 : mMessageList.size();
    }

}
