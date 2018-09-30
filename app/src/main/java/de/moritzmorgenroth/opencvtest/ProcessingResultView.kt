package de.moritzmorgenroth.opencvtest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.SurfaceHolder
import android.view.SurfaceView

class ProcessingResultView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        drawBitmap(null)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun drawBitmap(data: Bitmap?) {
        var canvas: Canvas? = null
        try {
            canvas = holder.lockCanvas(null)

            canvas.drawRGB(255, 128, 128)
            if (data != null) canvas.drawBitmap(data, 0f, 0f, null)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}