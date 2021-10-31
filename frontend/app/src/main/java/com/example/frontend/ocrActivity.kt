package com.example.frontend

import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.frontend.databinding.ActivityOcrBinding

class ocrActivity : AppCompatActivity() {

    //ViewBinding
    lateinit var binding:ActivityOcrBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOcrBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //from MainActivity's Camera 앱, get ImageURI
        val strImageUri = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(strImageUri)

        //set ImageView
        binding.imageView4.setImageURI(imageUri)

        //ImageProcessing
    }
}