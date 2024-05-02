package com.iganovir.cameraxsample.sampleapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.iganovir.cameraxsample.databinding.ActivityUserSatisficationBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

/**
 * @author Iga Noviyanti (iga.noviyanti@dana.id)
 * @version CameraObjectDetectionActivity, v 0.1 02/05/24 22.10 by Iga Noviyanti
 */
class UserSatisficationActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityUserSatisficationBinding

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var faceDetector: FaceDetector

    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    private var isUserSmiling = false
    private var stopAnalyze = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityUserSatisficationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        checkCameraPermission()
        // Initialize ML Kit Face Detector
        setUpFaceDetector()
        // Initialize CameraX
        startCamera()
        initViews()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkCameraPermission(){
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun initViews(){
        viewBinding.btnSwitchCamera.setOnClickListener {
            switchCamera()
        }
    }

    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        // Restart the camera with the new camera selector
        startCamera()
    }

    private fun setUpFaceDetector(){
        faceDetector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.15f)
                .enableTracking()
                .build()
        )
    }

    private fun startCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Get Latest CameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Prepare View for Preview
            val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            // Prepare ImageAnalyzer
            val imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, getFaceAnalyzer())
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                // Handle error
            }
        }, ContextCompat.getMainExecutor(this))
    }

   @OptIn(ExperimentalGetImage::class)
   private fun getFaceAnalyzer() : ImageAnalysis.Analyzer {
       return ImageAnalysis.Analyzer { imageProxy ->
           val mediaImage = imageProxy.image
           if (mediaImage != null) {
               val inputImage =
                   InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

               faceDetector.process(inputImage)
                   .addOnSuccessListener { faces ->
                       faces.isNotEmpty().let {
                           faces.forEach { face ->
                               // Check if the eyes are open
                               val userSmiling = face?.smilingProbability?.let { it > 0.7 } ?: false

                               if (userSmiling && !stopAnalyze){
                                   val userSatisfaction = face.smilingProbability?.let { it1 ->
                                       calculateUserSatisfaction(
                                           it1
                                       )
                                   }
                                   isUserSmiling = true
                                   stopAnalyze = true
                                   Log.d(TAG, "User is smiling ${face.smilingProbability}")
                                   Snackbar.make(viewBinding.root, "User is smiling! Satisfication rate $userSatisfaction", Snackbar.LENGTH_SHORT).show()
                               }
                           }
                       }
                       imageProxy.close()
                   }
                   .addOnFailureListener { e ->
                       // Handle failure
                       imageProxy.close()
                   }
           }
       }
   }

    private fun calculateUserSatisfaction(smilingProbability: Float): Int = (smilingProbability * 100).roundToInt()

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "UserSatisficationActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
