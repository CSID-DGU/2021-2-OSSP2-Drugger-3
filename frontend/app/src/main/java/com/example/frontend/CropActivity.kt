package com.example.frontend

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class CropActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //from MainActivity's Camera 앱, get ImageURI
        val strImageUri = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(strImageUri)

        CropImage.activity(imageUri).start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)

            if(resultCode == RESULT_OK){
                handleCropResult(result)
            }
            else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                val error = result.error
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleCropResult(res: CropImageView.CropResult){
        val intent = Intent(this, ocrActivity::class.java)
        intent.putExtra("URI", res.uri.toString())
        startActivity(intent)
    }
}