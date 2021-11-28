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
import java.io.*

class ocrActivity : AppCompatActivity() {
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

        binding.inputImg.setImageURI(imageUri)

        val contentUri = getUri(imageUri)
        val realPath = getAbPath(contentUri)
        var resultText = ""

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
                //inputText -> selectActivity로 전달
                val intent = Intent(this, TempActivity::class.java)
                intent.putExtra("input", inputText)
                startActivity(intent)
            }
        }
    }

    private fun getUri(uri: Uri): Uri {
        // URI -> Bitmap -> MediaStore를 이용해 external storage에 저장
        val bitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            // https://developer.android.com/reference/android/graphics/ImageDecoder
            // CvException [org.opencv.core.CvException: OpenCV(4.1.1) /build/master_pack-android/opencv/modules/java/generator/src/cpp/utils.cpp:38: error: (-215:Assertion failed) AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0
            /*
                  By default, a Bitmap created by ImageDecoder (including one that is inside a Drawable)
                  will be immutable (i.e. Bitmap#isMutable returns false), and it will typically
                  have Config Bitmap.Config#HARDWARE. Although these properties can be change
                  with setMutableRequired(true)
                 */
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source){ decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        }else{
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }

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
        var result = ""
        CoroutineScope(Dispatchers.Main).launch{
            result = CoroutineScope(Dispatchers.IO).async {
                getOCR(imgUrl)
            }.await()
            //이 부분 수정 바람
            binding.returnOCR.setText(result) //returnOCR 요소에 처리된 텍스트 출력
        }
        return result
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
            .url("http://http://34.125.3.13:8000/ocr")
            .post(body) //body 셋팅 필요
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

}