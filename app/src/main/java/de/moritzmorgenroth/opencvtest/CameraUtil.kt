package de.moritzmorgenroth.opencvtest

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.util.Log

object CameraUtil {
    /** Check if this device has a camera */
    private fun checkCameraHardware(context: Context): Boolean {
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true
        } else {
            // no camera on this device
            return false
        }
    }
    /** A safe way to get an instance of the Camera object. */
    fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance

        } catch (e: Exception) {
            // Camera is not available (in use or does not exist
            Log.e("CAMERA", e.message)
            null // returns null if camera is unavailable
        }

    }

    fun setCameraDisplayOrientation(mCamera: Camera) {
        val result: Int

        /* check API level. If upper API level 21, re-calculate orientation. */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val info = android.hardware.Camera.CameraInfo()
            android.hardware.Camera.getCameraInfo(0, info)
            val cameraOrientation = info.orientation
            result = (cameraOrientation + 360) % 360
        } else {
            /* if API level is lower than 21, use the default value */
            result = 90
        }

        /*set display orientation*/
        mCamera.setDisplayOrientation(result)
    }

    fun getOptimalPreviewSize(sizes: List<Camera.Size>?, w: Int, h: Int): Camera.Size? {
        val ASPECT_TOLERANCE = 0.1
        val targetRatio = h.toDouble() / w

        if (sizes == null) return null

        var optimalSize: Camera.Size? = null
        var minDiff = java.lang.Double.MAX_VALUE

        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - h).toDouble()
            }
        }

        if (optimalSize == null) {
            minDiff = java.lang.Double.MAX_VALUE
            for (size in sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - h).toDouble()
                }
            }
        }
        return optimalSize
    }
}