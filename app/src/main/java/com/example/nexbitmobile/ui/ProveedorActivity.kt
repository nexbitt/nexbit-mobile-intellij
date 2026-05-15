package com.example.nexbitmobile.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Proveedor
import com.example.nexbitmobile.model.ProveedorResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProveedorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_proveedor)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val txtNit = findViewById<EditText>(R.id.txtNit)
        val txtNombre = findViewById<EditText>(R.id.txtNombre)
        val txtCorreo = findViewById<EditText>(R.id.txtCorreo)
        val txtTelefono = findViewById<EditText>(R.id.txtTelefono)
        val txtDireccion = findViewById<EditText>(R.id.txtDireccion)

        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        btnGuardar.setOnClickListener {
            val nit = txtNit.text.toString().trim()
            val nombre = txtNombre.text.toString().trim()
            
            if (nit.isEmpty() || nombre.isEmpty()) {
                Toast.makeText(this, "El NIT y Nombre son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val proveedor = Proveedor(
                nit = nit,
                nombre = nombre,
                correo = txtCorreo.text.toString().trim(),
                telefono = txtTelefono.text.toString().trim(),
                direccion = txtDireccion.text.toString().trim()
            )

            // Obtener token guardado en SharedPreferences
            val prefs = getSharedPreferences("app", Context.MODE_PRIVATE)
            val savedToken = prefs.getString("token", "") ?: ""
            
            if (savedToken.isEmpty()) {
                Toast.makeText(this, "No hay token de autenticación. Inicie sesión nuevamente.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ApiClient.instance.createProveedor(proveedor)
                .enqueue(object : Callback<ProveedorResponse> {

                    override fun onResponse(
                        call: Call<ProveedorResponse>,
                        response: Response<ProveedorResponse>
                    ) {
                        Log.d("RESPUESTA", response.code().toString())

                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@ProveedorActivity,
                                "Proveedor guardado exitosamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            
                            // Limpiar campos después de guardar
                            txtNit.setText("")
                            txtNombre.setText("")
                            txtCorreo.setText("")
                            txtTelefono.setText("")
                            txtDireccion.setText("")
                        } else {
                            Log.d("ERROR", response.errorBody()?.string().toString())
                            Toast.makeText(
                                this@ProveedorActivity,
                                "Error al guardar: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<ProveedorResponse>, t: Throwable) {
                        Log.e("RETROFIT", t.message.toString())
                        Toast.makeText(
                            this@ProveedorActivity,
                            "Error de conexión: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}
