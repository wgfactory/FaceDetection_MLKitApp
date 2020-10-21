package com.wgf.faceapp

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.wgf.faceapp.Helper.FaceGraphicOverlay
import com.wgf.faceapp.Helper.RectOverlay
import com.wonderkiln.camerakit.*
import com.wonderkiln.camerakit.CameraKit.Constants.FACING_BACK
import com.wonderkiln.camerakit.CameraKit.Constants.FACING_FRONT
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.simpleName

//    private lateinit var binding: ActivityMainBinding

    val DRAW_FACE_BOX = 1
    val DRAW_FACE_CONTOUR = 2

    lateinit var waitingDialog: AlertDialog

    var camFacing = FACING_BACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, ">> onCreate()")

        // "잠시만 기다려 주세용!" 팝업창 코드
        waitingDialog = SpotsDialog.Builder().setContext(this)
            .setMessage("잠시만 기다려 주세용!") // TODO - 원하는 메세지로 수정하기!
            .setCancelable(false)
            .build()

        // 카메라 스위치 버튼 클릭했을 때 코드
        btn_cam_change.setOnClickListener {

            if(camFacing == FACING_BACK) {
                camFacing = FACING_FRONT
                camera_view.facing = FACING_FRONT
            } else {
                camFacing = FACING_BACK
                camera_view.facing = FACING_BACK
            }
        }

        // 얼굴 감지! 버튼 클릭했을 때 코드
        btn_detect.setOnClickListener {
            camera_view.start()
            camera_view.captureImage()
            grapic_overlay.clear()
        }

        // 카메라 뷰에 대한 코드
        camera_view.addCameraKitListener(object:CameraKitEventListener{
            override fun onVideo(p0: CameraKitVideo?) {
                Log.d(TAG, ">> onVideo")
            }

            override fun onEvent(p0: CameraKitEvent?) {
                //TODO - onEvent() 함수 로그 출력
            }

            override fun onImage(p0: CameraKitImage?) {
                //TODO - onImage() 함수 로그 출력

                waitingDialog.show()
                var bitmap = p0!!.bitmap
                bitmap = Bitmap.createScaledBitmap(bitmap, camera_view.width, camera_view.height, false)

                camera_view.stop()

                runFaceDetector(bitmap)
            }

            override fun onError(p0: CameraKitError?) {
                //TODO - onError() 함수 로그 출력

            }
        })
    }

    private fun runFaceDetector(bitmap: Bitmap?) {
        Log.d(TAG, ">> runFaceDetector()")

        var image = FirebaseVisionImage.fromBitmap(bitmap!!)

        // FireBase에서 얼굴 검출을 하기 위한 설정들!!
        var options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE) // 정확도
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS) // 모든 LandMark 표시
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS) // 얼굴 윤곽선 표시
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS) // 모든 얼굴 분류
            .build()

        var detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

        // 얼굴 검출이 되면 호출될 함수 설정하는 코드!
        detector.detectInImage(image)
            .addOnSuccessListener { result -> processFaceResult(result, DRAW_FACE_CONTOUR) }
            .addOnFailureListener {e -> Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()}

    }

    // 얼굴 검출이 완료되고 수행되는 결과 함수
    private fun processFaceResult(faceResult: List<FirebaseVisionFace>, drawType: Int) {
        Log.d(TAG, ">> processFaceResult()")

        var count = 0

        if(faceResult.size == 0) {
            // TODO - 원하는 메세지로 수정하기!
            showToast("사람의 얼굴을 못찾았습니다 ㅜㅜ!")
            waitingDialog.dismiss()
            return
        }

        Log.d(TAG, ">> faceResult.size = $faceResult.size")
        for(i in 0 until faceResult.size) {
            var face = faceResult.get(i)

            if(drawType == DRAW_FACE_BOX) {
                val bounds = face.boundingBox
                val rectOverlay = RectOverlay(grapic_overlay, bounds)
                grapic_overlay.add(rectOverlay)
            }

            if(drawType == DRAW_FACE_CONTOUR) {
                var faceGraphic = FaceGraphicOverlay(grapic_overlay, face)
                grapic_overlay.add(faceGraphic)
            }
            count++
        }

        // Dialog 사라지게 하는 코드
        waitingDialog.dismiss()
        showToast(String.format("인공지능이 감지한 얼굴은 %d 명 입니다!", count), Toast.LENGTH_LONG)
    }

    // 토스트 메세지 표시 하는 함수
    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(applicationContext, message, duration).show()
    }

    //onResume() 함수 - 화면이 보여질 때!!
    override fun onResume() {
        super.onResume()
        Log.d(TAG, ">> onResume()")

        camera_view.start()

        //어플 실행시에 전면카메라로 시작하기!
        camera_view.facing = FACING_FRONT
    }

    //onPause() 함수!!
    override fun onPause() {
        super.onPause()
        Log.d(TAG, ">> onPause()")

        camera_view.stop()
    }

    //onDestroy() 함수!!
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, ">> onDestroy()")
    }
}