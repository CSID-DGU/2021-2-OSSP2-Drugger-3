package com.example.frontend

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.frontend.databinding.ActivityJoinBinding
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException

class JoinActivity : AppCompatActivity() {

    lateinit var binding:ActivityJoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)

        binding.button.setOnClickListener() {
            val userId = binding.id.text.toString()
            val userPw = binding.pw.text.toString()

            //미입력 처리
            if(userId.isEmpty() || userId.isBlank() || userPw.isEmpty() || userPw.isBlank()){
                Toast.makeText(this, "입력을 완료해주세요.", Toast.LENGTH_SHORT).show()
            }else{
                join(userId, userPw, this)
            }

            //request login

        }
    }

    private fun toast(str: String){
        Thread(){
            run(){
                runOnUiThread(Runnable {
                    run(){
                        Toast.makeText(applicationContext, str, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }.start()
    }

    private fun join(id: String, pw: String, context: Context){
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val json = JSONObject()

        json.put("Id", id)
        json.put("Pw", pw)

        val body = RequestBody.create(JSON,json.toString())
        val request = Request.Builder()
            .url("http://34.125.3.13:8000/SignIn")
            .post(body)
            .build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object: okhttp3.Callback{
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("connection", "fail")
                toast("Fail!")
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                Log.d("connection", "success")

                if(response.isSuccessful){
                    val str = response.body?.string()
                    when(str){
                        "success" -> {
                            toast("회원가입이 완료되었습니다!")

                            val intent = Intent(context, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        "duplicate" -> toast("이미 존재하는 아이디입니다.")
                        else -> toast("Fail!")
                    }
                }else{ //not success!
                    toast("Fail!")
                }
            }
        })
    }
}