# 📱 Real-Time Edge Detection Viewer
## (Android + OpenCV 4.x + NDK + JNI + OpenGL)

Real-Time Edge Detection Viewer is a high-performance Android application that captures live camera frames, processes them natively using C++ with OpenCV, and displays the result in real time using OpenGL. Built with the Android NDK and JNI, this project highlights efficient native image processing for edge detection on mobile devices.

It demonstrates advanced mobile computer vision techniques, combining native rendering, JNI-based data flow, and OpenCV-powered image filters to deliver fast, low-latency performance — ideal for real-time AI or AR use cases.

## ✅ Features Implemented

- 📷 Real-time camera preview using `GLSurfaceView`
- 🧠 Frame processing done in native C++ using OpenCV
- 🚀 Efficient data transfer between Java and native layers via JNI
- 🔄 Fixed preview orientation and mirroring (front/back camera support)
- 🧪 Placeholder for applying image filters (e.g., grayscale, edge detection)

---

## 📷 Processed Edge Detection Screenshots

=> These images showcase the edge detection results processed by the native OpenCV C++ pipeline, demonstrating the app’s real-time image processing capability:

<table> <tr> <td align="center"> <img src="https://github.com/user-attachments/assets/2a118628-cbf5-4f1a-9d4e-6117998ef513" width="100%" /> <br><b>Screenshot 01</b> </td> <td align="center"> <img src="https://github.com/user-attachments/assets/f986e9ad-b29d-4e09-b910-6ffa48e4093f" width="100%" /> <br><b>Screenshot 02</b> </td> <td align="center"> <img src="https://github.com/user-attachments/assets/6d754a2a-b6f0-4f85-b845-2a5180e0ef2d" width="100%" /> <br><b>Screenshot 03</b> </td> </tr> <tr> <td align="center"> <img src="https://github.com/user-attachments/assets/17dca053-8e2e-4765-a65f-85d679a150a6" width="100%" /> <br><b>Screenshot 04</b> </td> <td align="center"> <img src="https://github.com/user-attachments/assets/70be0d44-b3a4-4005-b739-c8d2b47a26a9" width="100%" /> <br><b>Screenshot 05</b> </td> <td align="center"> <img src="https://github.com/user-attachments/assets/6060e671-6297-428a-a375-b30c53c14925" width="100%" /> <br><b>Screenshot 06</b> </td> </tr> </table>


---

## ⚙️ Setup Instructions

### Prerequisites

- Android Studio (Electric Eel or later recommended)
- Android NDK (version 23 or later)
- OpenCV Android SDK (tested with OpenCV 4.5+)

### Steps to Set Up

1. **Install NDK**  
   In Android Studio:  
   `Preferences > SDK Manager > SDK Tools > NDK (Side by side)`  
   Select and install it.

2. **Download and Link OpenCV**  
   - Download OpenCV Android SDK from:  
     [https://opencv.org/releases](https://opencv.org/releases)  
   - Unzip and copy the `sdk/native` folder to your project's root.
   - Update `CMakeLists.txt` to include OpenCV:

     ```cmake
     set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv/native/jni)
     find_package(OpenCV REQUIRED)
     include_directories(${OpenCV_INCLUDE_DIRS})
     target_link_libraries(native-lib ${OpenCV_LIBS})
     ```

3. **Sync Gradle and Build Project**  
   Build your project to ensure JNI and OpenCV are correctly linked.

---

## 🧠 Architecture and Frame Flow

### 🔄 Data Flow

Java Camera Frame → SurfaceTexture → GLSurfaceView
↓
Frame extracted as ByteBuffer
↓
Sent via JNI → C++ (native-lib.cpp)
↓
OpenCV processes frame (e.g., to grayscale)
↓
Output returned to Java or rendered


### 🧩 Components

- `MainActivity.java`: Initializes camera and OpenGL rendering surface
- `CameraRenderer.java`: Handles texture rendering and frame acquisition
- `native-lib.cpp`: Contains native JNI functions and OpenCV processing
- `CMakeLists.txt`: Configures build pipeline and OpenCV linking

---

## 📁 Directory Structure

├── app/
│ ├── src/
│ │ ├── main/
│ │ │ ├── cpp/ # Native C++ code
│ │ │ │ └── native-lib.cpp
│ │ │ ├── java/
│ │ │ │ ├── ... # Java code (Camera, Renderer)
│ │ │ └── res/ # Layouts and resources
│ ├── CMakeLists.txt # NDK build config
├── opencv/ # OpenCV SDK (native only)
│ └── native/ # JNI build libs
└── README.md


---

## 📌 Notes

- Ensure `externalNativeBuild` in `build.gradle` is configured for CMake.
- Some devices may require camera permission and NDK ABI filters (`armeabi-v7a`, `arm64-v8a`).
- Use `glPixelStorei(GL_UNPACK_ALIGNMENT, 1);` to avoid texture misalignment.

---
