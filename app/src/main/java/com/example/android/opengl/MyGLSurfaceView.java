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

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import static com.example.android.opengl.MyGLRenderer.FOVY;

/**
 * A view container where OpenGL ES graphics can be drawn on screen.
 * This view can also be used to capture touch events, such as a user
 * interacting with drawn objects.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    private float mSphereScale = 1.0f;

    private float zoomFactor = 11.0f;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
        mRenderer.setContext(context);
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private MotionEvent.PointerCoords point0 = new MotionEvent.PointerCoords();
    private MotionEvent.PointerCoords point1 = new MotionEvent.PointerCoords();

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getPointerCount()){
            case 1:
                switch(e.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        e.getPointerCoords(0, point0);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_MOVE:
                        e.getPointerCoords(0, point1);
                        setMoveDelta(point0, point1);
                        break;
                }
                break;
            case 2:
                switch (e.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_MOVE:
                        e.getPointerCoords(0, point0);
                        e.getPointerCoords(1, point1);
                        setZoom(point0, point1);
                        break;
                }
        }
        return true;
    }

    public void setZoom(MotionEvent.PointerCoords coordinate0, MotionEvent.PointerCoords coordinate1) {
        float hypotenuse = (float) Math.sqrt(Math.pow(coordinate0.x - coordinate1.x, 2) + Math.pow(coordinate0.y - coordinate1.y, 2));
        float prevmSphereScale = mSphereScale;
        if (hypotenuse > 0.0f) {
            mSphereScale = hypotenuse/ 500.0f;
            if (mSphereScale > 4.0f) {
                mSphereScale = 4.0f;
            } else if (mSphereScale < 0.5f) {
                mSphereScale = 0.5f;
            }
        }
        if(prevmSphereScale<mSphereScale){
            //zoom in
            FOVY-=1;
            zoomFactor+=0.1;
        }
        else{
            //zoom out
            FOVY+=1;
            zoomFactor-=0.1;
        }
        if(FOVY>150){
            FOVY=150;
        }
        if(FOVY<10){
            FOVY=10;
        }
        if(zoomFactor<0.5){
            zoomFactor = 0.5f;
        }
        if(zoomFactor>14.5){
            zoomFactor=14.5f;
        }
    }

    public void setMoveDelta(MotionEvent.PointerCoords pointA, MotionEvent.PointerCoords pointB) {
        mRenderer.mRotationAngle += (pointA.x - pointB.x)/ (25.0f*zoomFactor);
        mRenderer.mAxialTiltAngle -= (pointA.y - pointB.y)/ (25.0f*zoomFactor);
        if(mRenderer.mAxialTiltAngle>90){
            mRenderer.mAxialTiltAngle=90;
        }
        if(mRenderer.mAxialTiltAngle<-90){
            mRenderer.mAxialTiltAngle=-90;
        }
    }

}
