package com.example.real_timeedgedetectionviewer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 1001;

    private GLView glView;
    private GLSurfaceView glSurfaceView;
    private GLRenderer glRenderer;
    private CameraHelper cameraHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");

        // Use a FrameLayout to hold both views
        FrameLayout frameLayout = new FrameLayout(this);
        setContentView(frameLayout);

        // Create GLSurfaceView and GLRenderer
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        glRenderer = new GLRenderer();
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // Create GLView (TextureView subclass)
        glView = new GLView(this);

        // Add both views to layout, GLView on top (or adjust as needed)
        frameLayout.addView(glSurfaceView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        frameLayout.addView(glView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Setup GLRenderer listener for SurfaceTexture ready
        glRenderer.setSurfaceTextureReadyListener(surfaceTexture -> {
            Log.d(TAG, "SurfaceTexture from GLRenderer is ready");

            // ✅ Safe and correct placement
            surfaceTexture.setOnFrameAvailableListener(glRenderer.getFrameAvailableListener(glSurfaceView));

            // Pass SurfaceTexture to CameraHelper for preview
            if (cameraHelper != null) {
                cameraHelper.startCameraPreview(surfaceTexture);
            }
        });

        // Initialize camera helper
        cameraHelper = new CameraHelper(this);

        // Request Camera permission if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission not granted, requesting...");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            Log.d(TAG, "Camera permission already granted");
            // Camera preview will start when SurfaceTexture is ready
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        glSurfaceView.onPause();
        if (cameraHelper != null) {
            cameraHelper.stopCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted by user");
                if (glRenderer.getSurfaceTexture() != null) {
                    cameraHelper.startCameraPreview(glRenderer.getSurfaceTexture());
                }
            } else {
                Log.e(TAG, "Camera permission denied by user");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
