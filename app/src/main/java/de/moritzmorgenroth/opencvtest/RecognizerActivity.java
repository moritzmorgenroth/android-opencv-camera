package de.moritzmorgenroth.opencvtest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class RecognizerActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OPENCV::Activity";
    private CameraBridgeViewBase cameraBridgeViewBase;

    public static final String EXTRA_CAN = "extra_can";

    private BaseLoaderCallback _baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // Load ndk built module, as specified in moduleName in build.gradle
                    // after opencv initialization
                    System.loadLibrary("native-lib");
                    cameraBridgeViewBase.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_opencv);

        // Permissions for Android 6+
        ActivityCompat.requestPermissions(RecognizerActivity.this,
                new String[]{Manifest.permission.CAMERA},
                1);

        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.main_surface);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        cameraBridgeViewBase.setMaxFrameSize(2000, 2000);
    }

    @Override
    public void onPause() {
        super.onPause();
        disableCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, _baseLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            _baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(RecognizerActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onDestroy() {
        super.onDestroy();
        disableCamera();
    }

    public void disableCamera() {
        if (cameraBridgeViewBase != null)
            cameraBridgeViewBase.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    private Mat getSample() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.raw.ocrb_sample);
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY);
        return mat;
    }

    private boolean initalized = false;

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if(!initalized){
            nInit(this.getSample().getNativeObjAddr());
            initalized = true;
            Log.d(TAG, "Initialization called");
        }
        Mat original = inputFrame.rgba();

        Mat bw = new Mat();
        Imgproc.cvtColor(original, bw, Imgproc.COLOR_BGRA2GRAY);

        Core.rotate(bw, bw, Core.ROTATE_90_CLOCKWISE);

        Mat result = inputFrame.rgba();
        Core.rotate(result, result, Core.ROTATE_90_CLOCKWISE);

        String resString = "";

        Mat intermediate = new Mat();
        bw.copyTo(intermediate);

        String abc = nRecognize(bw.getNativeObjAddr(), intermediate.getNativeObjAddr());
        Log.d("Taggytag", abc);

        if(!abc.isEmpty()){
            Intent data = new Intent();
            data.putExtra(EXTRA_CAN, abc);
            setResult(RESULT_OK, data);
            finish();
        }
        Mat display = new Mat();
        Imgproc.cvtColor(intermediate, display, Imgproc.COLOR_GRAY2BGR);

        intermediate.release();
        bw.release();
        return display;
    }

    public native String nRecognize(long original, long intermediate);
    public native void nInit(long matAddrReference);
}