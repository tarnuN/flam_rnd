#include <opencv2/opencv.hpp>
#include <android/log.h>

#define LOG_TAG "ImageProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

void detectEdges(const cv::Mat& src, cv::Mat& dst) {
    if (src.empty()) {
        LOGI("Source image is empty");
        return;
    }

    cv::Mat gray;
    cv::cvtColor(src, gray, cv::COLOR_RGB2GRAY);

    cv::Canny(gray, dst, 100, 200);
    LOGI("Edges detected");
}
