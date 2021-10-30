package com.example.frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnEdit = findViewById<ImageView>(R.id.EditButton)
        btnEdit.setOnClickListener() {
            val intent = Intent(this, EditActivity::class.java)
            startActivity(intent)
        }
    }
}
