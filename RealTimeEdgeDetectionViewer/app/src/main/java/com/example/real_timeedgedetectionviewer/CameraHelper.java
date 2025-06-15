package com.example.real_timeedgedetectionviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.util.Collections;

public class CameraHelper {

    private static final String TAG = "CameraHelper";

    private final Context context;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private Size previewSize;

    public CameraHelper(Context context) {
        this.context = context;
    }

    @SuppressLint("MissingPermission") // Permission should be checked before calling this
    public void startCameraPreview(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "startCameraPreview called");

        startBackgroundThread();

        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = null;
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id;
                    break;
                }
            }

            if (cameraId == null) {
                Log.e(TAG, "No back-facing camera found");
                return;
            }

            // Configure SurfaceTexture buffer size for preview size (use 1920x1080 or other)
            previewSize = new Size(1920, 1080); // You can choose or get from camera characteristics
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    Log.d(TAG, "Camera opened");
                    cameraDevice = camera;
                    createCameraPreviewSession(surfaceTexture);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.e(TAG, "Camera disconnected");
                    camera.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e(TAG, "Camera error: " + error);
                    camera.close();
                    cameraDevice = null;
                }
            }, backgroundHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "CameraAccessException: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createCameraPreviewSession(SurfaceTexture surfaceTexture) {
        try {
            Log.d(TAG, "createCameraPreviewSession");

            Surface previewSurface = new Surface(surfaceTexture);

            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            Log.d(TAG, "Camera capture session configured");
                            if (cameraDevice == null) {
                                Log.e(TAG, "Camera device is null in session configured");
                                return;
                            }
                            captureSession = session;

                            try {
                                previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

                                captureSession.setRepeatingRequest(previewRequestBuilder.build(),
                                        null, backgroundHandler);

                            } catch (CameraAccessException e) {
                                Log.e(TAG, "CameraAccessException in setRepeatingRequest: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG, "Failed to configure camera capture session");
                        }
                    }, backgroundHandler);

        } catch (CameraAccessException e) {
            Log.e(TAG, "CameraAccessException in createCameraPreviewSession: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopCamera() {
        Log.d(TAG, "stopCamera called");
        try {
            if (captureSession != null) {
                captureSession.close();
                captureSession = null;
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            stopBackgroundThread();
        } catch (Exception e) {
            Log.e(TAG, "Exception in stopCamera: " + e.getMessage());
        }
    }

    private void startBackgroundThread() {
        Log.d(TAG, "startBackgroundThread");
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        Log.d(TAG, "stopBackgroundThread");
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException in stopBackgroundThread: " + e.getMessage());
            }
        }
    }
}
