package com.progremastudio.emergencymedicalteam.viewholder;

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

public class PostViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "post-view-holder";

    private TextView mNameField;
    private TextView mMessageField;
    private TextView mAddressField;
    private TextView mTimestampField;
    private TextView mEmergencyTypeField;
    private ImageView mPictureField;

    PostViewHolder(View itemView) {
        super(itemView);

        mNameField = (TextView) itemView.findViewById(R.id.display_name_field);
        mMessageField = (TextView) itemView.findViewById(R.id.message_field);
        mAddressField = (TextView) itemView.findViewById(R.id.address_field);
        mTimestampField = (TextView) itemView.findViewById(R.id.timestamp_field);
        mEmergencyTypeField = (TextView) itemView.findViewById(R.id.emergency_type_field);
        mPictureField = (ImageView) itemView.findViewById(R.id.picture_field);
    }

    public void bindToPost(Context context, Post post, View.OnClickListener clickListener) {

        mNameField.setText(post.displayName);
        mMessageField.setText(post.message);
        mAddressField.setText(post.locationCoordinate);
        mTimestampField.setText("- " + DateUtils.getRelativeTimeSpanString(Long.parseLong(post.timestamp)));
        mEmergencyTypeField.setText(post.emergencyType);
        mPictureField.setVisibility(View.GONE);

        /**
         * Show picture if exist
         */
        if (!post.pictureUrl.equals("No Picture")) {
            mPictureField.setVisibility(View.VISIBLE);
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(post.pictureUrl);
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .into(mPictureField);
        }

    }

}
