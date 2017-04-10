package com.progremastudio.emergencymedicalteam.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.Post;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "post-view-holder";

    private TextView mNameField;

    private TextView mMessageField;

    private TextView mAddressField;

    private TextView mTimestampField;

    private ImageView mAccidentPictureField;

    private CircleImageView mProfilePictureField;

    public PostViewHolder(View itemView) {

        super(itemView);

        mNameField = (TextView) itemView.findViewById(R.id.other_display_name_field);
        mMessageField = (TextView) itemView.findViewById(R.id.other_message_field);
        mAddressField = (TextView) itemView.findViewById(R.id.address_field);
        mTimestampField = (TextView) itemView.findViewById(R.id.other_timestamp_field);
        mAccidentPictureField = (ImageView) itemView.findViewById(R.id.accident_picture_field);
        mProfilePictureField = (CircleImageView) itemView.findViewById(R.id.profile_picture_field);
    }

    @SuppressLint("SetTextI18n")
    public void bindToPost(Context context, Post post, View.OnClickListener clickListener) {

        /*
        Show user display name
         */
        mNameField.setText(post.displayName);

        /*
        Show user post message
         */
        mMessageField.setText(post.message);

        /*
        Show user location address
         */
        mAddressField.setText(post.locationCoordinate);

        /*
        Show relative time span
         */
        mTimestampField.setText("- " + DateUtils.getRelativeTimeSpanString(Long.parseLong(post.timestamp)));

        /*
        Show profile picture if exist
         */
        if (!post.profileUrl.equals("No Picture")) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(post.profileUrl);
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .into(mProfilePictureField);
        }

        /*
        Show accident picture if exist
         */
        if (!post.pictureUrl.equals("No Picture")) {
            mAccidentPictureField.setVisibility(View.VISIBLE);
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(post.pictureUrl);
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .into(mAccidentPictureField);
        } else {
            mAccidentPictureField.setVisibility(View.GONE);
        }

    }

}
