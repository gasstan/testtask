package com.gasstan.wireframerendering.rendering

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import java.io.File


object WireframeRendering {

    //TODO use PixelCopy API
    fun renderWireframe(activity: Activity): Bitmap {
        val v = activity.window.decorView
        v.isDrawingCacheEnabled = true
        v.buildDrawingCache(true)
        val b: Bitmap = Bitmap.createBitmap(v.drawingCache)
        v.isDrawingCacheEnabled = false
        createWireframe(b, v)
        return b
    }

    private fun createWireframe(bitmap: Bitmap, v: View) {
        val canvas = Canvas(bitmap)

        v.getAllViews().forEach {
            if (!it.isShown) return@forEach
            if (it is TextView) {
                if (it.text.isEmpty()) {
                    canvas.hideView(it as View)
                    return@forEach
                }
                canvas.hideView(it)
            }

            if (it is ImageView) {
                canvas.hideView(it)
            }
        }

        storeImage(bitmap)
    }


    private fun storeImage(b: Bitmap) {
        val f = File(Environment.getExternalStorageDirectory().toString() + "/wireframe.png")
        f.writeBitmap(b)
    }
}