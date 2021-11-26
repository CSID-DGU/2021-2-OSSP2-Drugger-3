package com.example.frontend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val spinner = findViewById<Spinner>(R.id.spinner)
        spinner.adapter=ArrayAdapter.createFromResource(this, R.array.itemList, android.R.layout.simple_spinner_item)

        val btnconfirm = findViewById<TextView>(R.id.confirm)
        btnconfirm.setOnClickListener() {
            val intent = Intent(this, EditActivity::class.java)
            startActivity(intent)
        }

        val btncheck = findViewById<TextView>(R.id.checkButton)
        btncheck.setOnClickListener() {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        val btncancel = findViewById<TextView>(R.id.cancel)
        btncancel.setOnClickListener() {
            val intent = Intent(this, EditActivity::class.java)
            startActivity(intent)
        }
    }
}