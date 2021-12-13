package com.example.frontend

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.frontend.databinding.ActivityOcrBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.*

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

        //crop된 이미지 받아와 이미지 뷰에 setting
        val strImageUri = intent.getStringExtra("URI")
        val imageUri = Uri.parse(strImageUri)

        //실제 코드

        binding.inputImg.setImageURI(imageUri)
        //ImageProcessing
        val grayedImage = getGrayscaledImage(imageUri)


        //테스트 이미지로
//        val testImage = BitmapFactory.decodeResource(resources, R.drawable.parentest)
//        val grayedImage = getGrayscaledImage_test(testImage)

        binding.inputImg.setImageBitmap(grayedImage)

        //Uri to realPath
        val contentUri = getContentUri(grayedImage)
        val realPath = getAbPath(contentUri)
        var resultText = ""

        println(contentUri)
        println(realPath)

        if(realPath != null){
            resultText = coroutine(realPath)
        }

        //OCR로 처리된 기본 텍스트 값
        var inputText = resultText

        //사용자 수정후 update
        binding.returnOCR.doAfterTextChanged {
            inputText = binding.returnOCR.text.toString()
        }

        binding.gotoSelect.setOnClickListener() {
            //전달 텍스트가 없는 경우, 에러 토스트 메세지 출력
            if(inputText.isEmpty() || inputText.isBlank()){
                Toast.makeText(this, "입력이 없습니다, \n검색하고자하는 약물을 제대로 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
            //goto Select Activity with String Arr
            else{
                //inputText -> searchActivity로 전달
                val resultArr = inputText.split("\n")
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("search", ArrayList(resultArr))
                startActivity(intent)
            }
        }
    }

    private fun getContentUri(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val contentPath = MediaStore.Images.Media.insertImage(
            this.contentResolver, bitmap, "cropped", null)
        val contentUri = Uri.parse(contentPath)
        return contentUri
    }

    @SuppressLint("Range")
    fun getAbPath(uri: Uri): String?{
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToNext()
        val path = cursor?.getString(cursor.getColumnIndex("_data"))
        cursor?.close()
        return path
    }

    private fun coroutine(imgUrl : String): String{
        var str = ""
        var result :JSONObject
        CoroutineScope(Dispatchers.Main).launch{
            val temp = CoroutineScope(Dispatchers.IO).async {
                getOCR(imgUrl)
            }.await()
            println(temp)
            result = JSONObject(temp)
            println(result)
            val jsonArray = result.optJSONArray("medicine")
            var i = 0
            while(i < jsonArray.length()){
                str += (jsonArray[i] as String)
                if(i != jsonArray.length()-1)
                    str += "\n"
                i++
            }
            str = str.substring(str.lastIndexOf("/") + 1)
            binding.returnOCR.setText(str) //returnOCR 요소에 처리된 텍스트 출력
        }
        return str
    }

    private fun getOCR(imgUrl: String): String {
        val sourceFile = File(imgUrl)
        val MEDIA_TYPE = "image/jpg".toMediaTypeOrNull()
        val filename: String = imgUrl.substring(imgUrl.lastIndexOf("/") + 1)

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", filename, RequestBody.create(MEDIA_TYPE, sourceFile))
            .build()

        val request = Request.Builder()
            .url("http://34.125.3.13:8000/ocr")
            .post(body)
            .build()
        val client = OkHttpClient.Builder().build()

        client.newCall(request).execute().use{  response ->
            return if (response.body != null){
                response.body?.string().toString()
            }else{
                ""
            }
        }
    }
    //Image Processing with openCV
    private fun getGrayscaledImage(uri: Uri): Bitmap{
        //get bitmap from Uri
        val bitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source){ decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        }else{
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }

        //create openCV mat object & copy content from bitmap
        var mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        //convert to grayscale
        val grayedBitmap: Bitmap = bitmap
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)
        Utils.matToBitmap(mat, grayedBitmap)

        return grayedBitmap
    }
    private fun getGrayscaledImage_test(bitmap: Bitmap): Bitmap{
        //create openCV mat object & copy content from bitmap
        var mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        //convert to grayscale
        val grayedBitmap: Bitmap = bitmap
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)
        Utils.matToBitmap(mat, grayedBitmap)

        return grayedBitmap
    }
}
