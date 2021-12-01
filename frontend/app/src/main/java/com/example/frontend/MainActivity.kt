package com.example.frontend

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.frontend.databinding.ActivityMainBinding
import com.theartofdev.edmodo.cropper.CropImage
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.jar.Manifest
import okhttp3.*

class MainActivity : AppCompatActivity() {
    //ViewBinding
    private lateinit var binding: ActivityMainBinding
    private var allergyList = arrayListOf<Allergy>()

    //Permissions
    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val PERMISSION_REQUEST = 1

    //for 카메라 앱 실행
    private var tempImageUri: Uri? = null
    private var tempImageFilePath = ""
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()){ it ->
        if(it){
            val intent = Intent(this, CropActivity::class.java)
            intent.putExtra("imageUri", tempImageUri.toString())
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var cookie: String?
        cookie = MySharedPreferences.getMyCookie(this)

        loadInfo(cookie, this)

        //테스트용으로 resultPage로 가게끔
        binding.imageView.setOnClickListener(){
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.EditButton.setOnClickListener(){
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("allergyList", allergyList)
            startActivity(intent)
            finish()
        }

        //카메라 아이콘 클릭
        binding.camera.setOnClickListener() {
            //카메라 권한 확인
            checkPermission(PERMISSIONS, PERMISSION_REQUEST)

            //tempImage Uri & FilePath 생성
            tempImageUri = FileProvider.getUriForFile(this, "com.example.frontend.fileprovider", createImageFile().also{
                tempImageFilePath = it.absolutePath
            })
            //카메라 앱 실행
            cameraLauncher.launch(tempImageUri)
        }

        binding.logout.setOnClickListener(){
            val intent = Intent(this, LoginActivity::class.java)
            MySharedPreferences.clearUser(this)
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
                        while(i < jsonArray.length()){
                            val jsonObject = jsonArray.getJSONObject(i)
                            val material = jsonObject.getString("Mmaterial")
                            val medicine = jsonObject.getString("Mname")
                            val symptom = jsonObject.getString("Symptom")
                            allergyList.add(Allergy(medicine, material, symptom))
                            i++
                        }
                        println(allergyList)
                        showAllergy()
                    }
                }
            })
        }
    }

    private fun showAllergy(){
        Thread(){
            run(){
                runOnUiThread(Runnable {
                    run(){
                        val mAdapter = MainRvAdapter(this@MainActivity, allergyList)
                        val mRecyclerView = binding.mRecyclerView1
                        mRecyclerView.adapter = mAdapter

                        val lm = LinearLayoutManager(this@MainActivity)
                        mRecyclerView.layoutManager = lm
                        mRecyclerView.setHasFixedSize(true)
                    }
                })
            }
        }.start()
    }

    private fun createImageFile() : File{
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("temp_image", "jpg", storageDir)
    }

    //권한 관련
    private fun checkPermission(permissions: Array<String>, permissionRequest: Int): Boolean{
        val permissionList : MutableList<String> = mutableListOf()

        for(permission in permissions){
            val result = ContextCompat.checkSelfPermission(this, permission)
            if(result != PackageManager.PERMISSION_GRANTED){
                permissionList.add(permission)
            }
        }
        if(permissionList.isNotEmpty()){
            ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), PERMISSION_REQUEST)
            return false;
        }
        return true;
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var checkResult = false
        for(result in grantResults){
            if(result != PackageManager.PERMISSION_GRANTED){
                checkResult = true
            }
        }
        if(checkResult){
            Toast.makeText(this, "카메라 권한 승인이 필요합니다. [앱 설정]에서 확인해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}