package de.moritzmorgenroth.opencvtest

import android.content.Context
import android.graphics.*
import android.hardware.camera2.CameraDevice
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException



/** A basic Camera preview class */
class CameraPreview(
        context: Context,
        private val mCamera: Camera
) : SurfaceView(context), SurfaceHolder.Callback, Camera.PreviewCallback {

    var processingInProgress = false;
    init {

    }
    private val mHolder: SurfaceHolder = holder.apply {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        addCallback(this@CameraPreview)

        // deprecated setting, but required on Android versions prior to 3.0
        setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        mCamera.apply {
            try {
                //setPreviewDisplay(holder)
                startPreview()
            } catch (e: IOException) {
                Log.d("CAMERA", "Error setting camera preview: ${e.message}")
            }
        }
        var canvas: Canvas? = null
        try {
            canvas = holder.lockCanvas(null)
            synchronized(holder) {
                draw(canvas)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.surface == null) {
            // preview surface does not exist
            return
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        mCamera.apply {
            try {
                //setPreviewDisplay(mHolder)
                setPreviewCallback(this@CameraPreview)
            } catch (e: Exception) {
                Log.d("CAMERA", "Error starting camera preview: ${e.message}")
            }
        }

        if (mCamera != null) {
            val params = mCamera.parameters
            val sizes = params.supportedPreviewSizes
            mFrameWidth = width
            mFrameHeight = height

            // selecting optimal camera preview size
            run {
                var minDiff = Int.MAX_VALUE
                for (size in sizes) {
                    if (Math.abs(size.height - height) < minDiff) {
                        mFrameWidth = size.width
                        mFrameHeight = size.height
                        minDiff = Math.abs(size.height - height)
                    }
                }
            }

            params.setPreviewSize(mFrameWidth, mFrameHeight)
            mCamera.parameters = params
            try {
                mCamera.setPreviewDisplay(null)
            } catch (e: IOException) {
                Log.e(TAG, "mCamera.setPreviewDisplay fails: $e")
            }

            mCamera.startPreview()
        }
    }

    private var numFramesSkipped = 0;
    private var detectedBitmap: Bitmap? = null;

    var mFrameWidth: Int = 0
    var mFrameHeight: Int = 0

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {

        if (data == null) {
            return
        }

        if (processingInProgress) {
            // return frame buffer to pool
            numFramesSkipped++
            camera?.addCallbackBuffer(data)
            return
        }
        processingInProgress = true


        /** pika  */
        if(detectedBitmap == null) detectedBitmap = Bitmap.createBitmap(500,
                300, Bitmap.Config.ARGB_8888)
        nScanFrame(data,  detectedBitmap!!)

//        tryDrawing(mHolder, null)
//        processFrame(data)

        tryDrawing(mHolder, processFrame(data))

        // give the image buffer back to the camera, AFTER we're done reading
        // the image.
        camera?.addCallbackBuffer(data)
        processingInProgress = false

    }

    private val TAG = "CAMERA"

    private fun tryDrawing(holder: SurfaceHolder, bitmap: Bitmap?) {
        Log.i(TAG, "Trying to draw...")

        val canvas = holder.lockCanvas()
        if (canvas == null) {
            Log.e(TAG, "Cannot draw onto the canvas as it's null")
        } else {
            drawMyStuff(canvas, bitmap)
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawMyStuff(canvas: Canvas, bitmap: Bitmap?) {
        Log.i(TAG, "Drawing...")
        canvas.drawRGB(255, 128, 128)
//        if(bitmap != null)canvas.drawBitmap(bitmap!!, Rect(0, 0, 200, 200) , Rect(0, 0, 50, 100),null) :
        if(bitmap != null) {

            val matrix = Matrix();

            matrix.postRotate(90F);

            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, mFrameWidth, mFrameHeight, matrix, true)

            canvas.drawBitmap(rotatedBitmap!!, 0F, 0F, null)
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String
    external fun nScanFrame( data: ByteArray, resultBitmap: Bitmap);

    protected fun processFrame(data: ByteArray): Bitmap {
        val frameSize = mFrameWidth * mFrameHeight
        val rgba = IntArray(frameSize)

        nFindFeatures(mFrameWidth, mFrameHeight, data, rgba)

        val bmp = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888)
        bmp.setPixels(rgba, 0/* offset */, mFrameWidth /* stride */, 0, 0, mFrameWidth, mFrameHeight)
        return bmp
    }

    external fun nFindFeatures(width: Int, height: Int, yuv: ByteArray, rgba: IntArray)

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}