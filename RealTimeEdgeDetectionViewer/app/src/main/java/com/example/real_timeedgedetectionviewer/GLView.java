package com.example.real_timeedgedetectionviewer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
public class GLView extends TextureView implements TextureView.SurfaceTextureListener {

    private SurfaceTextureListener externalListener;

    public GLView(Context context) {
        super(context);
        setSurfaceTextureListener(this);
    }

    public void setExternalSurfaceTextureListener(SurfaceTextureListener listener) {
        this.externalListener = listener;
    }

    public SurfaceTextureListener getSurfaceTextureListener() {
        return this;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (externalListener != null) {
            externalListener.onSurfaceTextureAvailable(surface, width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (externalListener != null) {
            return externalListener.onSurfaceTextureDestroyed(surface);
        }
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (externalListener != null) {
            externalListener.onSurfaceTextureSizeChanged(surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (externalListener != null) {
            externalListener.onSurfaceTextureUpdated(surface);
        }
    }
}
