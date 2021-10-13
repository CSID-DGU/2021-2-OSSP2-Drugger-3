package com.example.frontend

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnJoin = findViewById<Button>(R.id.joinButton)
        btnJoin.setOnClickListener(){
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

    }
}