package com.example.frontend

import android.R
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.text.Layout.Alignment
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.example.frontend.databinding.ActivitySearchBinding
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class SearchActivity : AppCompatActivity() {

    lateinit var binding: ActivitySearchBinding
    private lateinit var loadingDialog: ProgressDialog
    private val spinnerDefault = "약물을 선택하세요"
    private val none = "검색되는 약물이 없습니다"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)

        val cookie = MySharedPreferences.getMyCookie(this)

        //val searchTargets = intent.getStringArrayListExtra("search") //Main 검색 or OCR 스캔 검색
        val searchTargets = arrayListOf<String>("낙센", "아스피린", "게보린", "타이레놀")

        if(searchTargets != null){
            //request & load to UI by Thread
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            getResult(cookie, searchTargets)

            //Just load Medicine Name with dynamic View generation
            loadMedicine(searchTargets)

            //loading Dialog 설정 및 표시
            loadingDialog = ProgressDialog(this, R.style.Theme_Material_Dialog_Alert)
            loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            loadingDialog.setMessage("리스트 설정하는 중...")
            loadingDialog.setCanceledOnTouchOutside(false)
            loadingDialog.show()
        }else{
            Toast.makeText(this, "결과가 없습니다.", Toast.LENGTH_SHORT).show()
        }

        //완료버튼 클릭 to Main
        binding.toResult.setOnClickListener {

            val confirm = confirmSelected()

            if(confirm.contains(spinnerDefault)){
                Toast.makeText(this, "선택을 완료해주세요", Toast.LENGTH_SHORT).show()
            }
            else if(confirm.size == 0){
                Toast.makeText(this, "검색할 수 있는 약물이 없습니다", Toast.LENGTH_SHORT).show()
            }
            else{
                val intent = Intent(this, ResultActivity::class.java)
                intent.putStringArrayListExtra("confirm", confirm)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun confirmSelected(): ArrayList<String>{
        val tableLayout = binding.table
        var row: TableRow
        var spinner: Spinner

        val trim = ArrayList<String>()

        for(i in 0 until tableLayout.childCount) {
            row = tableLayout.getChildAt(i) as TableRow
            spinner = row.getChildAt(1) as Spinner

            val selected = spinner.selectedItem.toString()
            if(selected != none)
                trim.add(selected)
        }

        return trim
    }

    private fun getResult(ck: String?, args: ArrayList<String>){
        val urlBuilder: HttpUrl.Builder = "http://34.125.3.13:8000/search".toHttpUrl().newBuilder()
        args.forEach {
            urlBuilder.addQueryParameter("mname", it)
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
                        val response = response.body?.string()
                        val responseArr = JSONArray(response)

                        var eachResultArr = ArrayList<ArrayList<String>>()

                        var tempJsonArr: JSONArray
                        var tempJsonObj: JSONObject
                        for(i in 0 until responseArr.length()) {
                            var tempStrArr = ArrayList<String>()
                            tempJsonArr = responseArr.getJSONArray(i)
                            for(j in 0 until tempJsonArr.length()){
                                tempJsonObj = tempJsonArr.getJSONObject(j)
                                tempStrArr.add(tempJsonObj.getString("mname"))
                            }
                            eachResultArr.add(tempStrArr)
                        }

                        println(eachResultArr)

                        //update UI with result
                        loadResult(eachResultArr)

                    } else { //not success!
                        Log.d("connection", "Bad")
                    }
                }
            })
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun loadMedicine(args: ArrayList<String>){
        val tableLayout = findViewById<TableLayout>(com.example.frontend.R.id.table)

        val rowLayoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        rowLayoutParams.setMargins(0, 2, 0, 2)
        val textViewLayoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.MATCH_PARENT
        )

        val len = args.size
        for(i in 0 until len){
            val row = TableRow(this)
            val label = TextView(this)
            val content = Spinner(this)

            //set row
            row.setBackgroundColor(Color.WHITE)

            //set label
            val str = args[i]
            label.text = str
            label.textSize = 16F
            label.textAlignment = View.TEXT_ALIGNMENT_CENTER
            label.gravity = Gravity.CENTER_VERTICAL

            //set spinner
            val popUp = Spinner::class.java.getDeclaredField("mPopup")
            popUp.isAccessible = true

            val window = popUp.get(content) as ListPopupWindow
            window.height = 700

            //add
            row.addView(label, textViewLayoutParams)
            row.addView(content)

            tableLayout.addView(row, rowLayoutParams)
        }
    }

    fun loadResult(result: ArrayList<ArrayList<String>>){
        Thread(){
            run(){
                runOnUiThread(Runnable {
                    run(){
                        val tableLayout = findViewById<TableLayout>(com.example.frontend.R.id.table)
                        var row: TableRow
                        var content: Spinner

                        for(i in 0 until tableLayout.childCount) {
                            row = tableLayout.getChildAt(i) as TableRow
                            content = row.getChildAt(1) as Spinner

                            if(result[i].size == 0){
                                result[i].add(0, none)
                            }
                            else{
                                result[i].add(0, spinnerDefault)
                            }
                            content.adapter = ArrayAdapter<String>(
                                this,
                                android.R.layout.simple_spinner_dropdown_item,
                                result[i]
                            )
                            content.setSelection(0) //defalut
                        }
                        loadingDialog.dismiss()
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    }
                })
            }
        }.start()
    }
}