package com.example.volumecontrolservice;

import static android.media.AudioManager.STREAM_MUSIC;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.widget.Toast.LENGTH_SHORT;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {
    // settings put secure enabled_accessibility_services com.example.volumecontrolservice/com.example.volumecontrolservice.AccessibilityService
    private static final String TAG = "yolo volume_control";
    private static final long DELAY_VOLUME_LONG_PRESS = 100;
    private Handler mLongPressHandler;
    private AudioManager audioManager;
    private boolean eatLongPressActionUp;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "service is connected");
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) { }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        return handleKeyEvent(event);
    }

    private boolean handleKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (keyCode == KEYCODE_DPAD_CENTER) {
            switch (action) {
                case ACTION_DOWN:
                    Log.i(TAG, "yolo dpad center is down ");
                    checkForLongPress();
                    return false;
                case ACTION_UP:
                    if(mLongPressHandler !=null ) mLongPressHandler.removeCallbacksAndMessages(null);
                    if(eatLongPressActionUp){
                        eatLongPressActionUp = false;
                        return true;
                    }
                    return false;
            }
        }
        return false;
    }

    private void checkForLongPress() {
        if (mLongPressHandler == null) {
            mLongPressHandler = new Handler();
        }
        else {
            // this shouldn't be happening, down event coming before previous up event is processed
            mLongPressHandler.removeCallbacksAndMessages(null);
            mLongPressHandler = new Handler();
        }

        mLongPressHandler.postDelayed(() -> mLongPressHandler.postDelayed(() -> {
//            Toast.makeText(this, "Adjust volume then press okay to resume", LENGTH_SHORT).show();
            Log.i(TAG, "ready to adjust volume");
            showDialog();
            eatLongPressActionUp = true;
        }, DELAY_VOLUME_LONG_PRESS), DELAY_VOLUME_LONG_PRESS);
    }

    private void showDialog() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            final boolean overlayEnabled = Settings.canDrawOverlays(this);
            if(!overlayEnabled){
                Toast.makeText(this, "Enable draw over apps permission", LENGTH_SHORT).show();
            }
            Log.i(TAG, "overlay: " + overlayEnabled);
            if (!overlayEnabled) return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {

            final Dialog dialog = new Dialog(AccessibilityService.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.volume_control);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.background_dark);
            dialog.setCancelable(true);
            ProgressBar volumeProgress = dialog.findViewById(R.id.volume_progress);
            dialog.findViewById(R.id.close).setOnClickListener(v -> dialog.cancel());
            volumeProgress.setMax(audioManager.getStreamMaxVolume(STREAM_MUSIC));
            volumeProgress.setProgress(audioManager.getStreamVolume(STREAM_MUSIC));
            dialog.setOnKeyListener((dialog1, keyCode, event) -> {
                int action = event.getAction();
                if (keyCode == KEYCODE_DPAD_LEFT || keyCode == KEYCODE_DPAD_DOWN) {
                    switch (action) {
                        case ACTION_DOWN:
                            Log.i(TAG, "yolo dpad down or left is pressed");
                            reduceVolume();
                            volumeProgress.setProgress(audioManager.getStreamVolume(STREAM_MUSIC));
                            return true;
                        case ACTION_UP:
                            return true;
                    }
                }

                if (keyCode == KEYCODE_DPAD_RIGHT || keyCode == KEYCODE_DPAD_UP) {
                    switch (action) {
                        case ACTION_DOWN:
                            Log.i(TAG, "yolo dpad up or right is pressed");
                            increaseVolume();
                            volumeProgress.setProgress(audioManager.getStreamVolume(STREAM_MUSIC));
                            return true;
                        case ACTION_UP:
                            return true;
                    }
                }

                return false;
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            }else{
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }

            dialog.show();
        });
    }

    private void increaseVolume() {
        Log.i(TAG, "yolo, increasing volume");
        audioManager.adjustStreamVolume(STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
    }

    private void reduceVolume() {
        Log.i(TAG, "yolo, reducing volume");
        audioManager.adjustStreamVolume(STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
    }
}