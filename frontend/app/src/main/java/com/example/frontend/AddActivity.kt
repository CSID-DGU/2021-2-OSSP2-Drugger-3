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
import com.example.frontend.databinding.ActivityAddBinding
import com.example.frontend.databinding.ActivitySearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

class AddActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddBinding
    private lateinit var loadingDialog: ProgressDialog
    private val spinnerDefault = "약물을 선택하세요"
    private val none = "검색되는 약물이 없습니다"
    private var mnameArr = ArrayList<String>()
    private var materialArr = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)

        val spinner = findViewById<Spinner>(com.example.frontend.R.id.spinner)
        val nameSpinner = findViewById<Spinner>(com.example.frontend.R.id.nameSpinner)
        spinner.adapter=ArrayAdapter.createFromResource(this, com.example.frontend.R.array.itemList, android.R.layout.simple_spinner_item)
        val cookie = MySharedPreferences.getMyCookie(this)

        binding.searchButton.setOnClickListener{
            //request & load to UI by Thread
            if(binding.drugName.text.toString().isEmpty() || binding.drugName.text.toString().isBlank()){
                Toast.makeText(this, "입력이 없습니다, \n검색하고자하는 약물을 제대로 입력해주세요!", Toast.LENGTH_SHORT).show()
            }else {
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                getResult(cookie, arrayListOf<String>(binding.drugName.text.toString()))
                loadingDialog = ProgressDialog(this, R.style.Theme_Material_Dialog_Alert)
                loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                loadingDialog.setMessage("리스트 설정하는 중...")
                loadingDialog.setCanceledOnTouchOutside(false)
                loadingDialog.show()
            }
        }

        binding.AddToEdit.setOnClickListener{
            val medicine = nameSpinner.selectedItem.toString()
            val symptom = spinner.selectedItem.toString()
            if(medicine.equals(none)){
                Toast.makeText(this, "검색할 수 있는 약물이 없습니다", Toast.LENGTH_SHORT).show()
            }else if(medicine.equals(spinnerDefault) || symptom.equals("증상")){
                Toast.makeText(this, "선택을 완료해 주세요", Toast.LENGTH_SHORT).show()
            }else{
                val index = mnameArr.indexOf(medicine)-1

                val material = materialArr[index]

                addAllergy(cookie, medicine, symptom, material)


            }
        }

        binding.Cancel.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun addAllergy(ck: String?, medicine: String, symptom: String, material: String){
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val json = JSONObject()

        json.put("Mname", medicine)
        json.put("Mmaterial", material)
        json.put("Symptom", symptom)

        val json_array = JSONArray()
        json_array.put(json)

        val body = RequestBody.create(JSON, json_array.toString())
        val request = ck?.let {
            Request.Builder()
                .url("http://34.125.3.13:8000/edit")
                .addHeader("Cookie", it)
                .post(body)
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
                        when(str){
                            "success" -> {
                                val intent = Intent(this@AddActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }else{
                        Log.d("connection", "BAD")
                    }
                }
            })
        }
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


                        var tempJsonArr: JSONArray
                        var tempJsonObj: JSONObject
                        tempJsonArr = responseArr.getJSONArray(0)
                        for(j in 0 until tempJsonArr.length()){
                            tempJsonObj = tempJsonArr.getJSONObject(j)
                            mnameArr.add(tempJsonObj.getString("mname"))
                            materialArr.add(tempJsonObj.getString("mmaterial"))
                        }


                        //update UI with result
                        loadResult()

                    } else { //not success!
                        Log.d("connection", "Bad")
                    }
                }
            })
        }
    }


    fun loadResult(){
        Thread(){
            run(){
                runOnUiThread(Runnable {
                    run(){
                        val content = findViewById<Spinner>(com.example.frontend.R.id.nameSpinner)

                        if(mnameArr.size == 0){
                            mnameArr.add(0, none)
                        }else{
                            mnameArr.add(0, spinnerDefault)
                        }
                        content.adapter = ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_spinner_item,
                            mnameArr
                        )

                        loadingDialog.dismiss()
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    }
                })
            }
        }.start()
    }

}

