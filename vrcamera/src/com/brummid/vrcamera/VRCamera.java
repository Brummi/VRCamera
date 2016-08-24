package com.brummid.vrcamera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;


public class VRCamera
{
    private float fieldOfView;
    private float near;
    private float far;
    private float eyeDistance;
    private float viewportWidth, viewportHeight;

    private PerspectiveCamera leftCam, rightCam;

    private Vector3 position;
    private Vector3 direction;
    private Vector3 up;
    private float yaw, pitch, roll;

    private Vector3 leftPosition;
    private Vector3 rightPosition;

    private FrameBuffer leftBuffer, rightBuffer;
    private RendererForVR renderer;

    private int screenWidth, screenHeight;

    private Vector2 tmpVec2;
    private Vector3 tmpVec3;

    /**
     *
     * @param fieldOfView The field of view of each eye (in degrees)
     * @param near The minimum distance to the camera a object needs to have to be rendered
     * @param far The maximum distance to the camera a object can have to be rendered
     * @param eyeDistance The distance between the eyes (@link PerspectiveCamera)
     * @param viewportWidth
     * @param viewportHeight
     * @param renderer The render interface. The VRCamer calls the interfaces render method for each eye.
     */

    public VRCamera(float fieldOfView, float near, float far, float eyeDistance, float viewportWidth, float viewportHeight, RendererForVR renderer)
    {
        tmpVec2 = new Vector2(0, 0);
        tmpVec3 = new Vector3(0, 0, 0);

        leftCam = new PerspectiveCamera();
        rightCam = new PerspectiveCamera();


        position = new Vector3(0, 0, 0);
        direction = new Vector3(0, 0, 0);
        up = new Vector3(0, 1, 0);

        yaw = 0;
        pitch = 0;
        roll = (float)Math.PI;

        leftPosition = new Vector3(0, 0, 0);
        rightPosition = new Vector3(0, 0, 0);

        setFieldOfView(fieldOfView);
        setNear(near);
        setFar(far);

        setViewportWidth(viewportWidth);
        setViewportHeight(viewportHeight);

        setEyeDistance(eyeDistance);

        setToRotationRad(yaw, pitch, roll);

        update();

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        leftBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, screenWidth, screenHeight, true);
        rightBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, screenWidth, screenHeight, true);

        this.renderer = renderer;
    }

    /**
     * @param renderer The render interface. The VRCamer calls the interfaces render method for each eye.
     */

    public VRCamera(RendererForVR renderer){
        this(90, 0.00001f, 128f, 0.5f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), renderer);
    }

    /**
     * Updates both "eyes" to fit the rotation and translation.
     */
    public void update()
    {
        leftCam.position.set(leftPosition);
        leftCam.direction.set(direction);
        leftCam.up.set(up);
        leftCam.update();

        rightCam.position.set(rightPosition);
        rightCam.direction.set(direction);
        rightCam.up.set(up);
        rightCam.update();
    }

    /**
     * Renders both eyes next to each other on the screen
     * @param batch The batch the camera renders both eyes on
     */
    public void render(Batch batch)
    {
        leftBuffer.begin();
        renderer.renderForVR(leftCam);
        leftBuffer.end();

        rightBuffer.begin();
        renderer.renderForVR(rightCam);
        rightBuffer.end();

        batch.begin();
        batch.draw(leftBuffer.getColorBufferTexture(), 0, 0, screenWidth / 2f, screenHeight);
        batch.draw(rightBuffer.getColorBufferTexture(), screenWidth / 2f, 0, screenWidth / 2f, screenHeight);
        batch.end();

    }

    public void translate(Vector3 translate){
        translate(translate.x, translate.y, translate.z);
    }

    public void translate(float x, float y, float z)
    {
        position.add(x, y, z);
        leftPosition.add(x, y, z);
        rightPosition.add(x, y, z);
    }

    public void setToTranslation(Vector3 translation){
        tmpVec3.set(translation);
        tmpVec3.sub(position);

        translate(tmpVec3);

        tmpVec3.setZero();
    }

    public void rotate(float dyaw, float dpitch, float droll){
        rotateRad((float) Math.toRadians(dyaw), (float) Math.toRadians(dpitch), (float) Math.toRadians(droll));
    }

    public void rotateRad(float dyaw, float dpitch, float droll){
        setToRotationRad(yaw + dyaw, pitch + dpitch, roll + droll);
    }

    public void setToRotation(float yaw, float pitch, float roll)
    {
        setToRotationRad((float) Math.toRadians(yaw), (float) Math.toRadians(pitch), (float) Math.toRadians(roll));
    }

    public void setToRotationRad(float yaw, float pitch, float roll){

        while(yaw < 0)yaw += (float)Math.PI * 2f;
        yaw = yaw % (float)(Math.PI * 2);

        while(pitch < 0)pitch += (float)Math.PI * 2f;
        pitch = pitch % (float)(Math.PI * 2);

        while(pitch < 0)roll += (float)Math.PI * 2f;
        roll = roll % (float)(Math.PI * 2);

        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;

        direction.set(vectorFromAngles(this.yaw, this.pitch));

        tmpVec3 = vectorFromAngles(this.yaw + (float)Math.PI / 2f, this.roll);

        up.set(direction);
        up.crs(tmpVec3);
        up.setLength(1);

        tmpVec3.setLength(eyeDistance / 2);

//        Gdx.app.log("TmpVec3", "Vec: " + tmpVec3);

        leftPosition.set(position);
        rightPosition.set(position);

        leftPosition.add(tmpVec3);
        rightPosition.sub(tmpVec3);
    }

    public void lookAt(Vector3 at){
        lookAt(at.x, at.y, at.z);
    }

    public void lookAt(float x, float y, float z){
        direction(x - position.x, y - position.y, z - position.z);
    }

    public void direction(Vector3 direction) {
        direction(direction.x, direction.y, direction.z);
    }

    public void direction(float x, float y, float z){
        float mYaw = - (float) Math.acos(x / pythagoras(x, z));
        float mPitch = (float) Math.atan(y / pythagoras(x, z));

        setToRotation(mYaw, mPitch, roll);
    }

    private Vector3 vectorFromAngles(float yaw, float pitch)
    {
        //!! Y-UP System

        tmpVec3.setZero();

        if(pitch == Math.PI / 2){
            tmpVec3.set(0, 1, 0);
            return tmpVec3;
        }

        tmpVec3.x = (float) Math.cos(-yaw);

        tmpVec3.z = (float) Math.sin(-yaw);

//        tmpVec3.y = (float) Math.abs(Math.tan(pitch) * pythagoras(tmpVec3.x, tmpVec3.z));
        tmpVec3.y = (float) Math.tan(pitch) * pythagoras(tmpVec3.x, tmpVec3.z);

        if(pitch > Math.PI / 2 && pitch < Math.PI / 2 * 3){
            tmpVec3.x = -tmpVec3.x;
            tmpVec3.z = -tmpVec3.z;
        }

//        if(pitch > Math.PI) tmpVec3.y = -tmpVec3.y;

        tmpVec3.setLength(1);

        return tmpVec3;
    }


    private float pythagoras(float a, float b) {
        return (float) Math.sqrt(a * a + b * b);
    }

    public void setFieldOfView(float fieldOfView){
        this.fieldOfView = fieldOfView;
        leftCam.fieldOfView = fieldOfView;
        rightCam.fieldOfView = fieldOfView;
    }

    public void setNear(float near){
        this.near = near;
        leftCam.near = near;
        rightCam.near = near;
    }

    public void setFar(float far){
        this.far = far;
        leftCam.far = far;
        rightCam.far = far;
    }

    public void setEyeDistance(float eyeDistance){
        this.eyeDistance = eyeDistance;
        setToRotationRad(yaw, pitch, roll);
    }

    public void setViewportWidth(float viewportWidth){
        this.viewportWidth = viewportWidth;
        leftCam.viewportWidth = viewportWidth / 2;
        rightCam.viewportWidth = viewportWidth / 2;
    }

    public void setViewportHeight(float viewportHeight){
        this.viewportHeight = viewportHeight;
        leftCam.viewportHeight = viewportHeight;
        rightCam.viewportHeight = viewportHeight;
    }

    public float getFieldOfView() {
        return fieldOfView;
    }

    public float getNear() {
        return near;
    }

    public float getFar() {
        return far;
    }

    public float getEyeDistance() {
        return eyeDistance;
    }

    public float getViewportWidth() {
        return viewportWidth;
    }

    public float getViewportHeight() {
        return viewportHeight;
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getDirection() {
        return direction;
    }

    public Vector3 getUp() {
        return up;
    }

    public float getYaw() {
        return (float) Math.toDegrees(yaw);
    }

    public float getPitch() {
        return (float) Math.toDegrees(pitch);
    }

    public float getRoll() {
        return (float) Math.toDegrees(roll);
    }

    public Vector3 getLeftPosition() {
        return leftPosition;
    }

    public Vector3 getRightPosition() {
        return rightPosition;
    }
}
