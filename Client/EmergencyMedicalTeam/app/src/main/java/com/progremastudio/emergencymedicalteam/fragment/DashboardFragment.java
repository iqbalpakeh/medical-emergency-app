package com.progremastudio.emergencymedicalteam.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.progremastudio.emergencymedicalteam.AppContext;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.Post;

import java.util.HashMap;
import java.util.Map;

public class DashboardFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "DashboardFragment";
    private static final String REQUIRED = "Required";

    private DatabaseReference mDatabase;

    private GoogleMap mMap;

    private EditText mEditText;
    private Button mSubmitButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mEditText = (EditText) rootView.findViewById(R.id.edit_text);

        mSubmitButton = (Button) rootView.findViewById(R.id.submit_code);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng sydney = new LatLng(-34, 151);
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void submit() {

        final String content = mEditText.getText().toString();

        if (TextUtils.isEmpty(content)) {
            mEditText.setError(REQUIRED);
            return;
        }

        setEditingEnabled(false);
        Toast.makeText(getActivity(), "Posting...", Toast.LENGTH_SHORT).show();

        final String userId = ((BaseActivity) getActivity()).getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get text value
                        Post post = dataSnapshot.getValue(Post.class);

                        if (post == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(getActivity(),
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewPost(userId, content);
                        }

                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        setEditingEnabled(true);
                    }
                });
    }

    private void setEditingEnabled(boolean enabled) {
        mEditText.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    private void writeNewPost(String userId, String message) {

        // Create new post at /user-posts/$userid/$postid and
        // at /posts/$postid simultaneously

        String key = mDatabase.child("posts").push().getKey();

        String displayName = AppContext.fetchCurrentUserDisplayName(getActivity());
        String email = AppContext.fetchCurrentUserEmail(getActivity());
        String timestamp = "to be implemented"; //todo: get timestamp
        String locationCoordinate ="to be implemented"; //todo: get location coordinate
        String pictureUrl = "to be implemented"; //todo: get picture url
        String emergencyType = "to be implemented"; //todo: get emergency type

        Post post = new Post(
                        userId,
                        displayName,
                        email,
                        timestamp,
                        locationCoordinate,
                        message,
                        pictureUrl,
                        emergencyType);

        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }

}
