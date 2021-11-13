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
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.frontend.databinding.ActivityMainBinding
import com.theartofdev.edmodo.cropper.CropImage
import okhttp3.Call
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    var str1 = ""
    var request = com.example.frontend.request.request

    //ViewBinding
    private lateinit var binding: ActivityMainBinding

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
        var allergyrequest = request
            .url("http://34.125.3.13:8000/main")
            .build()
        println("here1")
        HttpClient.client.newCall(allergyrequest).enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                println("error")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    str1 = response!!.body()!!.string()
                    println("str1 : ${str1}")
                    val allergy = JSONObject(str1)
                    val jsonArray = allergy.optJSONArray("result")
                    var i = 0
                    var str_list = ArrayList<String>();
                    while(i < jsonArray.length()){
                        val jsonObject = jsonArray.getJSONObject(i)
                        val material = jsonObject.getString("Mmaterial")
                        val medicine = jsonObject.getString("Mname")
                        val symptom = jsonObject.getString("Symptom")
                        println("material : ${material}")
                        println("medicine : ${medicine}")
                        println("symptom : ${symptom}")
                        str_list.add(medicine)
                        str_list.add(material)
                        str_list.add(symptom)
                    }
                    //println(str_list.toString())
                   // println(str1.toString())
                    println("here2")

                }
            }

        })

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