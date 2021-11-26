package com.example.frontend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val spinner = findViewById<Spinner>(R.id.spinner)
        spinner.adapter=ArrayAdapter.createFromResource(this, R.array.searchlist, android.R.layout.simple_spinner_item)

        val spinner2 = findViewById<Spinner>(R.id.spinner2)
        spinner2.adapter=ArrayAdapter.createFromResource(this, R.array.searchlist, android.R.layout.simple_spinner_item)

        val spinner3 = findViewById<Spinner>(R.id.spinner3)
        spinner3.adapter=ArrayAdapter.createFromResource(this, R.array.searchlist, android.R.layout.simple_spinner_item)

        val spinner4 = findViewById<Spinner>(R.id.spinner4)
        spinner4.adapter=ArrayAdapter.createFromResource(this, R.array.searchlist, android.R.layout.simple_spinner_item)

        val spinner5 = findViewById<Spinner>(R.id.spinner5)
        spinner5.adapter=ArrayAdapter.createFromResource(this, R.array.searchlist, android.R.layout.simple_spinner_item)

        val spinner6 = findViewById<Spinner>(R.id.spinner6)
        spinner6.adapter=ArrayAdapter.createFromResource(this, R.array.searchlist, android.R.layout.simple_spinner_item)

        val spinner7 = findViewById<Spinner>(R.id.spinner7)
        spinner7.adapter=ArrayAdapter.createFromResource(this, R.array.searchlist, android.R.layout.simple_spinner_item)

        val btnconfirm2 = findViewById<TextView>(R.id.confirm2)
        btnconfirm2.setOnClickListener() {
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
        }

        val btncancel2 = findViewById<TextView>(R.id.cancel2)
        btncancel2.setOnClickListener() {
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
        }
    }
}