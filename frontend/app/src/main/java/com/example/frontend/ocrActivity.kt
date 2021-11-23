package com.example.frontend

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.example.frontend.databinding.ActivityOcrBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.util.*

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

        //이 부분 수정 바람!
        val resultText = processOCR() //알맞게 넣어주기
        binding.returnOCR.setText(resultText) //returnOCR 요소에 처리된 텍스트 출력

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

    private fun processOCR(): String {
        var ocrResult: String = ""

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val json = JSONObject()

        val body = RequestBody.create(JSON,json.toString())

        val request = Request.Builder()
            .url("http://34.125.3.13:8000/ocr")
            .post(body) //body 셋팅 필요
            .build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object: okhttp3.Callback{
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("connection", "fail")
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                Log.d("connection", "success")

                if(response.isSuccessful){
                    val str = response.body?.string() //response body 값
                }else{ //not success!
                    Log.d("connection", "Bad")
                }
            }
        })

        return ocrResult
    }

}