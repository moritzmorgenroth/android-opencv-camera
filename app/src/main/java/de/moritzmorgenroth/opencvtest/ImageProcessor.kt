package de.moritzmorgenroth.opencvtest

import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import java.io.IOException

internal class ImageProcessor(
        /**
         * The image
         */
        private val image: Image,
        private val resultView: ProcessingResultView?
) : Runnable {

    override fun run() {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        try {

            Log.d(TAG, "process image")

            val mFrameWidth = image.width
            val mFrameHeight = image.height

            val data = IntArray(bytes.size) {it -> bytes[it].toInt() }

            val bmp = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888)
            bmp.setPixels(data, 0/* offset */, mFrameWidth /* stride */, 0, 0, mFrameWidth, mFrameHeight)

            resultView?.drawBitmap(bmp)

        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        } finally {
            image.close()
        }
    }

    /**
     * Native methods that are implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun nScanFrame( data: ByteArray, resultBitmap: Bitmap)
    external fun nFindFeatures(width: Int, height: Int, yuv: ByteArray, rgba: IntArray)

    companion object {

        private const val TAG = "ImageProcessor"

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}