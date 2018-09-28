#include <jni.h>
#include <string>
#include <android/log.h>

#include <jni.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>
#include <cv.hpp>

using namespace std;
using namespace cv;

/* This method forms the core of card.io scanning. All others (nCardDetected & nGetFocusScore) */
extern "C"
JNIEXPORT void JNICALL Java_de_moritzmorgenroth_opencvtest_CameraPreview_nScanFrame(JNIEnv *env, jobject thiz,
                                                                   jbyteArray jb, jobject jCardResultBitmap) {
    __android_log_print(ANDROID_LOG_INFO, "NATIVE", "Processing");

    int width = 500;
    int height = 300;

    Mat image = Mat(height,width,CV_8UC3, jb);

//    jbyte* _yuv = env->GetByteArrayElements(yuv, 0);

//    IplImage *image = cvCreateImageHeader(cvSize(width, height), IPL_DEPTH_8U, 1);
//    jbyte *jBytes = env->GetByteArrayElements(jb, 0);
//    image->imageData = (char *)jBytes;

//
//    cvReleaseImageHeader(&image);
//    env->ReleaseByteArrayElements(jb, jBytes, 0);
}

extern "C" JNIEXPORT jstring JNICALL
Java_de_moritzmorgenroth_opencvtest_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" {
JNIEXPORT void JNICALL Java_de_moritzmorgenroth_opencvtest_CameraPreview_nFindFeatures(JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray yuv, jintArray bgra)
{
    jbyte* _yuv  = env->GetByteArrayElements(yuv, 0);
    jint*  _bgra = env->GetIntArrayElements(bgra, 0);

    Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
    Mat mbgra(height, width, CV_8UC4, (unsigned char *)_bgra);
    Mat mgray(height, width, CV_8UC1, (unsigned char *)_yuv);

    __android_log_print(ANDROID_LOG_INFO, "NATIVE", "Dimens: %i , %i ", myuv.rows , myuv.cols);

    //Please make attention about BGRA byte order
    //ARGB stored in java as int array becomes BGRA at native level
    cvtColor(myuv, mbgra, CV_YUV420sp2BGR, 4);

    vector<KeyPoint> v;

//    FastFeatureDetector* detector = FastFeatureDetector::create(50);
//    detector->detect(mgray, v);

    Ptr<FastFeatureDetector> detector=FastFeatureDetector::create();
    vector<Mat> descriptor;

    detector->detect(mgray,v);
//    drawKeypoints(mbgra, v, mbgra);


    for( size_t i = 0; i < v.size(); i++ )
        circle(mbgra, Point(v[i].pt.x, v[i].pt.y), 10, Scalar(0,0,255,255));

    env->ReleaseIntArrayElements(bgra, _bgra, 0);
    env->ReleaseByteArrayElements(yuv, _yuv, 0);
}

}