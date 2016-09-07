package com.example.a111.game.video.video2d;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class VR2DVideoActivity extends Activity {

    private VideoView mVideoView;
    private String path = "/storage/sdcard1/VRResources/1.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mVideoView = new VideoView(this);
        setContentView(mVideoView);
        mVideoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        Bundle bundle = getIntent().getExtras().getBundle("content");
        path = bundle.getString("path");
       // mVideoView.setPath(path);
    }

    @Override
    protected void onResume() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
        mVideoView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mVideoView.onStop();
    }
}
