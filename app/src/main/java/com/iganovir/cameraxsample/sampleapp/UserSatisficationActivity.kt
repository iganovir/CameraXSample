package com.iganovir.cameraxsample.sampleapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var cameraController: LifecycleCameraController

    private var selectedCameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    private var stopAnalyze = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityUserSatisficationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        checkCameraPermission()
        initViews()
    }

    private fun initViews() {
        viewBinding.btnSwitchCamera.setOnClickListener {
            switchCamera()
        }
    }

    private fun switchCamera() {
        selectedCameraSelector =
            if (selectedCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
        startCamera()
    }

    private fun setUpFaceDetector() {
        faceDetector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.15f)
                .enableTracking()
                .build()
        )

        cameraController.setImageAnalysisAnalyzer(
            cameraExecutor,
            MlKitAnalyzer(
                listOf(faceDetector),
                ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result: MlKitAnalyzer.Result? ->
                val faces = result?.getValue(faceDetector)
                faces?.isNotEmpty().let {
                    faces?.forEach { face ->
                        val userSmiling =
                            face?.smilingProbability?.let { it > MINIMAL_SMILING_PROBABILITY }
                                ?: false
                        if (userSmiling && !stopAnalyze) {
                            val userSatisfaction = face.smilingProbability?.let { smile ->
                                calculateUserSatisfaction(smile)
                            }
                            stopAnalyze = true
                            showSnackbar(userSatisfaction ?: 0)
                        }
                    }
                }
            }
        )
    }

    private fun startCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraController = LifecycleCameraController(baseContext).apply {
            cameraSelector = selectedCameraSelector
            isPinchToZoomEnabled = false
            isTapToFocusEnabled = true
        }

        val previewView: PreviewView = viewBinding.viewFinder

        cameraController.bindToLifecycle(this)
        previewView.controller = cameraController

        setUpFaceDetector()
    }

    private fun showSnackbar(userSatisfaction: Int) {
        Snackbar.make(
            viewBinding.root,
            "User is smiling! Satisfication rate $userSatisfaction",
            Snackbar.LENGTH_SHORT
        ).addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                stopAnalyze = false
            }
        }).show()
    }

    private fun calculateUserSatisfaction(smilingProbability: Float): Int =
        (smilingProbability * 100).roundToInt()

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        faceDetector.close()
    }

    private fun checkCameraPermission() {
        if (REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(
                    baseContext,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startCamera()
        } else {
            Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {

        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val MINIMAL_SMILING_PROBABILITY = 0.7
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
