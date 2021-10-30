package com.example.frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity

class EditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val btnCheck = findViewById<Button>(R.id.CheckButton)
        btnCheck.setOnClickListener() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        val btnPlus = findViewById<ImageView>(R.id.Plus)
        btnPlus.setOnClickListener() {
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
        }

    }
}