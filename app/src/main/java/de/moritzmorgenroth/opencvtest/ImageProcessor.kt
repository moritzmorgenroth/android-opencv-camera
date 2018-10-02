package de.moritzmorgenroth.opencvtest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.media.Image
import android.util.Log
import java.io.IOException
import java.lang.IllegalStateException

internal class ImageProcessor(
        /**
         * The image
         */
        private val image: Image?,
        private val resultView: ProcessingResultView?,
        private val callback: () -> Unit
) : Runnable {

    var isProcessing = false;
    private var mFrameWidth : Int = 0
    private var mFrameHeight : Int = 0

    override fun run() {
        if (image == null) return

        // We only need plane #0, because that's the luminance and we want a b/w picture
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        try {

            Log.d(TAG, "process image")

            mFrameWidth = image.width
            mFrameHeight = image.height

            isProcessing = true;

            val bmp = doNativeWork(bytes)

            isProcessing = false;

            resultView?.drawBitmap(bmp)

        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        } finally {
            try {
                image.close()
            } catch (e: IllegalStateException) {
                Log.e(TAG, e.toString())
            }
        }
    }

    private fun doNativeWork(data: ByteArray) : Bitmap {
        val frameSize = mFrameWidth * mFrameHeight
        val rgba = IntArray(frameSize)

        nProcessPicture(mFrameWidth, mFrameHeight, data, rgba)


        val bmp = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888)
        bmp.setPixels(rgba, 0/* offset */, mFrameWidth /* stride */, 0, 0, mFrameWidth, mFrameHeight)

        val matrix = Matrix()
        matrix.postRotate(90F)
        //matrix.postScale(1.5F, 1.5F)
        val resultBitmap = Bitmap.createBitmap(bmp, 0, 0, mFrameWidth,  mFrameHeight, matrix, true)

        callback()

        return resultBitmap
    }

    /**
     * Native methods that are implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    private external fun nProcessPicture(width: Int, height: Int, yuv: ByteArray, rgba: IntArray)

    companion object {

        private const val TAG = "ImageProcessor"

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}