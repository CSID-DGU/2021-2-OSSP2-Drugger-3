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
import com.example.frontend.databinding.ActivityEditBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private var str_list = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var cookie: String?
        cookie = MySharedPreferences.getMyCookie(this)

        loadInfo(cookie, this)

        binding.plus.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.minus.setOnClickListener(){
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
    private fun loadInfo(cookie: String?, context: Context){
        val request = cookie?.let {
            Request.Builder()
                .url("http://34.125.3.13:8000/main")
                .get()
                .addHeader("Cookie", it)
                .build()
        }

        val client = OkHttpClient()

        if (request != null) {
            client.newCall(request).enqueue(object: okhttp3.Callback{
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.d("connection", "fail")
                }
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    Log.d("connection", "success")

                    if(response.isSuccessful){
                        val str = response.body?.string()
                        println("str1 : ${str}")
                        val allergy = JSONObject(str)
                        val jsonArray = allergy.optJSONArray("result")
                        var i = 0
                        var tableLayout = binding.tableLayout
                        while(i < jsonArray.length()){
                            val jsonObject = jsonArray.getJSONObject(i)
                            val material = jsonObject.getString("Mmaterial")
                            val medicine = jsonObject.getString("Mname")
                            val symptom = jsonObject.getString("Symptom")
                            str_list.add(medicine)
                            str_list.add(material)
                            str_list.add(symptom)
                            i++
                        }
                        println(str_list)
                        showAllergy(tableLayout, str_list)
                    }
                }
            })
        }
    }
    private fun showAllergy(tableLayout: TableLayout, str_list: ArrayList<String>){
        Thread(){
            run(){
                runOnUiThread(Runnable {
                    run(){
                        var temp = str_list.size/3
                        for (i : Int in 0 .. temp-1) {
                            var tr = TableRow(this@EditActivity)
                            tr.setLayoutParams(
                                TableRow.LayoutParams(
                                    TableRow.LayoutParams.FILL_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                                )
                            )
                            var t1 = CheckBox(this@EditActivity)
                            t1.id = i
                            //id 값 설정해서 나중에 확인 했을 때 id 값 받아오기
                            //이부분 마저 구현해야 됨


                            var t2 = TextView(this@EditActivity)
                            t2.setText(str_list[i*3])
                            var t3 = TextView(this@EditActivity)
                            t3.setText(str_list[i*3+1])
                            var t4 = TextView(this@EditActivity)
                            t4.setText(str_list[i*3+2])

                            tr.addView(t1)
                            tr.addView(t2)
                            tr.addView(t3)
                            tr.addView(t4)
                            tableLayout.addView(tr)
                            //println(str_list[i*3+2])
                        }
                        var tr = TableRow(this@EditActivity)
                        tr.setLayoutParams(
                            TableRow.LayoutParams(
                                TableRow.LayoutParams.FILL_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                            )
                        )
                        var t1 = TextView(this@EditActivity)
                        t1.setText("   ")
                        var t2 = TextView(this@EditActivity)
                        t2.setText("   ")
                        var t3 = TextView(this@EditActivity)
                        t3.setText("   ")
                        var t4 = TextView(this@EditActivity)
                        t4.setText("   ")
                        tr.addView(t1)
                        tr.addView(t2)
                        tr.addView(t3)
                        tr.addView(t4)
                        tableLayout.addView(tr)
                        //println(str_list[i*3+2])

                    }
                })
            }
        }.start()
    }
}