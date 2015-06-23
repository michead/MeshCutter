package com.michead.meshcutter2;

import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Simone on 6/23/2015.
 */
public class Utils {

    public static final String TAG = "Utils";

    public static final int BYTES_PER_FLOAT = 4;
    public static final float TOUCH_SCALE_FACTOR = 180.f / 320.f;
    public static final float Z_DISTANCE = 6.f;
    public static final float NORMAL_SCALE_FACTOR = 0.5f;
    public static final float RAY_LENGTH = 10.f;

    public enum STATE{ CUT, AFTER_CUT };

    public static class Ray {
        float[] origin;
        float[] direction;

        public Ray(float[] origin, float[] direction){
            this.origin = origin;
            this.direction = direction;
        }
    };

    public static float[] computeNormals(float[] vertices, byte[] indices){
        float[] normals = new float[vertices.length];

        for(int i = 0; i < vertices.length; i += 3){
            for(int j = 0; j < indices.length; j += 3) {
                if(indices[j] == i / 3 || indices[j + 1] == i / 3 || indices[j + 2] == i / 3){
                    float[] ab = new float[]{
                            vertices[indices[j + 1] * 3] - vertices[indices[j] * 3],
                            vertices[indices[j + 1] * 3 + 1] - vertices[indices[j] * 3 + 1],
                            vertices[indices[j + 1] * 3 + 2] - vertices[indices[j] * 3 + 2]
                    };

                    float[] ac = new float[]{
                            vertices[indices[j + 2] * 3] - vertices[indices[j] * 3],
                            vertices[indices[j + 2] * 3 + 1] - vertices[indices[j] * 3 + 1],
                            vertices[indices[j + 2] * 3 + 2] - vertices[indices[j] * 3 + 2]
                    };

                    float[] cpn = crossProductNormalized(ab, ac);

                    normals[i] += cpn[0];
                    normals[i + 1] += cpn[1];
                    normals[i + 2] += cpn[2];
                }
            }

            float vectorLen =
                    (float)Math.sqrt(normals[i] * normals[i] +
                                    normals[i + 1] * normals[i + 1] +
                                    normals[i + 2] * normals[i + 2]);

            normals[i] /= vectorLen;
            normals[i + 1] /= vectorLen;
            normals[i + 2] /= vectorLen;
        }

        return normals;
    }

    public static float[] crossProductNormalized(float[] a, float[] b){
        return vectorNormalized(new float[]
                {
                        a[1] * b[2] - a[2] * b[1],
                        a[2] * b[0] - a[0] * b[2],
                        a[0] * b[1] - a[1] * b[0]
                });
    }

    public static float[] vectorNormalized(float[] v){
        float vectorLen =
                (float)Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);

        v[0] /= vectorLen;
        v[1] /= vectorLen;
        v[2] /= vectorLen;

        return v;
    }

    public static Ray getRay(float x, float y, MatrixGrabber mg, int[] vp){
        float[] temp = new float[4];
        float[] temp2 = new float[4];

        float[] near = new float[3];
        float[] far = new float[3];

        int result = GLU.gluUnProject(x, vp[3] - y, 0.f, mg.mModelView, 0, mg.mProjection, 0, vp, 0, temp, 0);

        Matrix.multiplyMV(temp2, 0, mg.mModelView, 0, temp, 0);
        if(result == GL10.GL_TRUE){
            near[0] = temp2[0] / temp2[3];
            near[1] = temp2[1] / temp2[3];
            near[2] = temp2[2] / temp2[3];

        }

        result = GLU.gluUnProject(x, vp[3] - y, 1.f, mg.mModelView, 0, mg.mProjection, 0, vp, 0, temp, 0);

        Matrix.multiplyMV(temp2,0,mg.mModelView, 0, temp, 0);
        if(result == GL10.GL_TRUE){
            far[0] = temp2[0] / temp2[3];
            far[1] = temp2[1] / temp2[3];
            far[2] = temp2[2] / temp2[3];
        }

        float[] dir = vectorNormalized(new float[] {far[0] - near[0], far[1] - near[1], far[2] - near[2]});

        return new Ray(near, dir);
    }

    public static float[] getRayEndPoints(Ray ray){
        float[] res = new float[6];

        res[0] = ray.origin[0];
        res[1] = ray.origin[1];
        res[2] = ray.origin[2];
        res[3] = ray.origin[0] + ray.direction[0] * RAY_LENGTH;
        res[4] = ray.origin[1] + ray.direction[1] * RAY_LENGTH;
        res[5] = ray.origin[2] + ray.direction[2] * RAY_LENGTH;

        Log.d(TAG, "Ray: (" + res[0] + ", " + res[1] + ", " + res[2] + ")  (" + res[3] + ", " + res[4] + ", " + res[5] + ")");

        return res;
    }

    public static void drawRays(GL10 gl, final List<float[]> endPoints){
        gl.glDisable(GL10.GL_LIGHTING);

        gl.glColor4f(0.f, 1.f, 0.f, 1.f);

        byte[] ni = new byte[]{0, 1};

        ByteBuffer niBuffer = ByteBuffer.allocateDirect(2);
        niBuffer.put(ni);
        niBuffer.position(0);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        List<float[]> temp = new ArrayList<float[]>(){{ this.addAll(endPoints); }};

        for (float[] ep : temp){
            ByteBuffer epb = ByteBuffer.allocateDirect(ep.length * Utils.BYTES_PER_FLOAT);
            epb.order(ByteOrder.nativeOrder());
            FloatBuffer epBuffer = epb.asFloatBuffer();
            epBuffer.put(ep);
            epBuffer.position(0);

            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, epBuffer);

            gl.glDrawElements(GL10.GL_LINES, 2, GL10.GL_UNSIGNED_BYTE, niBuffer);
        }

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glEnable(GL10.GL_LIGHTING);
    }
}
