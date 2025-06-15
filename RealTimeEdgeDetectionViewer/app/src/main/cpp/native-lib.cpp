#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/opencv.hpp>

#define LOG_TAG "native-lib"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL
Java_com_example_real_1timeedgedetectionviewer_MainActivity_processFrame(
        JNIEnv* env,
        jobject /* this */,
        jbyteArray nv21,
        jint width,
        jint height) {

    jbyte* yuvData = env->GetByteArrayElements(nv21, nullptr);
    if (yuvData == nullptr) {
        LOGE("Failed to get YUV data from byte array");
        return;
    }

    cv::Mat yuvImg(height + height / 2, width, CV_8UC1, yuvData);
    cv::Mat rgbImg;
    cv::cvtColor(yuvImg, rgbImg, cv::COLOR_YUV2RGB_NV21);

    cv::Mat edges;
    cv::Canny(rgbImg, edges, 100, 200);

    LOGI("Processed frame with size: %d x %d", width, height);

    env->ReleaseByteArrayElements(nv21, yuvData, 0);
}
