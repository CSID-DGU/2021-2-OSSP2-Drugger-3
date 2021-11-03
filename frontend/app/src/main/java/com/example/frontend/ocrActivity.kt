package com.example.frontend

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.frontend.databinding.ActivityOcrBinding
import com.googlecode.tesseract.android.TessBaseAPI
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.medianBlur
import org.opencv.imgproc.Imgproc.threshold
import java.io.*
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

    //create Tesseract API' Object
    lateinit var tess: TessBaseAPI
    var dataPath: String = ""

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
        /*
        var resultImage: Bitmap? = null
        imageUri?.let{ uri ->
            //get Bitmap Img from URI
            val image = getBitmap(uri)

            //get grayedBitmap from Bitmap Img
            val grayedImage = image?.let { mkGray(it) }

            //get binary Img
            val binaryImage = grayedImage?.let { mkBinary(it) }

            //remove noise
            resultImage = binaryImage?.let { removeNoise(it) }
        }
        binding.imageView4.setImageBitmap(resultImage)
        */

        var testImg = BitmapFactory.decodeResource(resources, R.drawable.test)
        //testImg = mkGray(testImg)
        //testImg = mkBinary(grayedImg)
        //testImg = removeNoise(binaryImg)

        binding.imageView4.setImageBitmap(testImg)

        //OCR with tess-two
        dataPath = "$filesDir/tesseract/" //for language data

        checkFile(File(dataPath+"tessdata/"), "kor")

        val lang = "kor"
        tess = TessBaseAPI()
        tess.init(dataPath, lang)

        val resultText = processOCR(testImg)
        binding.result.text = resultText
    }

    //OCR Processing
    private fun copyFile(lang: String) = try {
      val filePath = "$dataPath/tessdata/$lang.traineddata"

      val assetManager: AssetManager = getAssets()

      val inputStream: InputStream = assetManager.open("tessdata/$lang.traineddata")
      val outStream: OutputStream = FileOutputStream(filePath)

      val buffer: ByteArray = ByteArray(1024)

      var read: Int = 0
      read = inputStream.read(buffer)
      while(read!=-1){
          outStream.write(buffer,0,read)
          read = inputStream.read(buffer)
      }
      outStream.flush()
      outStream.close()
      inputStream.close()
    }catch(e: FileNotFoundException){
        Log.e("fileError", e.toString())
    }catch(e: IOException){
        Log.e("ioError", e.toString())
    }

    private fun checkFile(dir: File, lang: String) {
        if(!dir.exists() && dir.mkdirs()){
            copyFile(lang)
        }
        if(dir.exists()){
            val dataFilePath: String = "$dataPath/tessdata/$lang.traineddata"
            val dataFile: File = File(dataFilePath)

            if(!dataFile.exists()){
                copyFile(lang)
            }
        }
    }

    private fun processOCR(resultImage: Bitmap?): String {
        Toast.makeText(this, "텍스트 변환 중입니다...", Toast.LENGTH_SHORT).show()

        var ocrResult: String? = null
        tess.setImage(resultImage)
        ocrResult = tess.utF8Text

        return ocrResult
    }

    //Image Processing
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