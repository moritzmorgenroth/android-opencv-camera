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
JNIEXPORT void JNICALL
Java_de_moritzmorgenroth_opencvtest_RecognizerActivity_nInit(JNIEnv *env, jobject thiz, jlong ref) {
    Mat mat = *(Mat *) ref;
    opencv_dmz::init(env, thiz, mat);
}
jstring JNICALL
Java_de_moritzmorgenroth_opencvtest_RecognizerActivity_nRecognize(JNIEnv *env, jobject instance, jlong originalAddress, jlong intermediateAddress) {
    Mat original = *(Mat *) originalAddress;
    Mat intermediate = *(Mat *) intermediateAddress;

//    const char* can = opencv_dmz::can(original, intermediate, result);
    std::string can = opencv_dmz::can(original, intermediate);

    const char * c = can.c_str();

    // do stuff
    jstring res = (*env).NewStringUTF(c);
    return res;

    //result = original;
}
}