package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R

class ClientProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_profile)

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val name = prefs.getString("userName", "Cliente") ?: "Cliente"
        val email = prefs.getString("userEmail", "") ?: ""
        val avatarUrl = prefs.getString("userAvatar", "") ?: ""

        val ivAvatar = findViewById<ImageView>(R.id.ivProfileAvatar)
        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvCategory = findViewById<TextView>(R.id.tvProfileCategory)
        val tvEmail = findViewById<TextView>(R.id.tvProfileEmail)

        tvName.text = name
        tvCategory.text = "Cliente Silver"

        if (email.isNotEmpty()) {
            tvEmail.text = email
            tvEmail.visibility = android.view.View.VISIBLE
        }

        if (avatarUrl.isNotEmpty()) {
            Glide.with(this).load(avatarUrl).circleCrop().into(ivAvatar)
        }

        findViewById<LinearLayout>(R.id.rowMisPedidos).setOnClickListener {
            startActivity(Intent(this, MisPedidosActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.rowDirecciones).setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.rowFacturas).setOnClickListener {
            startActivity(Intent(this, MisPedidosActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.rowPago).setOnClickListener {
        }

        findViewById<android.widget.Button>(R.id.btnLogout).setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, CatalogoActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        findViewById<ImageView>(R.id.btnProfileBack).setOnClickListener { finish() }
    }
}
