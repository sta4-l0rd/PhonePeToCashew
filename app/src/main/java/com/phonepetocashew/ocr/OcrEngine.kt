package com.phonepetocashew.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.phonepetocashew.receipt.ReceiptRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Interface for OCR engine to allow testability.
 */
interface IOcrEngine {
    suspend fun recognizeFullImage(bitmap: Bitmap): String
    suspend fun recognizeRegion(bitmap: Bitmap, region: ReceiptRegion): String
}

/**
 * On-device ML Kit text recognizer implementation.
 */
class MlKitOcrEngine(
    private val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
) : IOcrEngine {

    override suspend fun recognizeFullImage(bitmap: Bitmap): String = withContext(Dispatchers.Default) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val visionText = recognizer.process(image).await()
        visionText.text
    }

    override suspend fun recognizeRegion(bitmap: Bitmap, region: ReceiptRegion): String = withContext(Dispatchers.Default) {
        val croppedBitmap = region.crop(bitmap)
        val image = InputImage.fromBitmap(croppedBitmap, 0)
        val visionText = recognizer.process(image).await()
        visionText.text
    }
}
