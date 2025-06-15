package com.example.real_timeedgedetectionviewer;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "GLRenderer";

    private SurfaceTexture surfaceTexture;
    private int textureId;

    private int shaderProgram;
    private FloatBuffer vertexBuffer;

    private int surfaceWidth;
    private int surfaceHeight;

    // Quad vertex data: (x, y, z, u, v)
    private final float[] vertexData = {
            -1f, -1f, 0f, 1f, 1f,
            1f, -1f, 0f, 1f, 0f,
            -1f,  1f, 0f, 0f, 1f,
            1f,  1f, 0f, 0f, 0f
    };

    public interface SurfaceTextureReadyListener {
        void onSurfaceTextureReady(SurfaceTexture surfaceTexture);
    }

    private SurfaceTextureReadyListener surfaceTextureReadyListener;

    public void setSurfaceTextureReadyListener(SurfaceTextureReadyListener listener) {
        this.surfaceTextureReadyListener = listener;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        surfaceTexture = new SurfaceTexture(textureId);
        if (surfaceTextureReadyListener != null) {
            surfaceTextureReadyListener.onSurfaceTextureReady(surfaceTexture);
            Log.d(TAG, "SurfaceTexture ready and listener notified.");
        }

        setupShader();
        setupVertexBuffer();

        GLES20.glClearColor(0f, 0f, 0f, 1f);
    }

    private void setupVertexBuffer() {
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexData.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexData);
        vertexBuffer.position(0);
    }

    private void setupShader() {
        String vertexShaderCode =
                "attribute vec4 aPosition;" +
                        "attribute vec2 aTexCoord;" +
                        "varying vec2 vTexCoord;" +
                        "void main() {" +
                        "  gl_Position = aPosition;" +
                        "  vTexCoord = aTexCoord;" +
                        "}";

        String fragmentShaderCode =
                "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "varying vec2 vTexCoord;\n" +
                        "uniform vec2 texelSize;\n" +

                        "void main() {\n" +
                        "    float kernelX[9];\n" +
                        "    float kernelY[9];\n" +

                        "    kernelX[0] = -1.0; kernelX[1] = 0.0; kernelX[2] = 1.0;\n" +
                        "    kernelX[3] = -2.0; kernelX[4] = 0.0; kernelX[5] = 2.0;\n" +
                        "    kernelX[6] = -1.0; kernelX[7] = 0.0; kernelX[8] = 1.0;\n" +

                        "    kernelY[0] = -1.0; kernelY[1] = -2.0; kernelY[2] = -1.0;\n" +
                        "    kernelY[3] = 0.0;  kernelY[4] = 0.0;  kernelY[5] = 0.0;\n" +
                        "    kernelY[6] = 1.0;  kernelY[7] = 2.0;  kernelY[8] = 1.0;\n" +

                        "    float sumX = 0.0;\n" +
                        "    float sumY = 0.0;\n" +

                        "    int index = 0;\n" +
                        "    for(int i = -1; i <= 1; i++) {\n" +
                        "        for(int j = -1; j <= 1; j++) {\n" +
                        "            vec4 color = texture2D(sTexture, vTexCoord + vec2(float(j), float(i)) * texelSize);\n" +
                        "            float intensity = (color.r + color.g + color.b) / 3.0;\n" +
                        "            sumX += intensity * kernelX[index];\n" +
                        "            sumY += intensity * kernelY[index];\n" +
                        "            index++;\n" +
                        "        }\n" +
                        "    }\n" +

                        "    float edge = length(vec2(sumX, sumY));\n" +

                        "    gl_FragColor = vec4(vec3(edge), 1.0);\n" +
                        "}";

        int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);
        GLES20.glLinkProgram(shaderProgram);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            Log.e(TAG, "Shader program linking failed: " + GLES20.glGetProgramInfoLog(shaderProgram));
        } else {
            Log.d(TAG, "Shader program linked successfully.");
        }
    }

    private int compileShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Shader compile error: " + GLES20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: " + width + "x" + height);
        surfaceWidth = width;
        surfaceHeight = height;
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (surfaceTexture != null) {
            surfaceTexture.updateTexImage();
        }

        GLES20.glUseProgram(shaderProgram);

        int stride = 5 * 4;

        int positionHandle = GLES20.glGetAttribLocation(shaderProgram, "aPosition");
        int texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "aTexCoord");
        int texelSizeHandle = GLES20.glGetUniformLocation(shaderProgram, "texelSize");

        GLES20.glUniform2f(texelSizeHandle, 1.0f / surfaceWidth, 1.0f / surfaceHeight);

        vertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, stride, vertexBuffer);

        vertexBuffer.position(3);
        GLES20.glEnableVertexAttribArray(texCoordHandle);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, stride, vertexBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        int textureUniform = GLES20.glGetUniformLocation(shaderProgram, "sTexture");
        GLES20.glUniform1i(textureUniform, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public SurfaceTexture.OnFrameAvailableListener getFrameAvailableListener(final GLSurfaceView glSurfaceView) {
        return surfaceTexture -> glSurfaceView.requestRender();
    }

    public int getTextureId() {
        return textureId;
    }
}
