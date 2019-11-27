package com.fendonus.timetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fendonus.timetracker.motionDetection.MotionDetector;
import com.fendonus.timetracker.motionDetection.MotionDetectorCallback;

public class TimerActivity extends AppCompatActivity {
    private static final int MY_CAMERA_REQUEST_CODE = 101;
    private EditText startingTimeET;
    private Button okButton;
    private MotionDetector motionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_timer);

        startingTimeET = findViewById(R.id.startingET);
        okButton = findViewById(R.id.okButton);

        motionDetector = new MotionDetector(this, (SurfaceView) findViewById(R.id.surfaceViewTimer));

        checkCameraPermission();

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!startingTimeET.getText().toString().isEmpty()){
                    int millisec = Integer.parseInt(startingTimeET.getText().toString());
                    if (millisec >= 5){
                        Intent intent = new Intent(TimerActivity.this, MainActivity.class);
                        intent.putExtra("time", millisec);
                        startActivity(intent);
                        finish();
                    }else {
                        startingTimeET.setError("Set minimum 5 seconds");
                    }

                }
            }
        });
    }
    private void checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},  MY_CAMERA_REQUEST_CODE);
            }else {
                detectMotion();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                detectMotion();

            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        motionDetector.onResume();
        if (motionDetector.checkCameraHardware()) {
            //Toast.makeText(MainActivity.this, "Camera found", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(TimerActivity.this, "No Camera available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        motionDetector.onPause();
    }

    public void detectMotion(){
        motionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {
            @Override
            public void onMotionDetected() {

            }

            @Override
            public void onTooDark() {
                Toast.makeText(TimerActivity.this, "Too dark", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
