package com.example.frontend

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.frontend.databinding.ActivityEditBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.IOException
import java.time.format.TextStyle

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private var allergy_list = ArrayList<String>();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        allergy_list = intent.getSerializableExtra("allergyList") as ArrayList<String>

        val cookie = MySharedPreferences.getMyCookie(this)
        showAllergy()

        binding.plus.setOnClickListener(){
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.minus.setOnClickListener(){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("주의")
                .setMessage("정말로 삭제하시겠습니까?")
                .setPositiveButton("아니오", DialogInterface.OnClickListener{dialog, which->
                })
                .setNegativeButton("예", DialogInterface.OnClickListener({dialog, which ->
                    removeAllergy(cookie, this)
                }))
            builder.show()
        }

    }

    private fun removeAllergy(cookie: String?, context: Context){
        val tableLayout = findViewById<TableLayout>(R.id.table_edit)
        var row: TableRow
        var ischeck: CheckBox
        var medicine: TextView
        var material: TextView
        var symptom: TextView
        var count = 0
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        var json_array = JSONArray()
        for(i in 0 until tableLayout.childCount){
            row = tableLayout.getChildAt(i) as TableRow
            ischeck = row.getChildAt(0) as CheckBox
            medicine = row.getChildAt(1) as TextView
            material = row.getChildAt(2) as TextView
            symptom = row.getChildAt(3) as TextView
            if(ischeck.isChecked){
                println(medicine.text.toString())
                println(material.text.toString())
                println(symptom.text.toString())
                val json = JSONObject()

                json.put("Mname", medicine.text.toString())
                json.put("Mmaterial", material.text.toString())
                json.put("Symptom", symptom.text.toString())
                print(json.toString())
                json_array.put(json)
                count++
            }
        }
        val body = RequestBody.create(JSON, json_array.toString())
        if(cookie != null) {
            val request = Request.Builder()
                .addHeader("Cookie", cookie)
                .url("http://34.125.3.13:8000/edit")
                .put(body)
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
                        print("response : ")
                        println(response)
                    }
                }
            })
        }
        if (count>0)
            toast("정상적으로 제거되었습니다.")
        else
            toast( "선택한 것이 없습니다.")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun showAllergy(){
        Thread(){
            run(){
                runOnUiThread(Runnable {
                    run(){

                        val tableLayout = findViewById<TableLayout>(R.id.table_edit)

                        val rowLayoutParams = TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.MATCH_PARENT
                        )
                        val temp = allergy_list.size/3
                        for (i : Int in 0 .. temp-1) {
                            var tr = TableRow(this@EditActivity)
                            val textViewLayoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.FILL_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                            )
                            tr.setBackgroundColor(Color.WHITE)
                            var t1 = CheckBox(this@EditActivity)
                            var t2 = TextView(this@EditActivity)
                            t2.setText(allergy_list[i*3])
                            var t3 = TextView(this@EditActivity)
                            t3.setText(allergy_list[i*3+1])
                            var t4 = TextView(this@EditActivity)
                            t4.setText(allergy_list[i*3+2])
                            tr.addView(t1, textViewLayoutParams)
                            tr.addView(t2, textViewLayoutParams)
                            tr.addView(t3, textViewLayoutParams)
                            tr.addView(t4, textViewLayoutParams)
                            tableLayout.addView(tr, rowLayoutParams)
                        }
                    }
                })
            }
        }.start()
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
}