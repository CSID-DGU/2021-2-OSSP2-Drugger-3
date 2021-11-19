package com.example.frontend

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.frontend.databinding.ActivityOcrBinding
import com.example.frontend.databinding.ActivityTempBinding

class TempActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val binding: ActivityTempBinding = ActivityTempBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val input = intent.getStringExtra("input")
        binding.accepted.text=input
    }
}