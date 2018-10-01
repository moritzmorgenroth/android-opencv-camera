#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>
#include <cv.hpp>
#include "opencv-eid-recognizer/recognizer.h"

using namespace std;
using namespace cv;

/* This method forms the core of card.io scanning. All others (nCardDetected & nGetFocusScore) */
extern "C" {
    JNIEXPORT void JNICALL Java_de_moritzmorgenroth_opencvtest_ImageProcessor_nProcessPicture(JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray yuv, jintArray bgra)
    {
        opencv_dmz::abc(env, thiz, width, height, yuv, bgra);
    }
    JNIEXPORT void JNICALL Java_de_moritzmorgenroth_opencvtest_MainActivity_nInit(JNIEnv* env, jobject thiz, jint width, jint height, jintArray pixels)
    {
        opencv_dmz::init(env, thiz, pixels, width, height);
    }

}


void log(String text) {
    // TODO Implement
}