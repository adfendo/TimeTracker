package com.fendonus.timetracker.motionDetection;

public interface MotionDetectorCallback {
    void onMotionDetected();
    void onTooDark();
}
