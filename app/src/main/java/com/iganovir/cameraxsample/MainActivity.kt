package com.iganovir.cameraxsample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iganovir.cameraxsample.qrscanner.BarcodeScanningActivity
import com.iganovir.cameraxsample.basiccameraapp.cameraprovider.CameraProviderPreviewActivity
import com.iganovir.cameraxsample.databinding.ActivityMainBinding
import com.iganovir.cameraxsample.sampleapp.UserSatisficationActivity

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        with(viewBinding) {
            buttonCameraProviderPreview.setOnClickListener {
                val intent = Intent(this@MainActivity, CameraProviderPreviewActivity::class.java)
                startActivity(intent)
            }

            buttonBarcodeScanning.setOnClickListener {
                val intent = Intent(this@MainActivity, BarcodeScanningActivity::class.java)
                startActivity(intent)
            }

            buttonUserSatisfaction.setOnClickListener {
                val intent = Intent(this@MainActivity, UserSatisficationActivity::class.java)
                startActivity(intent)
            }
        }
    }
}