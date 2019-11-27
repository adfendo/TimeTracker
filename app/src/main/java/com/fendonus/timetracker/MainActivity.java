package com.fendonus.timetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fendonus.timetracker.motionDetection.MotionDetector;
import com.fendonus.timetracker.motionDetection.MotionDetectorCallback;

public class MainActivity extends AppCompatActivity {
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_PHOTO_REQUEST_CODE = 101;
    private MotionDetector motionDetector;
    private TextView countTv;
    private Button okBtn;
    private CountDownTimer countDownTimer;
    private long remainingTimeCount = 0;
    private boolean isReadyToStart;
    private int milliToStart = 0;
    long millisecondTime, startTime, timeBuff, updateTime = 0L ;
    Handler handler;
    int Seconds, Minutes, MilliSeconds ;
    private LinearLayout countLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        milliToStart = intent.getIntExtra("time",0);

        countLL = findViewById(R.id.countLL);
        countTv = findViewById(R.id.countTv);
        okBtn = findViewById(R.id.goBackBtn);
        okBtn.setVisibility(View.GONE);

        motionDetector = new MotionDetector(this, (SurfaceView) findViewById(R.id.surfaceView));

        checkCameraPermission();

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TimerActivity.class));
                finish();
            }
        });
    }

    int count = 0;
    private void startTimer(final long miliseconds) {
        countDownTimer = new CountDownTimer(miliseconds, 1000) {
            public void onTick(long millisUntilFinished) {
                countTv.setText("Starting in: " + String.valueOf(millisUntilFinished / 1000) + " seconds");
                remainingTimeCount = (millisUntilFinished / 1000);
                count = count + 1;
                if (remainingTimeCount == 1){
                    readySetGo();
                }
            }

            public void onFinish() {
                 countTv.setText("Started");
                 isReadyToStart = true;
                 detectMotion();
                 startStopwatch();
            }

        }.start();
    }

    private void checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},  MY_CAMERA_REQUEST_CODE);
            }else {
                startTimer((milliToStart * 1000));
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                //detectMotion();
                startTimer((milliToStart * 1000));
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public void detectMotion(){
        motionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {
            @Override
            public void onMotionDetected() {
                audioPlayer();
                Log.e("12345", "detected");
                okBtn.setVisibility(View.VISIBLE);
                handler.removeCallbacks(runnable);
                motionDetector.onPause();
            }

            @Override
            public void onTooDark() {
                Toast.makeText(MainActivity.this, "Too dark", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void startStopwatch(){
        if (isReadyToStart){
            handler = new Handler() ;
            startTime = SystemClock.uptimeMillis();
            handler.postDelayed(runnable, 0);
        }
    }

    public Runnable runnable = new Runnable() {
        public void run() {
            millisecondTime = SystemClock.uptimeMillis() - startTime;
            updateTime = timeBuff + millisecondTime;
            Seconds = (int) (updateTime / 1000);
            Minutes = Seconds / 60;
            Seconds = Seconds % 60;
            MilliSeconds = (int) (updateTime % 1000);
            countTv.setText("" + Minutes + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds));

            handler.postDelayed(this, 0);
        }

    };

    @Override
    protected void onResume() {
        super.onResume();

        motionDetector.onResume();

        if (motionDetector.checkCameraHardware()) {
            //Toast.makeText(MainActivity.this, "Camera found", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "No Camera available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        motionDetector.onPause();
    }

    public void audioPlayer(){
        MediaPlayer mediaPlayer= MediaPlayer.create(getApplicationContext(), R.raw.alarm);
        mediaPlayer.start();
    }

    public void readySetGo(){
        MediaPlayer mediaPlayer= MediaPlayer.create(getApplicationContext(), R.raw.gun_fire);
        mediaPlayer.start();
    }

}
