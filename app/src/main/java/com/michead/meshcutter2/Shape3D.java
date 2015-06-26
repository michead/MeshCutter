package com.michead.meshcutter2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Simone on 6/23/2015.
 */
public class Shape3D {

    private static final String TAG = "Shape3D";

    private FloatBuffer vertexBuffer;
    private FloatBuffer normalBuffer;
    private ByteBuffer indexBuffer;

    public float[] vertices;
    public float[] normals;
    public byte[] indices;
    public float[] color;

    private float[] defaultVertices = new float[]{
        -1.f, -1.f, 1.f,
        1.f, -1.f, 1.f,
        -1.f, 1.f, 1.f,
        1.f, 1.f, 1.f,
        -1.f, -1.f, -1.f,
        1.f, -1.f, -1.f,
        -1.f, 1.f, -1.f,
        1.f, 1.f, -1.f
    };

    private byte[] defaultIndices = new byte[]{
        0, 1, 2,
        1, 3, 2,
        4, 7, 5,
        4, 6, 7,
        0, 6, 4,
        0, 2, 6,
        1, 5, 3,
        5, 7, 3,
        0, 5, 1,
        0, 4, 5,
        2, 3, 6,
        3, 7, 6
    };

    private float[] defaultColor = new float[]{1.f, 1.f, 1.f, 1.f};

    public Shape3D(){
        this.vertices = defaultVertices;
        this.indices = defaultIndices;
        this.color = defaultColor;

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * Utils.BYTES_PER_FLOAT);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        normals = Utils.computeNormals(vertices, indices);
        ByteBuffer nbb = ByteBuffer.allocateDirect(vertices.length * Utils.BYTES_PER_FLOAT);
        nbb.order(ByteOrder.nativeOrder());
        normalBuffer = nbb.asFloatBuffer();
        normalBuffer.put(normals);
        normalBuffer.position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.length);
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    public Shape3D(float[] vertices, byte[] indices){
        this.vertices = vertices;
        this.indices = indices;
        this.color = defaultColor;

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * Utils.BYTES_PER_FLOAT);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        normals = Utils.computeNormals(vertices, indices);
        ByteBuffer nbb = ByteBuffer.allocateDirect(vertices.length * Utils.BYTES_PER_FLOAT);
        nbb.order(ByteOrder.nativeOrder());
        normalBuffer = nbb.asFloatBuffer();
        normalBuffer.put(normals);
        normalBuffer.position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.length);
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    public Shape3D(float[] vertices, byte[] indices, float[] color){
        this.vertices = vertices;
        this.indices = indices;
        this.color = color;

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * Utils.BYTES_PER_FLOAT);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        normals = Utils.computeNormals(vertices, indices);
        ByteBuffer nbb = ByteBuffer.allocateDirect(vertices.length * Utils.BYTES_PER_FLOAT);
        nbb.order(ByteOrder.nativeOrder());
        normalBuffer = nbb.asFloatBuffer();
        normalBuffer.put(normals);
        normalBuffer.position(0);

        indexBuffer = ByteBuffer.allocateDirect(indices.length);
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    public void draw(GL10 gl, boolean wireframe, boolean drawNormals){
        gl.glFrontFace(GL10.GL_CCW);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glCullFace(GL10.GL_BACK);

        gl.glColor4f(color[0], color[1], color[2], color[3]);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);

        if(wireframe){
            gl.glDisable(GL10.GL_LIGHTING);

            gl.glDrawElements(GL10.GL_POINTS, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer.rewind());
            for(int i = 0; i < indices.length; i += 3)
                gl.glDrawElements(GL10.GL_LINE_LOOP, 3, GL10.GL_UNSIGNED_BYTE, indexBuffer.rewind().position(i));

            gl.glEnable(GL10.GL_LIGHTING);
        }
        else gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer.rewind());

        if(drawNormals){
            gl.glDisable(GL10.GL_LIGHTING);

            gl.glColor4f(0.f, 0.f, 1.f, 1.f);

            byte[] ni = new byte[]{0, 1};

            ByteBuffer niBuffer = ByteBuffer.allocateDirect(2);
            niBuffer.put(ni);
            niBuffer.position(0);

            for(int i = 0; i < normals.length; i += 3){
                float[] nv = new float[]{
                    vertices[i],
                    vertices[i + 1],
                    vertices[i + 2],
                    vertices[i] + normals[i] * Utils.NORMAL_SCALE_FACTOR,
                    vertices[i + 1] + normals[i + 1] * Utils.NORMAL_SCALE_FACTOR,
                    vertices[i + 2] + normals[i + 2] * Utils.NORMAL_SCALE_FACTOR
                };

                ByteBuffer nbb = ByteBuffer.allocateDirect(nv.length * Utils.BYTES_PER_FLOAT);
                nbb.order(ByteOrder.nativeOrder());
                FloatBuffer nvBuffer = nbb.asFloatBuffer();
                nvBuffer.put(nv);
                nvBuffer.position(0);

                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, nvBuffer);

                gl.glDrawElements(GL10.GL_LINES, 2, GL10.GL_UNSIGNED_BYTE, niBuffer);
            }

            gl.glEnable(GL10.GL_LIGHTING);
        }

        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisable(GL10.GL_CULL_FACE);
    }
}
