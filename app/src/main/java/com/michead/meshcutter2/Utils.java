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

    private static final String TAG = "Utils";

    public static final int BYTES_PER_FLOAT = 4;
    public static final float TOUCH_SCALE_FACTOR = 180.f / 320.f;
    public static final float Z_DISTANCE = 6.f;
    public static final float NORMAL_SCALE_FACTOR = 0.5f;
    public static final float RAY_LENGTH = 10.f;
    public static final float UPPERMOST_Y_VALUE = 1.f;
    public static final float BOTTOMMOST_Y_VALUE = -1.f;
    public static final float FRONTMOST_Z_VALUE = 1.f;
    public static final float BACKMOST_Z_VALUE = -1.f;

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

    public static List<float[]> computeHitPoints(Ray ray, List<Shape3D> shapes){
        List<float[]> res = new ArrayList<>();

        for(Shape3D shape : shapes){
            for(int i = 0; i < shape.indices.length; i += 3){
                float[] hitPoint = checkIntersection(
                        ray,
                        shape.vertices[shape.indices[i] * 3],
                        shape.vertices[shape.indices[i] * 3 + 1],
                        shape.vertices[shape.indices[i] * 3 + 2],
                        shape.vertices[shape.indices[i + 1] * 3],
                        shape.vertices[shape.indices[i + 1] * 3 + 1],
                        shape.vertices[shape.indices[i + 1] * 3 + 2],
                        shape.vertices[shape.indices[i + 2] * 3],
                        shape.vertices[shape.indices[i + 2] * 3 + 1],
                        shape.vertices[shape.indices[i + 2] * 3 + 2]
                );

                if(hitPoint != null) res.add(hitPoint);
            }
        }

        return res;
    }

    public static float[] checkIntersection(Ray ray,
                                            float x1, float y1, float z1,
                                            float x2, float y2, float z2,
                                            float x3, float y3, float z3){

        float[] matA = new float[]{
                x1 - x2, x1 - x3, ray.direction[0],
                y1 - y2, y1 - y3, ray.direction[1],
                z1 - z2, z1 - z3, ray.direction[2]
        };

        float detA = determinant33(matA);

        float[] matB = new float[]{
                x1 - ray.origin[0], x1 - x3, ray.direction[0],
                y1 - ray.origin[1], y1 - y3, ray.direction[1],
                z1 - ray.origin[2], z1 - z3, ray.direction[2]
        };

        float[] matC = new float[]{
                x1 - x2, x1 - ray.origin[0], ray.direction[0],
                y1 - y2, y1 - ray.origin[1], ray.direction[1],
                z1 - z2, z1 - ray.origin[2], ray.direction[2]
        };

        float[] matT = new float[]{
                x1 - x2, x1 - x3, x1 - ray.origin[0],
                y1 - y2, y1 - y3, y1 - ray.origin[1],
                z1 - z2, z1 - z3, z1 - ray.origin[2]
        };

        float beta = determinant33(matB) / detA;
        float gamma = determinant33(matC) / detA;
        float alpha = 1 - beta - gamma;
        float t = determinant33(matT) / detA;

        if (beta + gamma <= 1 && beta >= 0 && gamma >= 0 && t >= 0)
            return new float[]{
                    ray.origin[0] + ray.direction[0] * t,
                    ray.origin[1] + ray.direction[1] * t,
                    ray.origin[2] + ray.direction[2] * t,
            };

        return null;
    }

    public static float determinant33(float[] mat){
        float det =
                mat[0] * (mat[4] * mat[8] - mat[5] * mat[7]) -
                mat[1] * (mat[3] * mat[8] - mat[5] * mat[6]) +
                mat[2] * (mat[3] * mat[7] - mat[4] * mat[6]);

        return det;
    }

    public static void drawHitPoints(GL10 gl, final List<float[]> hitPoints){
        gl.glDisable(GL10.GL_LIGHTING);

        gl.glColor4f(1.f, 0.f, 0.f, 1.f);


        byte[] ni = new byte[]{0};

        ByteBuffer niBuffer = ByteBuffer.allocateDirect(1);
        niBuffer.put(ni);
        niBuffer.position(0);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        List<float[]> temp = new ArrayList<float[]>(){{ this.addAll(hitPoints); }};

        for (float[] ep : temp){
            ByteBuffer epb = ByteBuffer.allocateDirect(ep.length * Utils.BYTES_PER_FLOAT);
            epb.order(ByteOrder.nativeOrder());
            FloatBuffer epBuffer = epb.asFloatBuffer();
            epBuffer.put(ep);
            epBuffer.position(0);

            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, epBuffer);

            gl.glDrawElements(GL10.GL_POINTS, 1, GL10.GL_UNSIGNED_BYTE, niBuffer);
        }

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glEnable(GL10.GL_LIGHTING);
    }

    public static float[] computeSliceLinePoints(List<float[]> points){
        int fb = -1, fu = -1, bb = -1, bu = -1;

        float fb_ = 1.f, fu_ = -1.f, bb_ = 1.f, bu_ = -1.f;

        float[] points_array = new float[points.size() * 3];
        for(int i = 0; i < points.size(); i++)
        {
            points_array[i * 3] = points.get(i)[0];
            points_array[i * 3 + 1] = points.get(i)[1];
            points_array[i * 3 + 2] = points.get(i)[2];
        }

        for(int i = 0; i < points_array.length; i += 3)
        {
            if(Math.abs(points_array[i + 2] - 1.f) < 0.1f){
                points_array[i + 2] = 1.f;
                if(points_array[i + 1] > fu_)
                {
                    fu = i;
                    fu_ = points_array[i + 1];
                }
                if(points_array[i + 1] < fb_)
                {
                    fb = i;
                    fb_ = points_array[i + 1];
                }
            }
            else if(Math.abs(points_array[i + 2] + 1.f) < 0.1f){
                points_array[i + 2] = -1.f;
                if(points_array[i + 1] > bu_)
                {
                    bu = i;
                    bu_ = points_array[i + 1];
                }
                if(points_array[i + 1] < bb_)
                {
                    bb = i;
                    bb_ = points_array[i + 1];
                }
            }
        }

        for(int i = 0; i < points_array.length; i += 3)
            Log.d(TAG, points_array[i] + " " + points_array[i + 1] + " " + points_array[i + 2]);

        points_array[fu + 1] = UPPERMOST_Y_VALUE;
        points_array[fb + 1] = BOTTOMMOST_Y_VALUE;

        points_array[bu + 1] = UPPERMOST_Y_VALUE;
        points_array[bb + 1] = BOTTOMMOST_Y_VALUE;


        Log.d(TAG, fu / 3 + " " + fb / 3 + " " + bu / 3 + " " + bb / 3);

        return points_array;
    }

    public static void drawLineAndPoints(GL10 gl, float[] points){
        gl.glDisable(GL10.GL_LIGHTING);
        gl.glColor4f(0.f, 0.f, 1.f, 1.f);
        gl.glLineWidth(3.f);

        byte[] indices = new byte[points.length / 3];
        for(byte i = 0; i < indices.length; i++) indices[i] = i;

        ByteBuffer vbb = ByteBuffer.allocateDirect(points.length * Utils.BYTES_PER_FLOAT);
        vbb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(points);
        vertexBuffer.position(0);

        ByteBuffer indexBuffer = ByteBuffer.allocateDirect(indices.length);
        indexBuffer.put(indices);
        indexBuffer.position(0);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

        gl.glDrawElements(GL10.GL_POINTS, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);
        gl.glDrawElements(GL10.GL_LINE_STRIP, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer.position(0));

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glColor4f(1.f, 1.f, 1.f, 1.f);
        gl.glLineWidth(1.f);
        gl.glEnable(GL10.GL_LIGHTING);
    }
}
