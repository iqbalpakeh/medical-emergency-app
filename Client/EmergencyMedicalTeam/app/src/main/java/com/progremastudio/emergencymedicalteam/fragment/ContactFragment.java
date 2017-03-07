package com.progremastudio.emergencymedicalteam.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.progremastudio.emergencymedicalteam.R;

import java.io.File;

public class ContactFragment extends Fragment {

    private ImageView mImageView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);

        mImageView = (ImageView) rootView.findViewById(R.id.image_view);
        showImageView();

        return rootView;

    }

    private void showImageView() {

        try {

            File directoryPath = new File(getActivity().getFilesDir(), "post");
            File filePath = new File(directoryPath.getPath() + File.separator + "accident.jpg");

            Bitmap myBitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath());

            try {
                ExifInterface exif = new ExifInterface(filePath.getAbsolutePath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                }
                else if (orientation == 3) {
                    matrix.postRotate(180);
                }
                else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
            }
            catch (Exception e) {

            }

            mImageView.setImageBitmap(myBitmap);


        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

}
