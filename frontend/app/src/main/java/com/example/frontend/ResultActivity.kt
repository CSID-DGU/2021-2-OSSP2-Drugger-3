package com.example.frontend

import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import com.example.frontend.databinding.ActivityJoinBinding
import com.example.frontend.databinding.ActivityResultBinding
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread
import kotlin.reflect.typeOf

class ResultActivity : AppCompatActivity() {

    lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)

        val cookie = intent.getStringExtra("cookie")

        //val targets = intent.getStringArrayListExtra("confirm") //searchPage에서 넘어올 때
        //val targets = arrayListOf<String>("게보린에프정", "낙센정") //약물 이름으로 입력
        val targets = arrayListOf<String>( //for Testing, 약물 주성분 입력
            "이부프로펜", "낙프록센", "아목시실린")
        /*

            , "케토롤락", "세파클러",
            "나부메톤", "세푸록심", "세프프로질", "아세클로페낙", "록소프로펜",
            "레보플록사신", "프록시캄", "아스피린", "암피실린", "세파드록실",
            "오플록사신", "잘토프로펜")


         */ //this is testing for scrolling


        //request & load to UI by Thread
        getResult(cookie, targets)

        //Just load Medicine Name with dinamic View generation
        loadMedicine(targets)
        Toast.makeText(this,"결과 받아오는 중...", Toast.LENGTH_SHORT).show()

        //완료버튼 클릭 to Main
        binding.toMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun getResult(ck: String?, args: ArrayList<String>){
        val urlBuilder: HttpUrl.Builder = "http://34.125.3.13:8000/analysis".toHttpUrl().newBuilder()
        args.forEach {
            urlBuilder.addQueryParameter("mmaterial", it)
        }
        val url = urlBuilder.build()

        if (ck != null) {
            val request: Request = Request.Builder()
                .url(url)
                .addHeader("Cookie", ck)
                .build()
            val client = OkHttpClient()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.d("connection", "fail")
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    Log.d("connection", "success")

                    if (response.isSuccessful) {
                        //response to result & stat
                        val response = response.body?.string()
                        val responseArr = JSONArray(response)

                        var result = ArrayList<String>()
                        var stat = ArrayList<String>()
                        for(i in 0 until responseArr.length()){
                            val elem = responseArr.getJSONObject(i)
                            result.add(elem.getString("material"))
                            stat.add(elem.getString("status"))
                        }

                        //update UI with result
                        loadResult(result, stat)
                    } else { //not success!
                        Log.d("connection", "Bad")
                    }
                }
            })
        }
    }

    private fun loadMedicine(args: ArrayList<String>){
        val tableLayout = findViewById<TableLayout>(R.id.table)

        val rowLayoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.MATCH_PARENT
        )
        val textViewLayoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            100
        )
        val len = args.size
        for(i in 0 until len){
            val row = TableRow(this)
            val content = TextView(this)

            val str = "\t\t${args[i]}"
            content.text = str
            content.textSize = 16F
            content.gravity = Gravity.CENTER_VERTICAL
            content.setBackgroundColor(Color.WHITE)

            row.addView(content, textViewLayoutParams)
            tableLayout.addView(row, rowLayoutParams)
        }
    }

    fun loadResult(result: ArrayList<String>, status: ArrayList<String>){
        Thread(){
            run(){
                runOnUiThread(Runnable {
                    run(){
                        val tableLayout = findViewById<TableLayout>(R.id.table)
                        var row: TableRow
                        var content: TextView
                        var str: String

                        for(i in 0 until tableLayout.childCount){
                            row = tableLayout.getChildAt(i) as TableRow
                            content = row.getChildAt(0) as TextView

                            str = content.text.toString()
                            str += " (${result[i]})"
                            content.text = str

                            if(status[i] == "DUPLICATE"){
                                content.setBackgroundColor(Color.parseColor("#FF7A5E"))
                                content.setTextColor(Color.WHITE)
                            }else{
                                content.setBackgroundColor(Color.WHITE)
                                content.setTextColor(Color.BLACK)
                            }
                        }
                    }
                })
            }
        }.start()
    }
}
