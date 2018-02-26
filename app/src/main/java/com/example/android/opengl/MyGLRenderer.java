/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.opengl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
    private Sphere mSphere;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mProjectionMatrix = new float[16];
    private float[] textureMatrix = new float[16];
    private float[] pvMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] mvpMatrix = new float[16];
    private float[] modelMatrix = new float[16];

    private ShaderProgram shaderProgram;

    private int aPositionLocation;
    private int uMVPMatrixLocation;
    private int uTextureMatrixLocation;
    private int aTextureCoordLocation;

    private float mAngle;
    private Context mContext;
    private SurfaceTexture surfaceTexture;
    private int textureId = -1;

    private float ratio;

    private Sphere sphere;

    public static float FOVY = 45f;
    public static float mRotationAngle;
    public static float mAxialTiltAngle;

    private static final float Z_NEAR = 1f;
    private static final float Z_FAR = 1000f;
    private static final float INITIAL_PITCH_DEGREES = -90.f;

    public static final int SPHERE_SLICES = 180;
    private static final int SPHERE_INDICES_PER_VERTEX = 1;
    private static final float SPHERE_RADIUS = 500.0f;

    private String vertex_shader="uniform mat4 uMVPMatrix;\n" +
            "uniform mat4 uTextureMatrix;\n" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_Position = uMVPMatrix * aPosition * vec4(1, -1, 1, 1);\n" +
            "    vTextureCoord = (uTextureMatrix * aTextureCoord).xy;\n" +
            "}\n";
    private String fragment_shader= "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D sTexture;\n" +
            "\n" +
            "void main() {\n" +
            "    vec4 color = texture2D(sTexture, vTextureCoord);\n" +
            "    gl_FragColor = color;\n" +
            "}";

    public void setContext(Context context){
        mContext = context;
        this.mRotationAngle = 0.0f;
        this.mAxialTiltAngle = 0.0f;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        ShaderProgramWorks();

    }

    public void ShaderProgramWorks(){
        shaderProgram = new ShaderProgram(vertex_shader, fragment_shader);

        aPositionLocation = shaderProgram.getAttribute("aPosition");
        uMVPMatrixLocation = shaderProgram.getUniform("uMVPMatrix");
        uTextureMatrixLocation = shaderProgram.getUniform("uTextureMatrix");
        aTextureCoordLocation = shaderProgram.getAttribute("aTextureCoord");

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        sphere = new Sphere(SPHERE_SLICES, 0.f, 0.f, 0.f, SPHERE_RADIUS, SPHERE_INDICES_PER_VERTEX);

        GLES20.glUseProgram(shaderProgram.getShaderHandle());

        GLES20.glEnableVertexAttribArray(aPositionLocation);
        checkGlError("glEnableVertexAttribArray");

        int x = sphere.getVerticesStride();

        GLES20.glVertexAttribPointer(aPositionLocation, 3,
                GLES20.GL_FLOAT, false, sphere.getVerticesStride(), sphere.getVertices());

        checkGlError("glVertexAttribPointer");

        GLES20.glEnableVertexAttribArray(aTextureCoordLocation);
        checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(aTextureCoordLocation, 2,
                GLES20.GL_FLOAT, false, sphere.getVerticesStride(),
                sphere.getVertices().duplicate().position(3));
        checkGlError("glVertexAttribPointer");

        GLES20.glUseProgram(shaderProgram.getShaderHandle());

        GLES20.glEnableVertexAttribArray(aPositionLocation);
        checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(aPositionLocation, 3,
                GLES20.GL_FLOAT, false, sphere.getVerticesStride(), sphere.getVertices());

        checkGlError("glVertexAttribPointer");

        GLES20.glEnableVertexAttribArray(aTextureCoordLocation);
        checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(aTextureCoordLocation, 2,
                GLES20.GL_FLOAT, false, sphere.getVerticesStride(),
                sphere.getVertices().duplicate().position(3));
        checkGlError("glVertexAttribPointer");

        textureId = loadTexture(mContext, R.drawable.img);

        surfaceTexture = new SurfaceTexture(textureId);

        surfaceTexture.getTransformMatrix(textureMatrix);

        Matrix.translateM(textureMatrix, 0, 0, 1, 0);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        Matrix.perspectiveM(projectionMatrix, 0, FOVY, ratio, Z_NEAR, Z_FAR);
        Matrix.setIdentityM(viewMatrix, 0);

        Matrix.multiplyMM(pvMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.setRotateM(modelMatrix,0,INITIAL_PITCH_DEGREES,1,0,0);
        Matrix.rotateM(modelMatrix,0,mAxialTiltAngle,1,0,0);
        Matrix.rotateM(modelMatrix,0,mRotationAngle,0,0,1);
        Matrix.multiplyMM(mvpMatrix, 0, pvMatrix, 0, modelMatrix , 0);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glBindTexture(
                GLES20.GL_TEXTURE_2D,
                textureId);

        GLES20.glUniformMatrix4fv(uTextureMatrixLocation, 1, false, textureMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);

        for (int j = 0; j < sphere.getNumIndices().length; ++j) {
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                    sphere.getNumIndices()[j], GLES20.GL_UNSIGNED_SHORT,
                    sphere.getIndices()[j]);
        }

    }

    public int loadTexture(Context context, int texture){
        int externalTextureId = -1;
        int[] textures = new int[1];
        try {
            GLES20.glGenTextures(1, textures, 0);
            externalTextureId = textures[0];
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(
                    GLES20.GL_TEXTURE_2D,
                    externalTextureId);
            GLES20.glTexParameterf(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameterf(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), texture);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        } catch (RuntimeException e) {
            Log.e(TAG, e.toString(), e);
            if (externalTextureId != -1) {
                GLES20.glDeleteTextures(1, textures, 0);
            }
            return -1;
        }
        return externalTextureId;
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Returns the rotation angle of the triangle shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    public float getAngle() {
        return mAngle;
    }

    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    public void setAngle(float angle) {
        mAngle = angle;
    }

    /**
     * Change the rotation angles for the sphere and its position (zoom).
     *
     * @param coordinate0 The first finger coordinate.
     * @param coordinate1 The second finger coordinate.
     */

}