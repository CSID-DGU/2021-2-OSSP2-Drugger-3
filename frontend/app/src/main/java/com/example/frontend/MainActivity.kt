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
import java.io.File
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

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
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()){
        if(it){
            val intent = Intent(this, ocrActivity::class.java)
            intent.putExtra("imageUri", tempImageUri.toString())
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //카메라 아이콘 클릭
        binding.camera.setOnClickListener() {
            //카메라 권한 확인
            checkPermission(PERMISSIONS, PERMISSION_REQUEST)

            //tempImage Uri & FilePath 생성
            tempImageUri = FileProvider.getUriForFile(this, "com.example.frontend.fileprovider", createImgaeFile().also{
                tempImageFilePath = it.absolutePath
            })
            //카메라 앱 실행
            cameraLauncher.launch(tempImageUri)
        }
    }

    private fun createImgaeFile() : File{
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