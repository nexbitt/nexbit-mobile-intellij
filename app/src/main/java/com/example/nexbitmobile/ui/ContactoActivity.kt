package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.nexbitmobile.R
import com.example.nexbitmobile.util.LanguageHelper

class ContactoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacto)

        findViewById<TextView>(R.id.tvTitle).text = LanguageHelper.getString(this, "soporte_title")
        findViewById<TextView>(R.id.tvDesc).text = LanguageHelper.getString(this, "soporte_desc")
        findViewById<TextView>(R.id.tvEmail).text = LanguageHelper.getString(this, "soporte_email")
        findViewById<TextView>(R.id.tvTel).text = LanguageHelper.getString(this, "soporte_tel")
        findViewById<TextView>(R.id.btnChat).text = LanguageHelper.getString(this, "soporte_chat")

        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<TextView>(R.id.btnChat).setOnClickListener {
            // Opens ChatActivity (generic) - could be enhanced with a specific support conversation
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }
}
