package com.michead.meshcutter2;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.opengl.GLSurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";

    private MeshCutterGLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        glSurfaceView = new MeshCutterGLSurfaceView(this);

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs1 = configurationInfo.reqGlEsVersion >= 0x10000;

        if(!supportsEs1){
            Toast.makeText(this, "This device doesn't support OpenGL ES", Toast.LENGTH_SHORT).show();
            return;
        }

        glSurfaceView.setGLWrapper(new GLSurfaceView.GLWrapper() {
            public GL wrap(GL gl) {
                return new MatrixTrackingGL(gl);
            }
        });

        setContentView(glSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean("wireframe", glSurfaceView.renderer.wireframe);
        editor.putBoolean("rays", glSurfaceView.renderer.drawRays);
        editor.putBoolean("normals", glSurfaceView.renderer.drawNormals);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        boolean savedWireframe =
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean("wireframe", false);
        boolean savedNormals =
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean("normals", false);
        boolean savedRays =
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean("rays", false);

        menu.getItem(1).setChecked(savedWireframe);
        menu.getItem(2).setChecked(savedNormals);
        menu.getItem(3).setChecked(savedRays);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        item.setChecked(!item.isChecked());

        if (id == R.id.wireframe)
            glSurfaceView.renderer.wireframe = !glSurfaceView.renderer.wireframe;
        else if (id == R.id.normals)
            glSurfaceView.renderer.drawNormals = !glSurfaceView.renderer.drawNormals;
        else if (id == R.id.rays)
            glSurfaceView.renderer.drawRays = !glSurfaceView.renderer.drawRays;
        else if (id == R.id.refresh) refresh();

        return super.onOptionsItemSelected(item);
    }

    public void refresh(){
        glSurfaceView.renderer.shapes = new ArrayList<Shape3D>(){{
            this.add(new Shape3D());
        }};

        glSurfaceView.state = Utils.STATE.CUT;

        glSurfaceView.renderer.angleX = 0;
        glSurfaceView.renderer.angleY = 0;

        glSurfaceView.renderer.rayEndPoints.clear();
        glSurfaceView.renderer.hitPoints.clear();
        glSurfaceView.renderer.slPoints = null;
    }
}
