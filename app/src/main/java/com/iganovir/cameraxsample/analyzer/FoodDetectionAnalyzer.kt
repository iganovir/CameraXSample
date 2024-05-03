package com.iganovir.cameraxsample.analyzer

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

typealias FoodDetectionListener = (foodObjects: List<DetectedObject>) -> Unit

class FoodDetectionAnalyzer(private val listener: FoodDetectionListener) : ImageAnalysis.Analyzer {

    private val options = ObjectDetectorOptions.Builder()
        .enableMultipleObjects()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .enableClassification()
        .build()

    private val objectDetector: ObjectDetector = ObjectDetection.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            objectDetector.process(image)
                .addOnSuccessListener { detectedObjects ->
                    detectedObjects.forEach {
                        Log.d("FoodDetectionAnalyzer", "Detected object: ${it.labels}")
                    }
                    val foodObjects = detectedObjects.filter { isFood(it) }
                    listener(foodObjects)
                }
                .addOnFailureListener { e ->
                    // Handle any errors
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun isFood(detectedObject: DetectedObject): Boolean {
        return detectedObject.labels.any { it.text == "Food" }
    }
}