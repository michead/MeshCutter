package com.michead.meshcutter2;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Simone on 6/23/2015.
 */
public class MeshCutterRenderer implements GLSurfaceView.Renderer {

    private Context context;

    public List<Shape3D> shapes;
    public List<float[]> rayEndPoints;

    private float[] lightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};
    private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] lightPosition = {0.0f, 0.0f, 2.0f, 1.0f};

    public boolean wireframe = false;
    public boolean drawNormals = false;

    float angleX = 0.f;
    float angleY = 0.f;

    MatrixGrabber mg;
    int[] vp;

    public MeshCutterRenderer(Context context){
        this.context = context;

        shapes = new ArrayList<>();
        shapes.add(new Shape3D());

        rayEndPoints = new ArrayList<>();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glDisable(GL10.GL_DITHER);

        gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, lightPosition, 0);
        gl.glEnable(GL10.GL_LIGHT1);
        gl.glEnable(GL10.GL_LIGHT0);
        gl.glEnable(GL10.GL_LIGHTING);

        gl.glPointSize(10.f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (height == 0) height = 1;
        float aspect = (float)width / height;

        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        GLU.gluPerspective(gl, 45, aspect, 1f, 10.f);
        GLU.gluLookAt(gl, 0, 0, Utils.Z_DISTANCE, 0.f, 0.f, 0.f, 0.f, 1.f, 0.f);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        mg =  new MatrixGrabber();
        mg.getCurrentState(gl);

        vp = new int[] {0, 0, width, height};
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glLoadIdentity();

        gl.glPushMatrix();

        gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);

        for(Shape3D shape : shapes) shape.draw(gl, wireframe, drawNormals);

        // DEBUG draw rays
        Utils.drawRays(gl, rayEndPoints);

        gl.glPopMatrix();
    }
}
