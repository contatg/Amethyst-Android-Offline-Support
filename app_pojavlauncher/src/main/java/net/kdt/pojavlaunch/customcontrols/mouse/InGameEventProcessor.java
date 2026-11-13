package net.kdt.pojavlaunch.customcontrols.mouse;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;

import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import org.lwjgl.glfw.CallbackBridge;

public class InGameEventProcessor implements TouchEventProcessor {
    private final Handler mGestureHandler = new Handler(Looper.getMainLooper());
    private final double mSensitivity;
    private boolean mEventTransitioned = true;
    private boolean mCameraOnly;
    private final PointerTracker mTracker = new PointerTracker();
    private final LeftClickGesture mLeftClickGesture = new LeftClickGesture(mGestureHandler);
    private final RightClickGesture mRightClickGesture = new RightClickGesture(mGestureHandler);

    public InGameEventProcessor(double sensitivity) {
        mSensitivity = sensitivity;
    }

    public void setCameraOnly(boolean cameraOnly) {
        if (mCameraOnly == cameraOnly) return;
        cancelPendingActions();
        mCameraOnly = cameraOnly;
    }

    @Override
    public boolean processTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTracker.startTracking(motionEvent);
                if (mCameraOnly || LauncherPreferences.PREF_DISABLE_GESTURES) break;
                mEventTransitioned = false;
                checkGestures();
                break;
            case MotionEvent.ACTION_MOVE:
                mTracker.trackEvent(motionEvent);
                float[] motionVector = mTracker.getMotionVector();
                double sensitivity = mSensitivity * (mCameraOnly ? LauncherPreferences.PREF_MOUSESPEED : 1);
                float deltaX = (float) (motionVector[0] * sensitivity);
                float deltaY = (float) (motionVector[1] * sensitivity);
                if (mCameraOnly) {
                    if (deltaX != 0 || deltaY != 0) {
                        CallbackBridge.sendCursorPos(CallbackBridge.mouseX + deltaX, CallbackBridge.mouseY + deltaY);
                    }
                    break;
                }
                mLeftClickGesture.setMotion(deltaX, deltaY);
                mRightClickGesture.setMotion(deltaX, deltaY);
                CallbackBridge.mouseX += deltaX;
                CallbackBridge.mouseY += deltaY;
                CallbackBridge.sendCursorPos(CallbackBridge.mouseX, CallbackBridge.mouseY);
                if(LauncherPreferences.PREF_DISABLE_GESTURES) break;
                checkGestures();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mCameraOnly) mTracker.updatePointerCount(motionEvent);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (mCameraOnly) mTracker.updatePointerAfterLift(motionEvent);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mTracker.cancelTracking();
                cancelGestures(false);
        }
        return true;
    }

    @Override
    public void cancelPendingActions() {
        mTracker.cancelTracking();
        cancelGestures(true);
    }

    private void checkGestures() {
        mLeftClickGesture.inputEvent();
        // Only register right click events if it's a fresh event stream, not one after a transition.
        // This is done to avoid problems when people hold the button for just a bit too long after
        // exiting a menu for example.
        if(!mEventTransitioned) mRightClickGesture.inputEvent();
    }

    private void cancelGestures(boolean isSwitching) {
        mEventTransitioned = true;
        mLeftClickGesture.cancel(isSwitching);
        mRightClickGesture.cancel(isSwitching);
    }
}
