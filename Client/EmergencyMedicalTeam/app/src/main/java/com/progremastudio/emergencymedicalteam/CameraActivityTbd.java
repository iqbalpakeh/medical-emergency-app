package com.progremastudio.emergencymedicalteam;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;

import java.io.File;
import java.io.FileOutputStream;

public class CameraActivityTbd extends BaseActivity {

    private static final String TAG = "camera-activity";

    private CameraView mCameraView;

    private ImageButton mCaptureButton;

    private Uri mUri;

    private Bitmap mBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_tbd);

        mCameraView = (CameraView) findViewById(R.id.camera);
        mCameraView.setCameraListener(new CameraListener() {
            @Override
            public void onCameraOpened() {
                super.onCameraOpened();
            }

            @Override
            public void onCameraClosed() {
                super.onCameraClosed();
            }

            @Override
            public void onPictureTaken(byte[] jpeg) {
                super.onPictureTaken(jpeg);

                Log.d(TAG, "picture taken");

                // Create a bitmap
                mBitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);

                saveBitmapOnDirectory();

                startActivity(new Intent(CameraActivityTbd.this, MainActivity.class));
                finish();

            }

            @Override
            public void onPictureTaken(YuvImage yuv) {
                super.onPictureTaken(yuv);
            }

            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
            }
        });

        mCaptureButton = (ImageButton) findViewById(R.id.capture_button);
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraView.captureImage();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void saveBitmapOnDirectory() {

        try {

            File directoryPath = new File(this.getFilesDir(), "post");
            if (!directoryPath.exists()) {
                directoryPath.mkdirs();
            }

            File filePath = new File(directoryPath.getPath() + File.separator + "accident.jpg");
            FileOutputStream outputStream = new FileOutputStream(filePath.getPath());

            mUri = Uri.parse("file://" + filePath.getPath());
            mBitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);

            outputStream.flush();
            outputStream.close();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
