cmake_minimum_required(VERSION 3.4.1)


# add native jni module

add_library(native-lib SHARED src/main/jni/native-lib.cpp)

# compile dmz

add_library(opencv-dmz SHARED src/main/jni/opencv-eid-recognizer/recognizer.cpp)

# compile OpenCV

set(OpenCV_DIR ../opencv/sdk/native/jni)
find_package(OpenCV REQUIRED)

# android ndk modules

find_library(log-lib log)
find_library(dl-lib dl)

# link all together
target_link_libraries(opencv-dmz ${OpenCV_LIBS} ${log-lib} ${dl-lib})
target_link_libraries(native-lib ${opencv-dmz} ${OpenCV_LIBS} ${log-lib} ${dl-lib})
