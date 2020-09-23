package com.wgf.faceapp

import android.app.AlertDialog
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.wgf.faceapp.Helper.RectOverlay
import com.wonderkiln.camerakit.*
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_main.*

import com.wonderkiln.camerakit.CameraKit.Constants.FACING_BACK
import com.wonderkiln.camerakit.CameraKit.Constants.FACING_FRONT
import com.wonderkiln.camerakit.CameraKit.Constants.FLASH_AUTO
import com.wonderkiln.camerakit.CameraKit.Constants.FLASH_OFF
import com.wonderkiln.camerakit.CameraKit.Constants.FLASH_ON


class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.name

    lateinit var waitingDialog: AlertDialog

    var camFacing = FACING_BACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, ">> onCreate()")

        //Init Waiting Dialog
        waitingDialog = SpotsDialog.Builder().setContext(this)
            .setMessage("잠시만 기다려 주세용!")
            .setCancelable(false)
            .build()

        btn_cam_change.setOnClickListener {

            if(camFacing == FACING_BACK) {
                camFacing = FACING_FRONT
                camera_view.facing = FACING_FRONT
            } else {
                camFacing = FACING_BACK
                camera_view.facing = FACING_BACK
            }
        }

        btn_detect.setOnClickListener {
            camera_view.start()
            camera_view.captureImage()
            grapic_overlay.clear()
        }

        camera_view.addCameraKitListener(object:CameraKitEventListener{
            override fun onVideo(p0: CameraKitVideo?) {
                Log.d(TAG, ">> onVideo")
            }

            override fun onEvent(p0: CameraKitEvent?) {
                //TODO 로그 출력
            }

            override fun onImage(p0: CameraKitImage?) {
                //TODO 로그 출력

                waitingDialog.show()
                var bitmap = p0!!.bitmap
                bitmap = Bitmap.createScaledBitmap(bitmap, camera_view.width, camera_view.height, false)

                camera_view.stop()

                runFaceDetector(bitmap)
            }

            override fun onError(p0: CameraKitError?) {
                //TODO 로그 출력
            }
        })
    }

    private fun runFaceDetector(bitmap: Bitmap?) {
        //TODO 로그 출력

        var image = FirebaseVisionImage.fromBitmap(bitmap!!)
        var options = FirebaseVisionFaceDetectorOptions.Builder().build()
        var detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

        detector.detectInImage(image)
            .addOnSuccessListener { result -> processFaceResult(result) }
            .addOnFailureListener {e -> Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()}

    }

    private fun processFaceResult(result: List<FirebaseVisionFace>) {
        //TODO 로그 출력

        var count = 0

        for(face in result) {
            val bounds = face.boundingBox
            val rectOverlay = RectOverlay(grapic_overlay, bounds)
            grapic_overlay.add(rectOverlay)

            count++
        }
        waitingDialog.dismiss()
        Toast.makeText(this, String.format("인공지능이 감지한 얼굴은 %d 명 입니다!", count), Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()

        //TODO 로그 출력

        camera_view.start()
    }

    override fun onPause() {
        super.onPause()

        //TODO 로그 출력

        camera_view.stop()
    }

    override fun onDestroy() {
        super.onDestroy()

        //TODO 로그 출력
    }
}