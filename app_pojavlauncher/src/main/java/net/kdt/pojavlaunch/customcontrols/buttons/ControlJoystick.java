package net.kdt.pojavlaunch.customcontrols.buttons;

import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_EAST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NONE;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NORTH;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NORTH_EAST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NORTH_WEST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_SOUTH;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_SOUTH_EAST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_SOUTH_WEST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_WEST;

import android.annotation.SuppressLint;
import android.view.View;

import net.kdt.pojavlaunch.LwjglGlfwKeycode;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.customcontrols.ControlData;
import net.kdt.pojavlaunch.customcontrols.ControlJoystickData;
import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.handleview.EditControlSideDialog;

import org.lwjgl.glfw.CallbackBridge;

import io.github.controlwear.virtual.joystick.android.JoystickView;

@SuppressLint("ViewConstructor")
public class ControlJoystick extends JoystickView implements ControlInterface {
    public final static int DIRECTION_FORWARD_LOCK = 8;
    // Directions keycode
    private final int[] mDirectionForwardLock = new int[]{LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL};
    private static final int[] MOVEMENT_KEYS = {
            LwjglGlfwKeycode.GLFW_KEY_W, LwjglGlfwKeycode.GLFW_KEY_D,
            LwjglGlfwKeycode.GLFW_KEY_S, LwjglGlfwKeycode.GLFW_KEY_A
    };
    private ControlJoystickData mControlData;
    private int movementKeys;
    private boolean forwardLocked;
    public ControlJoystick(ControlLayout parent, ControlJoystickData data) {
        super(parent.getContext());
        init(data, parent);
    }

    private static void sendInput(int[] keys, boolean isDown) {
        for (int key : keys) {
            CallbackBridge.sendKeyPress(key, CallbackBridge.getCurrentMods(), isDown);
        }
    }

    private void init(ControlJoystickData data, ControlLayout layout) {
        mControlData = data;
        setProperties(preProcessProperties(data, layout));
        setDeadzone(24);
        setFixedCenter(data.absolute);
        setAutoReCenterButton(true);

        injectBehaviors();

        setOnMoveListener(new OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                updateMovement(getDirectionInt(angle, strength));
            }

            @Override
            public void onForwardLock(boolean isLocked) {
                if (forwardLocked == isLocked) return;
                forwardLocked = isLocked;
                sendInput(mDirectionForwardLock, isLocked);
            }
        });
    }

    @Override
    public void releaseTouchInputs() {
        updateMovement(DIRECTION_NONE);
        if (forwardLocked) {
            forwardLocked = false;
            sendInput(mDirectionForwardLock, false);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (!visible) releaseTouchInputs();
        ControlInterface.super.setVisible(visible);
    }

    @Override
    public View getControlView() {
        return this;
    }

    @Override
    public ControlData getProperties() {
        return mControlData;
    }

    @Override
    public void setProperties(ControlData properties, boolean changePos) {
        mControlData = (ControlJoystickData) properties;
        mControlData.isHideable = true;
        ControlInterface.super.setProperties(properties, changePos);
        postDelayed(() -> {
            setForwardLockDistance(mControlData.forwardLock ? (int) Tools.dpToPx(60) : 0);
            setFixedCenter(mControlData.absolute);
        }, 10);
    }

    @Override
    public void removeButton() {
        getControlLayoutParent().getLayout().mJoystickDataList.remove(getProperties());
        getControlLayoutParent().removeView(this);
    }

    @Override
    public void cloneButton() {
        ControlJoystickData data = new ControlJoystickData(mControlData);
        getControlLayoutParent().addJoystickButton(data);
    }


    @Override
    public void setBackground() {
        setBorderWidth((int) Tools.dpToPx(getProperties().strokeWidth * (getControlLayoutParent().getLayoutScale()/100f)));
        setBorderColor(getProperties().strokeColor);
        setBackgroundColor(getProperties().bgColor);
    }

    @Override
    public void sendKeyPresses(boolean isDown) {/*STUB since non swipeable*/ }

    @Override
    public void loadEditValues(EditControlSideDialog editControlPopup) {
        editControlPopup.loadJoystickValues(mControlData);
    }

    private int getDirectionInt(int angle, int intensity) {
        if (intensity == 0) return DIRECTION_NONE;
        return (int) (((angle + 22.5) / 45) % 8);
    }

    private void updateMovement(int direction) {
        int nextKeys = movementMask(direction);
        int changedKeys = movementKeys ^ nextKeys;
        for (int i = 0; i < MOVEMENT_KEYS.length; i++) {
            int bit = 1 << i;
            if ((changedKeys & bit) != 0) {
                CallbackBridge.sendKeyPress(MOVEMENT_KEYS[i], CallbackBridge.getCurrentMods(), (nextKeys & bit) != 0);
            }
        }
        movementKeys = nextKeys;
    }

    private static int movementMask(int direction) {
        switch (direction) {
            case DIRECTION_NORTH: return 1;
            case DIRECTION_NORTH_EAST: return 1 | 2;
            case DIRECTION_EAST: return 2;
            case DIRECTION_SOUTH_EAST: return 2 | 4;
            case DIRECTION_SOUTH: return 4;
            case DIRECTION_SOUTH_WEST: return 4 | 8;
            case DIRECTION_WEST: return 8;
            case DIRECTION_NORTH_WEST: return 8 | 1;
            default: return 0;
        }
    }

}
