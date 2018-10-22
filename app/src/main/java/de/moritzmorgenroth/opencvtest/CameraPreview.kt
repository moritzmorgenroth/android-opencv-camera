package de.moritzmorgenroth.opencvtest

import android.content.Context
import android.graphics.*
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException
import android.graphics.BitmapFactory
import android.graphics.Bitmap





/** A basic Camera preview class */
class CameraPreview(
        context: Context,
        private val mCamera: Camera
) : SurfaceView(context), SurfaceHolder.Callback, Camera.PreviewCallback {


    // FIXME stop analysing when invisible.


    private val TAG = CameraPreview.javaClass.simpleName;

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

            // Since the image is served upside down, this is a little counterintuitive

            mFrameWidth = width
            mFrameHeight = height

            Log.d(TAG, "ViewHolder Size: " + width  + "," +  height )

            val size = CameraUtil.getOptimalPreviewSize(sizes, mFrameHeight, mFrameWidth)
            mFrameWidth = size!!.width
            mFrameHeight = size!!.height

            Log.d(TAG, "Selected Size: " + mFrameWidth  + "," +  mFrameHeight )

            params.setPreviewSize(mFrameWidth, mFrameHeight)
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.parameters = params
            try {
                mCamera.setPreviewDisplay(null)
            } catch (e: IOException) {
                Log.e(TAG, "mCamera.setPreviewDisplay fails: $e")
            }

            mCamera.startPreview()
        }
    }

    var processingInProgress = false;

    val analytics = Analytics()
    private var detectedBitmap: Bitmap = Bitmap.createBitmap(1,
            1, Bitmap.Config.ARGB_8888)

    var mFrameWidth: Int = 0
    var mFrameHeight: Int = 0

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {

        if (data == null) {
            return
        }

        if (processingInProgress) {
            // return frame buffer to pool
            analytics.numFramesSkipped++
            camera?.addCallbackBuffer(data)
            return
        }
        processingInProgress = true


        /** pika  */
        nScanFrame(data,  detectedBitmap)

        Log.d(TAG, "Dimens: " + detectedBitmap.width + " , " + detectedBitmap.height )
//        tryDrawing(mHolder, null)
//        processFrame(data)

        tryDrawing(mHolder, processFrame(data))

        // give the image buffer back to the camera, AFTER we're done reading
        // the image.
        camera?.addCallbackBuffer(data)
        processingInProgress = false

    }

    protected fun processFrame(data: ByteArray): Bitmap {
        Log.d(TAG, data.size.toString());
        val frameSize = mFrameWidth * mFrameHeight
        val rgba = IntArray(frameSize)

        nFindFeatures(mFrameWidth, mFrameHeight, data, rgba)


        val bmp = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888)
        bmp.setPixels(rgba, 0/* offset */, mFrameWidth /* stride */, 0, 0, mFrameWidth, mFrameHeight)



        //val bmp2 = Bitmap.createBitmap(data)
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)


        return bmp
    }

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

        Log.d(TAG, "Dimens of Frame: " + bitmap!!.width + "," + bitmap!!.height)

        if(bitmap != null) {
            val matrix = Matrix();
            matrix.postRotate(90F);
            //matrix.postScale(1.5F, 1.5F)
            var resultBitmap = Bitmap.createBitmap(bitmap, 0, 0, mFrameWidth,  mFrameHeight, matrix, true)
            canvas.drawBitmap(resultBitmap!!, 0F, 0F, null)
        }
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    external fun nScanFrame( data: ByteArray, resultBitmap: Bitmap);

    external fun nFindFeatures(width: Int, height: Int, yuv: ByteArray, rgba: IntArray)

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}