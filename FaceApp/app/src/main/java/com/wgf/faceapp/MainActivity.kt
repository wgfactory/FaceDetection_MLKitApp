package com.wgf.faceapp

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import com.wgf.faceapp.Helper.FaceGraphicOverlay
import com.wonderkiln.camerakit.*
import com.wonderkiln.camerakit.CameraKit.Constants.FACING_BACK
import com.wonderkiln.camerakit.CameraKit.Constants.FACING_FRONT
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.name

    lateinit var waitingDialog: AlertDialog
//    var mGraphicOverlay: GraphicOverlay? = null

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

        // 얼굴 검출을 하기 위한 옵션들 설정
        var options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE) // 정확도
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS) // 모든 LandMark 표시
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS) // 얼굴 윤곽선 표시
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS) // 모든 얼굴 분류
            .build()

        var detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

        detector.detectInImage(image)
            .addOnSuccessListener { result -> processFaceResult(result) }
            .addOnFailureListener {e -> Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()}

    }

    // 얼굴 검출 결과 함수
    private fun processFaceResult(faceResult: List<FirebaseVisionFace>) {
        //TODO 로그 출력

        var count = 0

        if(faceResult.size == 0) {
            showToast("사람의 얼굴을 못찾았습니다 ㅜㅜ!")
            return
        }

        Log.d(TAG, ">> faceResult.size = $faceResult.size")

        // 사각형박스로 얼굴 검출 결과 표시
        /*for(face in faceResult) {
            val bounds = face.boundingBox
            val rectOverlay = RectOverlay(mGraphicOverlay!!, bounds)
            mGraphicOverlay!!.add(rectOverlay)

            count++

            // 스마일 값 최대 = 1 점점 값 작아짐
            if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                val smileProb = face.smilingProbability
                showToast(String.format("당신의 스마일 값은 %.2f!", smileProb))
            }

            // 눈의 크기 값 최대 = 1 점점 값 작아짐
            if (face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                val rightEyeOpenProb = face.rightEyeOpenProbability
                showToast(String.format("당신의 오른쪽 눈 윙크 값은 %.2f!", rightEyeOpenProb))
            }
        }*/

        // 사각형박스 + 얼굴 윤곽선 및 랜드마크 표시하기
        for(i in 0 until faceResult.size) {
            var face = faceResult.get(i)
            var faceGraphic =
                FaceGraphicOverlay(grapic_overlay, face)
            grapic_overlay.add(faceGraphic)
            count++

        }

        waitingDialog.dismiss()
        showToast(String.format("인공지능이 감지한 얼굴은 %d 명 입니다!", count), Toast.LENGTH_LONG)
    }

    private fun drawFaceLandmark(canvas: Canvas, @FirebaseVisionFaceLandmark.LandmarkType landmarkType: Int) {

    }

    // 토스트 메세지 표시 하는 함수
    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(applicationContext, message, duration).show()
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