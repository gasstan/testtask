package com.gasstan.wireframerendering.rendering

import android.graphics.*
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.plus
import androidx.core.graphics.toRectF
import androidx.core.view.children
import java.io.File

//View extensions
internal fun View.getAllViews(): List<View> {
    if (this !is ViewGroup || childCount == 0) return listOf(this)

    return children
            .toList()
            .flatMap { it.getAllViews() }
            .plus(this as View)
}

internal fun ImageView.getDominantColor(): Int {
    val b = drawable?.toBitmap(1, 1) ?: return Color.WHITE
    val color = b.getPixel(0, 0)
    b.recycle()
    return ColorUtils.setAlphaComponent(color, 255)
}

internal fun TextView.getTextLines() = mutableListOf<String>().apply {
    for (line in 0 until lineCount) {
        val start: Int = layout.getLineStart(line)
        val end: Int = layout.getLineEnd(line)
        val substring = text.subSequence(start, end).toString()
        add(substring)
    }
}

//File extensions
internal fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 90) {
    if (!exists()) createNewFile()
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}

//Canvas extensions
internal fun Canvas.hideView(textView: TextView) {
    val paint = createPaint(ColorUtils.setAlphaComponent(textView.currentTextColor, 255))
    val globalRect = Rect()
    textView.getGlobalVisibleRect(globalRect)
    val texts = textView.getTextLines()

    for ((i, text) in texts.withIndex()) {
        val textRect = Rect()
        textRect.fillTextBounds(textView, text)

        val p = when (textView.gravity) {
            Gravity.CENTER -> Point(globalRect.centerX() - (textRect.width() / 2), globalRect.top + (textRect.height() / 2) + textView.lineHeight * (i + 1))
            Gravity.CENTER_HORIZONTAL -> Point(globalRect.centerX() - (textRect.width() / 2), globalRect.top + textView.lineHeight * (i + 1))
            Gravity.CENTER_VERTICAL -> Point(globalRect.left + textView.paddingLeft, globalRect.top + (textRect.height() / 2) + textView.lineHeight * (i + 1))
            else -> Point(globalRect.left + textView.paddingLeft, globalRect.top - textView.paddingTop + textView.lineHeight * (i + 1))
        }
        val result = textRect.plus(p).apply {
            top -= 5
            bottom += 5
            left -= 5
            right += 5
        }
        drawRoundRect(result.toRectF(), 10f, 10f, paint)
    }
}

internal fun Canvas.hideView(imageView: ImageView) {
    val paint = createPaint(imageView.getDominantColor())
    val r = Rect()
    imageView.getGlobalVisibleRect(r)
    drawRect(r, paint)
}

internal fun Canvas.hideView(view: View) {
    val paint = createPaint(Color.WHITE)

    val r = Rect()
    view.getGlobalVisibleRect(r)
    drawRect(r, paint)
}

private fun createPaint(color: Int) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
        }

//Rect extensions
private fun Rect.fillTextBounds(textView: TextView, text: String) {
    Paint().apply {
        typeface = textView.typeface
        textSize = textView.textSize
        getTextBounds(text, 0, text.length, this@fillTextBounds)
    }
}