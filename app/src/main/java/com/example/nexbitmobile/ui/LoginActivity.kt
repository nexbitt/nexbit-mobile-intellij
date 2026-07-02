package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.api.SocketManager
import com.example.nexbitmobile.model.LoginRequest
import com.example.nexbitmobile.model.LoginResponse
import com.example.nexbitmobile.util.SecurePrefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Slide-up entrance animation
        val root = findViewById<View>(android.R.id.content)
        root.translationY = root.height.toFloat()
        root.animate()
            .translationY(0f)
            .setDuration(220)
            .setInterpolator(DecelerateInterpolator())
            .start()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvMessage = findViewById<TextView>(R.id.tvMessage)

        // Press feedback
        listOf(btnLogin, btnRegister).forEach { btn ->
            btn.setOnTouchListener { _, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        btn.animate().scaleX(0.97f).scaleY(0.97f).alpha(0.85f).setDuration(80).start()
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        btn.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(80).start()
                    }
                }
                false
            }
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                tvMessage.visibility = View.VISIBLE
                tvMessage.text = "Completa todos los campos"
                return@setOnClickListener
            }

            tvMessage.visibility = View.GONE

            val loginRequest = LoginRequest(email, password)

            ApiClient.instance.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()

                        val prefs = getSharedPreferences("app", MODE_PRIVATE)
                        prefs.edit()
                            .putInt("userId", loginResponse?.user?.id_usuario ?: 0)
                            .putInt("rolId", loginResponse?.user?.rol_id ?: 0)
                            .putString("userName", loginResponse?.user?.nombre)
                            .putString("userEmail", loginResponse?.user?.email)
                            .putString("userDocType", loginResponse?.user?.tipo_documento)
                            .putString("userDocNum", loginResponse?.user?.numero_documento)
                            .putString("userPhone", loginResponse?.user?.telefono)
                            .putString("userAddress", loginResponse?.user?.direccion)
                            .putString("token", loginResponse?.token)
                            .putString("userRole", loginResponse?.user?.rol_nombre)
                            .apply()

                        // Guardar token cifrado en EncryptedSharedPreferences
                        loginResponse?.token?.let { token ->
                            SecurePrefs.saveToken(this@LoginActivity, token)
                        }

                        val uid = loginResponse?.user?.id_usuario?.toString() ?: "0"
                        val role = loginResponse?.user?.rol_nombre ?: ""
                        SocketManager.connectToServer(uid, role)
                        routeByRole(loginResponse?.user?.rol_nombre ?: "", loginResponse?.user?.nombre ?: "")
                    } else {
                        showFieldError(etEmail, etPassword, tvMessage, "Credenciales incorrectas")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    showFieldError(etEmail, etPassword, tvMessage, "Error de conexión")
                }
            })
        }

        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, RecoveryActivity::class.java))
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun routeByRole(rolNombre: String, userName: String) {
        val intent = when (rolNombre) {
            "Administrador" -> Intent(this, MainOrbixActivity::class.java)
            "Repartidor" -> Intent(this, EntregasActivity::class.java)
            else -> Intent(this, ClientMainActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showFieldError(etEmail: EditText, etPassword: EditText, tvMessage: TextView, msg: String) {
        tvMessage.text = msg
        tvMessage.visibility = View.VISIBLE
        etEmail.setBackgroundResource(R.drawable.bg_input_auth_error)
        etPassword.setBackgroundResource(R.drawable.bg_input_auth_error)
    }
}
