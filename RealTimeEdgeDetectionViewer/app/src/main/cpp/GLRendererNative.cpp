#include <GLES2/gl2.h>
#include <android/log.h>

#define LOG_TAG "GLRendererNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {

void initGL() {
    glClearColor(0.1f, 0.2f, 0.3f, 1.0f);
    LOGI("OpenGL initialized");
}

void renderFrame() {
    glClear(GL_COLOR_BUFFER_BIT);
    LOGI("Rendered frame");
}

}
