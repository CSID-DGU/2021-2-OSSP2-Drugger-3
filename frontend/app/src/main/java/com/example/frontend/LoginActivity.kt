package com.example.frontend

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.frontend.databinding.ActivityLoginBinding
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)
        //자동 로그인 점검
        if(!(MySharedPreferences.getMyCookie(this).isNullOrBlank())){
            //비어있지 않은 경우
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
            //비어있는 경우
            binding.joinButton.setOnClickListener() {
                val intent = Intent(this, JoinActivity::class.java)
                startActivity(intent)
                finish()
            }

            binding.loginButton.setOnClickListener() {

                val userId = binding.userID.text.toString()
                val userPw = binding.userPW.text.toString()

                //미입력 처리
                if (userId.isEmpty() || userId.isBlank() || userPw.isEmpty() || userPw.isBlank()) {
                    Toast.makeText(this, "입력을 완료해주세요.", Toast.LENGTH_SHORT).show()
                }

                //request login
                login(userId, userPw, this)
            }
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

    private fun login(id: String, pw: String, context: Context) {
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val json = JSONObject()

        json.put("Id", id)
        json.put("Pw", pw)

        val body = RequestBody.create(JSON,json.toString())
        val request = Request.Builder()
            .url("http://34.125.3.13:8000/login")
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
                            val cookie: String? = response.headers["Set-Cookie"]
                            val intent = Intent(context, MainActivity::class.java)
                            MySharedPreferences.setMyCookie(context, cookie)
                            startActivity(intent)
                            finish()
                        }
                        "check id" -> toast("아이디를 확인하세요.")
                        "check pwd" -> toast("비밀번호를 확인하세요.")
                        else -> toast("Fail!")
                    }
                }else{ //not success!
                    toast("Fail!")
                }
            }
        })
    }
}