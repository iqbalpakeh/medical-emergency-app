package com.progremastudio.emergencymedicalteam.viewholder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
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

    private ImageView mPictureField;

    public PostViewHolder(View itemView) {

        super(itemView);

        mNameField = (TextView) itemView.findViewById(R.id.display_name_field);
        mMessageField = (TextView) itemView.findViewById(R.id.message_field);
        mAddressField = (TextView) itemView.findViewById(R.id.address_field);
        mTimestampField = (TextView) itemView.findViewById(R.id.timestamp_field);
        mPictureField = (ImageView) itemView.findViewById(R.id.picture_field);
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
        Show picture if exist
         */
        if (!post.pictureUrl.equals("No Picture")) {
            mPictureField.setVisibility(View.VISIBLE);
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(post.pictureUrl);
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .transform(new MyTransformation(context, 90))
                    .into(mPictureField);
        } else {
            mPictureField.setVisibility(View.GONE);
        }

    }

    class MyTransformation extends BitmapTransformation {

        private int mOrientation;

        public MyTransformation(Context context, int orientation) {
            super(context);
            mOrientation = orientation;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            int exifOrientationDegrees = getExifOrientationDegrees(mOrientation);
            return TransformationUtils.rotateImageExif(toTransform, pool, exifOrientationDegrees);
        }

        @Override
        public String getId() {
            return "com.progremastudio.emergencymedicalteam.viewholder.PostViewHolder.MyTransformation";
        }

        private int getExifOrientationDegrees(int orientation) {
            int exifInt;
            switch (orientation) {
                case 90:
                    exifInt = ExifInterface.ORIENTATION_ROTATE_90;
                    break;
                default:
                    exifInt = ExifInterface.ORIENTATION_NORMAL;
                    break;
            }
            return exifInt;
        }
    }

}
