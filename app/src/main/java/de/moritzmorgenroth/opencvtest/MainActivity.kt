package de.moritzmorgenroth.opencvtest

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import de.moritzmorgenroth.opencvtest.camera2.Camera2PreviewFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var mCamera2PreviewFragment: Camera2PreviewFragment
    private lateinit var mProcessingResultView: ProcessingResultView

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initNative()

        mProcessingResultView = ProcessingResultView(this)
        mCamera2PreviewFragment = Camera2PreviewFragment.newInstance()

        savedInstanceState ?: supportFragmentManager.beginTransaction()
                .replace(R.id.cameraPreview, mCamera2PreviewFragment)
                .commit()

        mCamera2PreviewFragment.processingResultView = mProcessingResultView
        processingResult.addView(mProcessingResultView)

//        // Create an instance of Camera
//        mCamera = CameraUtil.getCameraInstance()
//
//        //CameraUtil.setCameraDisplayOrientation(mCamera!!)
//
//        mPreview = mCamera?.let {
//            // Create our Preview view
//            CameraPreview(this, it)
//        }
//
//        // Set the Preview view as the content of our activity.
//        mPreview?.also {
//            val preview: FrameLayout = cameraPreview
//            preview.addView(it)
//        }

        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 50);
        }
    }

    private fun requestCameraPermission() {
//        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
//            // ConfirmationDialog().show(childFragmentManager, Camera2PreviewFragment.FRAGMENT_DIALOG)
//        } else {
//            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
//        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // ErrorDialog.newInstance(getString(R.string.request_permission))
                //         .show(childFragmentManager, Camera2PreviewFragment.FRAGMENT_DIALOG)
                runOnUiThread { Toast.makeText(this, "Camera Permission not granted", Toast.LENGTH_SHORT).show() }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun initNative() {
        val input = resources.openRawResource(R.raw.ocrb_sample)
        val bmp = BitmapFactory.decodeStream(input)

        val pixels = IntArray(bmp.width * bmp.height)
        bmp.getPixels(pixels, 0, bmp.width, 0, 0, bmp.width, bmp.height)

        nInit(bmp.width, bmp.height, pixels)
    }

    private external fun nInit(width: Int, height: Int, pixels: IntArray)

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1

        init {
            System.loadLibrary("native-lib")
        }
    }
}
