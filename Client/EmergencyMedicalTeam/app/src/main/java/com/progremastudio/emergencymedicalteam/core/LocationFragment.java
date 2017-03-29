package com.progremastudio.emergencymedicalteam.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.progremastudio.emergencymedicalteam.R;

public class LocationFragment extends Fragment {

    private static final String TAG = "location-fragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_location, container, false);

        return rootView;

    }
}
