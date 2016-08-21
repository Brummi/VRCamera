package com.brummid.vrcamera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Felix on 14.08.2016.
 */
public class VRCameraInputAdapter
{
    private VRCamera cam;

    private float yaw, pitch, roll;
    private float dYaw, dPitch, dRoll;
    private float mYaw, mPitch, mRoll;
    private float calibYaw, calibPitch, calibRoll;
    private float factorYaw, factorPitch, factorRoll;

    private Interpolator yawInterpolator, pitchInterpolator, rollInterpolator;
    private int interpolatorSize;

    private boolean logging;

    public VRCameraInputAdapter(VRCamera cam)
    {
        this.cam = cam;

        yaw = 0;
        pitch = 0;
        roll = 0;

        dYaw = 0;
        dPitch = 0;
        dRoll = 0;

        calibYaw = 0;
        calibPitch = 0;
        calibRoll = 0;

        factorYaw = 1;
        factorPitch = 1;
        factorRoll = 1;

        yawInterpolator = new Interpolator(4);
        pitchInterpolator = new Interpolator(4);
        rollInterpolator = new Interpolator(4);

        logging = false;
    }

    public void update(float delta){
        mYaw += Gdx.input.getGyroscopeX() * delta % ((float)Math.PI * 2);

        mPitch = Gdx.input.getAccelerometerZ() / getTotalAcceleration();
        if(mPitch > 1)mPitch = 1;
        else if(mPitch < -1)mPitch = -1;
        mPitch = (float) Math.asin(mPitch) - pitch;

        mRoll = Gdx.input.getAccelerometerY() / getTotalAcceleration();
        if(mRoll > 1)mRoll = 1;
        else if(mRoll < -1)mRoll = -1;
        mRoll = (float) Math.asin(mRoll) - roll;

        if(Float.isNaN(mYaw))mYaw = 0;
        if(Float.isNaN(mPitch))mPitch = 0;
        if(Float.isNaN(mRoll))mRoll = 0;

        dYaw = yawInterpolator.calculate(mYaw - yaw, delta);
        dPitch = pitchInterpolator.calculate((mPitch + Gdx.input.getGyroscopeY() * delta) / 2f, delta);
        dRoll = rollInterpolator.calculate((mRoll + Gdx.input.getGyroscopeZ() * delta) / 2f, delta);

        yaw += dYaw;
        pitch += dPitch;
        roll += dRoll;

        cam.rotateRad(
                factorYaw * dYaw,
                factorPitch * dPitch,
                factorRoll * dRoll
        );

        if(logging){
            Gdx.app.log("Rotation", "Yaw: " + Math.toDegrees(yaw) + " Pitch: " + Math.toDegrees(pitch) + " Roll: " + Math.toDegrees(roll));
            Gdx.app.log("Delta", "dYaw: " + dYaw + " dPitch: " + dPitch + " dRoll: " + dRoll);
        }
    }

    private class Interpolator{

        private Array<Float> lastValues;
        private int size;

        private float tmp;

        public Interpolator(int size){
            this.size = size;
            lastValues = new Array<Float>(size);
        }

        public float calculate(float value, float delta){
            value /= delta;

            if(lastValues.size == size)lastValues.removeIndex(size - 1);

            lastValues.add(value);

            tmp = 0;
            for(int i = 0; i < lastValues.size; ++i)tmp += lastValues.get(i);
            tmp /= (float) lastValues.size;
            value = (value + tmp) / 2;

            return value * delta;
        }
    }

    public void calibrate()
    {
        calibYaw = yaw;
        calibPitch = (float) Math.asin(Gdx.input.getAccelerometerZ() / 9.81f);
        calibRoll = (float) Math.asin(Gdx.input.getAccelerometerY() / 9.81f);

        cam.rotateRad(calibYaw, calibPitch, calibRoll);
    }

    private float getTotalAcceleration(){
        return (float) Math.sqrt(
                Gdx.input.getAccelerometerX() * Gdx.input.getAccelerometerX() +
                Gdx.input.getAccelerometerY() * Gdx.input.getAccelerometerY() +
                Gdx.input.getAccelerometerZ() * Gdx.input.getAccelerometerZ()
        );
    }

    public float getCalibYaw() {
        return calibYaw;
    }

    public void setCalibYaw(float calibYaw) {
        this.calibYaw = calibYaw;
        cam.rotate(calibYaw, 0, 0);
    }

    public float getCalibPitch() {
        return calibPitch;
    }

    public void setCalibPitch(float calibPitch) {
        this.calibPitch = calibPitch;
        cam.rotate(0, calibPitch, 0);
    }

    public float getCalibRoll() {
        return calibRoll;
    }

    public void setCalibRoll(float calibRoll) {
        this.calibRoll = calibRoll;
        cam.rotate(0, 0, calibRoll);
    }

    public float getFactorYaw() {
        return factorYaw;
    }

    public void setFactorYaw(float factorYaw) {
        this.factorYaw = factorYaw;
    }

    public float getFactorPitch() {
        return factorPitch;
    }

    public void setFactorPitch(float factorPitch) {
        this.factorPitch = factorPitch;
    }

    public float getFactorRoll() {
        return factorRoll;
    }

    public void setFactorRoll(float factorRoll) {
        this.factorRoll = factorRoll;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public void setInterpolatorSize(int size){
        if(size < 1)size = 1;
        this.interpolatorSize = size;
        yawInterpolator = new Interpolator(size);
        pitchInterpolator = new Interpolator(size);
        rollInterpolator = new Interpolator(size);
    }

    public int getInterpolatorSize(){
        return interpolatorSize;
    }
}
