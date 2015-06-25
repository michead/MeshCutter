package com.michead.meshcutter2;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by michead on 23/06/15.
 */
public class MeshCutterGLSurfaceView extends GLSurfaceView {

    private static final String TAG = "MeshCutterGLSurfaceView";

    MeshCutterRenderer renderer;

    private float previousX;
    private float previousY;

    public Utils.STATE state;

    public MeshCutterGLSurfaceView(Context context) {
        super(context);

        renderer = new MeshCutterRenderer(context);
        this.setRenderer(renderer);

        this.requestFocus();
        this.setFocusableInTouchMode(true);

        state = Utils.STATE.CUT;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent evt) {
        float currentX = evt.getX();
        float currentY = evt.getY();
        float deltaX, deltaY;

        if(state == Utils.STATE.CUT)
            switch (evt.getAction()) {
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_DOWN:
                    Utils.Ray ray = Utils.getRay(currentX, currentY, renderer.mg, renderer.vp);
                    renderer.rayEndPoints.add(Utils.getRayEndPoints(ray));
                    renderer.hitPoints.addAll(Utils.computeHitPoints(ray, renderer.shapes));
                    break;
                case MotionEvent.ACTION_UP:
                    state = Utils.STATE.AFTER_CUT;
                    // TODO needs further checks
                    renderer.slPoints = Utils.computeSliceLinePoints(renderer.hitPoints);
            }

        else switch (evt.getAction()) {
                case MotionEvent.ACTION_MOVE:
                deltaX = currentX - previousX;
                deltaY = currentY - previousY;
                renderer.angleX += deltaY * Utils.TOUCH_SCALE_FACTOR;
                renderer.angleY += deltaX * Utils.TOUCH_SCALE_FACTOR;
            }

        previousX = currentX;
        previousY = currentY;
        return true;
    }
}
