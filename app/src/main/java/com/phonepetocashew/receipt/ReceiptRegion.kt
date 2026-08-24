package com.phonepetocashew.receipt

import android.graphics.Bitmap

/**
 * Defines a normalized region of an image (0.0 to 1.0)
 * to support resolution-independent cropping and OCR.
 */
data class ReceiptRegion(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    init {
        require(left in 0f..1f) { "left must be between 0.0 and 1.0 (got $left)" }
        require(top in 0f..1f) { "top must be between 0.0 and 1.0 (got $top)" }
        require(right in 0f..1f) { "right must be between 0.0 and 1.0 (got $right)" }
        require(bottom in 0f..1f) { "bottom must be between 0.0 and 1.0 (got $bottom)" }
        require(left < right) { "left ($left) must be less than right ($right)" }
        require(top < bottom) { "top ($top) must be less than bottom ($bottom)" }
    }

    /**
     * Crops this normalized region from the given bitmap safely.
     */
    fun crop(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val x = (left * width).toInt().coerceIn(0, width - 1)
        val y = (top * height).toInt().coerceIn(0, height - 1)
        val w = ((right - left) * width).toInt().coerceIn(1, width - x)
        val h = ((bottom - top) * height).toInt().coerceIn(1, height - y)

        return Bitmap.createBitmap(bitmap, x, y, w, h)
    }
}
