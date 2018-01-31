package com.willcapo.radartag;

import android.Manifest;
import android.app.Activity;
import android.content.Context;

import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by WillsHome on 4/3/17.
 */

public class CameraActivity extends Activity {
    private static final String TAG = "CameraActivity";
    private Size mPreviewSize;
    private String mCameraId;
    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    setupCamera(width, height);
                    openCamera();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            };

    private CameraDevice mCameraDevice;
    private Handler backgroundHandler;
    private CameraDevice.StateCallback mCameraDeviceStateCallback
            = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            // Toast.makeText(getApplicationContext(), "Camera Opened, Will!", Toast.LENGTH_SHORT).show();

            // https://www.youtube.com/watch?v=xK9fvxGzM1Q 2:04 Part 4
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close(); // *
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close(); // *
            mCameraDevice = null;
        }
    };

    private CaptureRequest mPreviewCaptureRequest;
    private CaptureRequest.Builder mPreviewCaptureRequestBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }
    };

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);

            mPreviewCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewCaptureRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    if (mCameraDevice == null) {
                        return;
                    }
                    try {
                        mPreviewCaptureRequest = mPreviewCaptureRequestBuilder.build();
                        mCameraCaptureSession = session;
                        mCameraCaptureSession.setRepeatingRequest(
                                mPreviewCaptureRequest,
                                mSessionCaptureCallback,
                                null
                        );
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(
                            getApplicationContext(),
                            "create camera session failed!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size getPreferredPreviewSize(Size[] mapSizes, int width, int height) {
        List<Size> collectorSizes = new ArrayList<>();
        for (Size option : mapSizes) {
            if (width > height) {
                if (option.getWidth() > width &&
                        option.getHeight() > height) {
                }
            } else {
                if (option.getWidth() > height &&
                        option.getHeight() > width) {
                    collectorSizes.add(option);
                }
            }
        }
        if (collectorSizes.size() > 0) {
            return Collections.min(collectorSizes, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return mapSizes[0];
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_main);
        mTextureView = (TextureView) findViewById(R.id.textureView);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTextureView.isAvailable()) {

        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    private static final int REQUEST_CAMERA_RESULT = 1;
    private void openCamera() {
        int permissionCheck = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA);
        // Check if permission was Granted
        // Ask for permission, if permission was not granted (probably due to higher api level)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "CAMERA permission GRANTED");
        } else {
            Log.v(TAG, "CAMERA permission NOT - GRANTED");
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
                Toast.makeText(this,"No Permission to use the Camera services", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[] {android.Manifest.permission.CAMERA},REQUEST_CAMERA_RESULT);
        }

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT){
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mPreviewSize = getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            Log.v(TAG, "====CameraAccessException====");
            e.printStackTrace();
        }
    }

}

