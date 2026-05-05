package com.example.nexbitmobile.loginActivity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.nexbitmobile.R
import com.example.nexbitmobile.MainActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvMessage = findViewById<TextView>(R.id.tvMessage)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (email == "usuario@ejemplo.com" && password == "123456") {
                    tvMessage.setTextColor(Color.GREEN)
                    tvMessage.text = "Login exitoso"

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    tvMessage.setTextColor(Color.RED)
                    tvMessage.text = "Credenciales incorrectas"
                }
            } else {
                tvMessage.setTextColor(Color.RED)
                tvMessage.text = "Completa todos los campos"
            }
        }
    }
}
