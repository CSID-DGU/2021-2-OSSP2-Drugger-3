package com.example.frontend

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.frontend.databinding.ActivityOcrBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.medianBlur
import org.opencv.imgproc.Imgproc.threshold
import java.lang.Exception

class ocrActivity : AppCompatActivity() {

    //Used to load the 'native-lib' library on application startUp
    companion object{
        init{
            if(!OpenCVLoader.initDebug()){
                Log.d("OCR_activity", "openCV is not loaded!")
            }
            else{
                Log.d("OCR_activity", "openCV is loaded, successfully!")
            }
        }
    }

    //ViewBinding
    private lateinit var binding:ActivityOcrBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val strImageUri = intent.getStringExtra("URI")
        val imageUri = Uri.parse(strImageUri)

        //ImageProcessing
        imageUri?.let{ uri ->
            //get Bitmap Img from URI
            val image = getBitmap(uri)

            //get grayedBitmap from Bitmap Img
            val grayedImage = image?.let { mkGray(it) }

            //get binary Img
            val binaryImage = grayedImage?.let { mkBinary(it) }

            //remove noise
            val noiselessImage = binaryImage?.let { removeNoise(it) }
        }
    }

    private fun getBitmap(imgUri: Uri): Bitmap? {
        var bitmap: Bitmap? = null

        try{
            bitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                // https://developer.android.com/reference/android/graphics/ImageDecoder
                // CvException [org.opencv.core.CvException: OpenCV(4.1.1) /build/master_pack-android/opencv/modules/java/generator/src/cpp/utils.cpp:38: error: (-215:Assertion failed) AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0
                /*
                  By default, a Bitmap created by ImageDecoder (including one that is inside a Drawable)
                  will be immutable (i.e. Bitmap#isMutable returns false), and it will typically
                  have Config Bitmap.Config#HARDWARE. Although these properties can be changed
                  with setMutableRequired(true)
                 */
                val source = ImageDecoder.createSource(contentResolver, imgUri)
                ImageDecoder.decodeBitmap(source){ decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
            } else{
                MediaStore.Images.Media.getBitmap(contentResolver, imgUri)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

        return bitmap
    }

    private fun mkGray(bitmap: Bitmap): Bitmap{
        //create openCV mat object & copy content from bitmap
        var mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        //convert to grayscale
        val grayedBitmap: Bitmap = bitmap
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)
        Utils.matToBitmap(mat, grayedBitmap)

        return grayedBitmap
    }

    private fun mkBinary(grayedBitmap: Bitmap): Bitmap{
        //create openCV mat object & copy content from bitmap
        var mat = Mat()
        Utils.bitmapToMat(grayedBitmap, mat)

        //convert to binary
        val binaryBitmap: Bitmap = grayedBitmap
        threshold(mat, mat, 127.0, 255.0, Imgproc.THRESH_BINARY)
        Utils.matToBitmap(mat, binaryBitmap)

        return binaryBitmap
    }

    private fun removeNoise(bitmap: Bitmap, kernelSize: Int = 5): Bitmap{
        //create openCV mat object & copy content from bitmap
        var mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        //remove noise
        val noiselessBitmap: Bitmap = bitmap
        medianBlur(mat, mat, kernelSize)
        Utils.matToBitmap(mat, noiselessBitmap)

        return noiselessBitmap
    }
}