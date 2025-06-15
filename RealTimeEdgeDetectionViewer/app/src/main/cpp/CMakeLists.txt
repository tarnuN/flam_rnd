cmake_minimum_required(VERSION 3.10.2)
project("real_timeedgedetectionviewer")

# Set paths to OpenCV include and libs (adjust these relative paths if needed)
set(OpenCV_INCLUDE_DIR "${CMAKE_SOURCE_DIR}/../../../../opencv/sdk/native/jni/include")
set(OpenCV_LIBS_DIR "${CMAKE_SOURCE_DIR}/../../../../opencv/sdk/native/libs")

# Include OpenCV headers
include_directories(${OpenCV_INCLUDE_DIR})

# Add native source files for your native-lib shared library
add_library(native-lib SHARED
        native-lib.cpp
        GLRendererNative.cpp
        ImageProcessor.cpp
        )

# Import OpenCV shared library
add_library(libopencv_java4 SHARED IMPORTED)
set_target_properties(libopencv_java4 PROPERTIES
        IMPORTED_LOCATION "${OpenCV_LIBS_DIR}/${ANDROID_ABI}/libopencv_java4.so"
        )

# Find system libraries needed
find_library(log-lib log)
find_library(android-lib android)
find_library(gles-lib GLESv2)

# Link native-lib with OpenCV and system libraries
target_link_libraries(native-lib
        libopencv_java4
        ${log-lib}
        ${android-lib}
        ${gles-lib}
        )
