package com.example.frontend

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnbutPopup = findViewById<Button>(R.id.butPopup)
        btnbutPopup.setOnClickListener{
            showSettingPopup()
        }

        val btnEdit = findViewById<ImageView>(R.id.EditButton)
        btnEdit.setOnClickListener() {
            val intent = Intent(this, EditActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showSettingPopup() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.alert_popup, null)

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("              로그아웃 하시겠습니까?")
            .create()

        val butSave = view.findViewById<Button>(R.id.butSave)  // 요렇게 써도 됨
        butSave.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val butCancel = view.findViewById<Button>(R.id.butCancel)
        butCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.setView(view)
        alertDialog.show()

    }
}
