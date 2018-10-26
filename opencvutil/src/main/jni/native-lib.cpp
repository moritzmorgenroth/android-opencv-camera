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
Java_de_moritzmorgenroth_opencvutil_RecognizerActivity_nInit(JNIEnv *env, jobject thiz, jlong ref) {
    Mat mat = *(Mat *) ref;
    opencv_dmz::init(env, thiz, mat);
}
jstring JNICALL
Java_de_moritzmorgenroth_opencvutil_RecognizerActivity_nRecognize(JNIEnv *env, jobject instance, jlong originalAddress, jlong intermediateAddress, jlong resultAddress) {
    Mat original = *(Mat *) originalAddress;
    Mat intermediate = *(Mat *) intermediateAddress;
    Mat result = *(Mat *) resultAddress;

    const char* can = opencv_dmz::can(original, intermediate, result);

    jstring res = (*env).NewStringUTF(can);
    return res;

    //result = original;
}
}