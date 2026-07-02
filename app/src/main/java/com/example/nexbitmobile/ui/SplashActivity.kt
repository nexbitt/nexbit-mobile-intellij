package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.util.SecurePrefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.nexbitmobile.R.layout.activity_splash)

        val token = SecurePrefs.getToken(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (!token.isNullOrEmpty()) {
                validateToken(token)
            } else {
                goToCatalog()
            }
        }, 600)
    }

    private fun validateToken(token: String) {
        ApiClient.instance.getMe().enqueue(object : Callback<com.example.nexbitmobile.model.Usuario> {
            override fun onResponse(
                call: Call<com.example.nexbitmobile.model.Usuario>,
                response: Response<com.example.nexbitmobile.model.Usuario>
            ) {
                if (response.isSuccessful) {
                    val user = response.body()
                    val prefs = getSharedPreferences("app", MODE_PRIVATE)
                    prefs.edit()
                        .putInt("userId", user?.id_usuario ?: 0)
                        .putInt("rolId", user?.rol_id ?: 0)
                        .putString("userName", user?.nombre)
                        .putString("userEmail", user?.email)
                        .putString("userRole", user?.rol_nombre)
                        .apply()

                    routeByRole(user?.rol_nombre ?: "")
                } else {
                    goToCatalog()
                }
            }

            override fun onFailure(call: Call<com.example.nexbitmobile.model.Usuario>, t: Throwable) {
                goToCatalog()
            }
        })
    }

    private fun routeByRole(rolNombre: String) {
        val intent: Intent = when (rolNombre) {
            "Administrador" -> Intent(this, MainOrbixActivity::class.java)
            "Repartidor" -> Intent(this, EntregasActivity::class.java)
            else -> Intent(this, ClientMainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun goToCatalog() {
        startActivity(Intent(this, ClientMainActivity::class.java))
        finish()
    }
}
