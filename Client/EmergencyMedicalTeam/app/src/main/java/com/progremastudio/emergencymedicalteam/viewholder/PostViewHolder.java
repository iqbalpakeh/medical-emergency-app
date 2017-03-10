package com.progremastudio.emergencymedicalteam.viewholder;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.Post;

public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView mNameField;
    public TextView mMessageField;
    public TextView mAddressField;
    public TextView mTimestampField;
    public TextView mEmergencyTypeField;
    public TextView mPictureUrlField;

    PostViewHolder(View itemView) {
        super(itemView);
        mNameField = (TextView) itemView.findViewById(R.id.display_name_field);
        mMessageField = (TextView) itemView.findViewById(R.id.message_field);
        mAddressField = (TextView) itemView.findViewById(R.id.address_field);
        mTimestampField = (TextView) itemView.findViewById(R.id.timestamp_field);
        mEmergencyTypeField = (TextView) itemView.findViewById(R.id.emergency_type_field);
        mPictureUrlField = (TextView) itemView.findViewById(R.id.picture_url_field);
    }

    public void bindToPost(Post post, View.OnClickListener clickListener) {
        mNameField.setText(post.displayName);
        mMessageField.setText(post.message);
        mAddressField.setText(post.locationCoordinate);
        mTimestampField.setText(post.timestamp);
        mEmergencyTypeField.setText(post.emergencyType);
        mPictureUrlField.setText(post.pictureUrl);
    }

    private class ThumbnailTasks extends AsyncTask {

        private PostViewHolder mHolder;
        private String mUrl;

        public ThumbnailTasks(PostViewHolder holder, String url) {
            mHolder = holder;
            mUrl = url;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }
    }

}
