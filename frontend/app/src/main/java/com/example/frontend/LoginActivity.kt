package com.example.frontend

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.example.frontend.HttpClient.client
import com.example.frontend.databinding.ActivityLoginBinding
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import okhttp3.*

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    var str = ""
    var islogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val etid = findViewById<EditText>(R.id.userID)
        val etpw = findViewById<EditText>(R.id.userPW)
        binding.joinButton.setOnClickListener(){
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener() {
            var isExistBlank = false
            val Id = etid.text.toString();
            val Pw = etpw.text.toString();
            val json = JSONObject()
            json.put("Id", Id)
            json.put("Pw", Pw)
            val body = RequestBody.create(JSON,json.toString())
            var loginrequest = Request.Builder()
                .url("http://34.125.3.13:8000/login")
                .post(body)
                .build()
            var client1 = HttpClient.client
            var dialog = AlertDialog.Builder(this@LoginActivity)
            //handler
            if(Id.isEmpty() || Pw.isEmpty()){
                isExistBlank = true
            }
            if(isExistBlank){
                //알림메시지 띄우는 것
                showPopup()
            }
            else {
                client1.newCall(loginrequest).enqueue(object : okhttp3.Callback{
                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        var dialog = AlertDialog.Builder(this@LoginActivity)
                        dialog.setTitle("에러")
                        dialog.setMessage("호출실패했습니다.")
                        dialog.show()
                    }

                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        str = response!!.body()!!.string()
                        if (str == "success") {
                            val intent = Intent(binding.loginButton.context, MainActivity::class.java)
                            startActivity(intent)
                        } else{
                            showdialog(str)
                        }
                        str = ""
                    }
                })
            }
        }
    }

    private fun showPopup(){
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.alert_popup, null)
        var textView: TextView = view.findViewById<EditText>(R.id.textView)
        textView.text = "아이디와 비밀번호 모두 입력하시오"
        var alertDialog = AlertDialog.Builder(this)
            .setTitle("에러")
            .setPositiveButton("확인", null)
            .create()
        alertDialog.setView(view)
        alertDialog.show()

    }
    private fun showdialog(str: String){
        Thread(){
            run(){
                runOnUiThread(Runnable {
                    run(){
                        if (str == "check id") {
                            println("str : $str")
                            Toast.makeText(applicationContext, "아이디를 확인하십시오", Toast.LENGTH_SHORT).show()
                        } else if (str == "check pwd") {
                            println("str : $str")
                            Toast.makeText(applicationContext, "비밀번호를 확인하십시오", Toast.LENGTH_SHORT).show()
                        } else {
                            println("str : $str")
                            Toast.makeText(applicationContext, "아이디와 비밀번호를 확인하십시오", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        }.start()
    }
}