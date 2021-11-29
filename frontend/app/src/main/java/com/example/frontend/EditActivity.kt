package com.example.frontend

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.CheckBox
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontend.databinding.ActivityEditBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        var allergyList = intent.getSerializableExtra("allergyList") as ArrayList<Allergy>

        var cookie: String?
        cookie = MySharedPreferences.getMyCookie(this)

        binding.plus.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.minus.setOnClickListener(){
            showAllergy(allergyList)
            /**checkbox id값 받아서 id마다 check 되었는지 확인(0부터 시작)됨, 반복문 돈다.
            if(check 되었다) check 된거 삭제
            id가 1이면 str_list[3], str_list[4], str_list[5]순으로 내용 담아서 delete 해야됨
            다 하고나서 intent mainactivity*/
            /**var temp = str_list.size/3
            for(i: Int in 0..temp-1) {
                if(i<temp){
                    val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
                    val json = JSONObject()

                    json.put("Mname", str_list[i*3])
                    json.put("Mmaterial", str_list[i*3+1])
                    json.put("Symptom", str_list[i*3+2])

                    val body = RequestBody.create(JSON, json.toString())
                    val request = Request.Builder()
                        .url("http://34.125.3.13:8000/edit")
                        .delete(body)
                        .build()
                    val client = OkHttpClient()

                    client.newCall(request).enqueue(object: okhttp3.Callback{
                        override fun onFailure(call: okhttp3.Call, e: IOException) {
                            Log.d("connection", "fail")
                        }
                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            Log.d("connection", "success")
                        }
                    })
                }
            }*/
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
    private fun showAllergy(allergyList : ArrayList<Allergy>){
        Thread(){
            run(){
                runOnUiThread(Runnable {
                    run(){
                        val mAdapter = EditRvAdapter(this@EditActivity, allergyList)
                        val mRecyclerView = binding.mRecyclerView
                        mRecyclerView.adapter = mAdapter

                        val lm = LinearLayoutManager(this@EditActivity)
                        mRecyclerView.layoutManager = lm
                        mRecyclerView.setHasFixedSize(true)
                    }
                })
            }
        }.start()
    }
}