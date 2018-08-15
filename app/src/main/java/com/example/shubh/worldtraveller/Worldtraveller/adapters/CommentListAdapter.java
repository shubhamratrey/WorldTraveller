package com.example.shubh.worldtraveller.Worldtraveller.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shubh.worldtraveller.R;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.GetTimeAgo;
import com.example.shubh.worldtraveller.Worldtraveller.Utils.MyLeadingMarginSpan;
import com.example.shubh.worldtraveller.Worldtraveller.models.Comment;
import com.example.shubh.worldtraveller.Worldtraveller.models.UserAccountSettings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by User on 8/22/2017.
 */

public class CommentListAdapter extends ArrayAdapter<Comment> {

    private static final String TAG = "CommentListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;

    public CommentListAdapter(@NonNull Context context, @LayoutRes int resource,
                              @NonNull List<Comment> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
    }

    private static class ViewHolder {
        TextView comment, username, timestamp, reply, likes;
        CircleImageView profileImage;
        ImageView like;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.comment = (TextView) convertView.findViewById(R.id.comment);
            holder.username = (TextView) convertView.findViewById(R.id.comment_username);
            holder.timestamp = (TextView) convertView.findViewById(R.id.comment_time_posted);
            holder.profileImage = (CircleImageView) convertView.findViewById(R.id.comment_profile_image);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //set the comment
        //holder.comment.setText(getItem(position).getComment());

        String timestampDifference = getTimestampDifference(getItem(position));
        if (!timestampDifference.equals("0")) {
            holder.timestamp.setText(timestampDifference + " d");
        } else {
            holder.timestamp.setText("today");
        }

        //set the username and profile image
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_users))
                .child(mContext.getString(R.string.dbname_profile))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    holder.username.setText(
                            singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    holder.username.measure(0,0);
                    int leftMargin = holder.username.getMeasuredWidth()+10;
                    Log.d(TAG, "onDataChange: leftmargin"+leftMargin);

                    String comment = getItem(position).getComment();
                    SpannableString ss = new SpannableString(comment);
                    ss.setSpan(new MyLeadingMarginSpan(1,leftMargin),0,ss.length(),0);

                    holder.comment.setText(ss);

                    /*// Get the icon and its width
		Drawable dIcon = getResources().getDrawable(R.drawable.icon);
		int leftMargin = dIcon.getIntrinsicWidth() + 10;

		// Set the icon in R.id.icon
		ImageView icon = (ImageView) findViewById(R.id.icon);
		icon.setBackgroundDrawable(dIcon);

		SpannableString ss = new SpannableString(text);
		// Expose the indent for the first three rows
		ss.setSpan(new MyLeadingMarginSpan2(3, leftMargin), 0, ss.length(), 0);

		TextView messageView = (TextView) findViewById(R.id.message_view);
		messageView.setText(ss);*/

                    ImageLoader imageLoader = ImageLoader.getInstance();

                    imageLoader.displayImage(
                            singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });

        return convertView;
    }

    /**
     * Returns a string representing the number of days ago the post was made
     *
     * @return
     */
    private String getTimestampDifference(Comment comment) {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = comment.getDate_created();
        try {
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24)));
            // difference = photoTimestamp;
        } catch (ParseException e) {
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage());
            difference = "0";
        }
        return difference;
    }
}






























