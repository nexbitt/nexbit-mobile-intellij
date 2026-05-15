package com.example.nexbitmobile.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PerfilPruebaActivity : AppCompatActivity() {

    private lateinit var etId: EditText
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etDocType: EditText
    private lateinit var etDocNum: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil_prueba)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias a campos
        etId = findViewById(R.id.etCrudId)
        etName = findViewById(R.id.etCrudName)
        etEmail = findViewById(R.id.etCrudEmail)
        etPassword = findViewById(R.id.etCrudPassword)
        etDocType = findViewById(R.id.etCrudDocType)
        etDocNum = findViewById(R.id.etCrudDocNum)
        etPhone = findViewById(R.id.etCrudPhone)
        etAddress = findViewById(R.id.etCrudAddress)
        tvStatus = findViewById(R.id.tvCrudStatus)

        val btnSave = findViewById<Button>(R.id.btnCrudSave)
        val btnSearch = findViewById<Button>(R.id.btnCrudSearch)
        val btnEdit = findViewById<Button>(R.id.btnCrudEdit)
        val btnDelete = findViewById<Button>(R.id.btnCrudDelete)
        val btnClear = findViewById<Button>(R.id.btnCrudClear)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // ═══════════════════════════════════════════════
        // CREAR USUARIO
        // ═══════════════════════════════════════════════
        btnSave.setOnClickListener {
            val nombre = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showStatus("⚠ Nombre, email y contraseña son obligatorios", true)
                return@setOnClickListener
            }

            if (password.length < 6) {
                showStatus("⚠ La contraseña debe tener al menos 6 caracteres", true)
                return@setOnClickListener
            }

            val req = UsuarioCreateRequest(
                rol_id = 2, // Cliente por defecto
                nombre = nombre,
                email = email,
                password = password,
                tipo_documento = etDocType.text.toString().trim().takeIf { it.isNotEmpty() },
                numero_documento = etDocNum.text.toString().trim().takeIf { it.isNotEmpty() },
                telefono = etPhone.text.toString().trim().takeIf { it.isNotEmpty() },
                direccion = etAddress.text.toString().trim().takeIf { it.isNotEmpty() }
            )

            showStatus("Creando usuario...", false)

            ApiClient.instance.createUsuario(req).enqueue(object : Callback<UsuarioCreateResponse> {
                override fun onResponse(call: Call<UsuarioCreateResponse>, response: Response<UsuarioCreateResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val newId = body?.id_usuario ?: 0
                        etId.setText(newId.toString())
                        showStatus("✓ Usuario creado exitosamente (ID: $newId)", false)
                        Toast.makeText(this@PerfilPruebaActivity, "¡Usuario creado con ID: $newId!", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMsg = when (response.code()) {
                            400 -> "Datos inválidos o faltantes"
                            401 -> "Token inválido. Inicie sesión nuevamente"
                            409 -> "El correo ya está registrado"
                            else -> "Error del servidor (${response.code()})"
                        }
                        showStatus("✗ $errorMsg", true)
                    }
                }
                override fun onFailure(call: Call<UsuarioCreateResponse>, t: Throwable) {
                    showStatus("✗ Error de conexión: ${t.message}", true)
                }
            })
        }

        // ═══════════════════════════════════════════════
        // BUSCAR USUARIO
        // ═══════════════════════════════════════════════
        btnSearch.setOnClickListener {
            val idStr = etId.text.toString().trim()
            if (idStr.isEmpty()) {
                showStatus("⚠ Ingresa un ID para buscar", true)
                return@setOnClickListener
            }

            val id = idStr.toIntOrNull()
            if (id == null || id <= 0) {
                showStatus("⚠ ID debe ser un número positivo", true)
                return@setOnClickListener
            }

            showStatus("Buscando usuario #$id...", false)

            ApiClient.instance.getUsuario(id).enqueue(object : Callback<Usuario> {
                override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        if (user != null) {
                            etName.setText(user.nombre)
                            etEmail.setText(user.email)
                            etDocType.setText(user.tipo_documento ?: "")
                            etDocNum.setText(user.numero_documento ?: "")
                            etPhone.setText(user.telefono ?: "")
                            etAddress.setText(user.direccion ?: "")
                            etPassword.setText("") // No mostrar password por seguridad

                            val estado = if (user.activo) "Activo" else "Inactivo"
                            showStatus("✓ Usuario encontrado: ${user.nombre} (${estado})", false)
                        }
                    } else {
                        when (response.code()) {
                            401 -> showStatus("✗ Token inválido. Inicie sesión nuevamente", true)
                            404 -> showStatus("✗ No se encontró usuario con ID #$id", true)
                            else -> showStatus("✗ Error al buscar (${response.code()})", true)
                        }
                    }
                }
                override fun onFailure(call: Call<Usuario>, t: Throwable) {
                    showStatus("✗ Error de conexión: ${t.message}", true)
                }
            })
        }

        // ═══════════════════════════════════════════════
        // ACTUALIZAR USUARIO
        // ═══════════════════════════════════════════════
        btnEdit.setOnClickListener {
            val idStr = etId.text.toString().trim()
            if (idStr.isEmpty()) {
                showStatus("⚠ Primero busca un usuario por ID", true)
                return@setOnClickListener
            }

            val id = idStr.toIntOrNull()
            if (id == null || id <= 0) {
                showStatus("⚠ ID inválido", true)
                return@setOnClickListener
            }

            val nombre = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty()) {
                showStatus("⚠ Nombre y email son obligatorios", true)
                return@setOnClickListener
            }

            val password = etPassword.text.toString().trim().takeIf { it.isNotEmpty() }

            val req = UsuarioUpdateRequest(
                nombre = nombre,
                email = email,
                password = password,
                tipo_documento = etDocType.text.toString().trim().takeIf { it.isNotEmpty() },
                numero_documento = etDocNum.text.toString().trim().takeIf { it.isNotEmpty() },
                telefono = etPhone.text.toString().trim().takeIf { it.isNotEmpty() },
                direccion = etAddress.text.toString().trim().takeIf { it.isNotEmpty() }
            )

            showStatus("Actualizando usuario #$id...", false)

            ApiClient.instance.updateUsuario(id, req).enqueue(object : Callback<Usuario> {
                override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        showStatus("✓ Usuario #$id actualizado exitosamente", false)
                        Toast.makeText(this@PerfilPruebaActivity, "¡Actualizado correctamente!", Toast.LENGTH_SHORT).show()

                        // Refrescar campos con la respuesta del servidor
                        if (user != null) {
                            etName.setText(user.nombre)
                            etEmail.setText(user.email)
                            etDocType.setText(user.tipo_documento ?: "")
                            etDocNum.setText(user.numero_documento ?: "")
                            etPhone.setText(user.telefono ?: "")
                            etAddress.setText(user.direccion ?: "")
                            etPassword.setText("")
                        }
                    } else {
                        val errorMsg = when (response.code()) {
                            400 -> "Datos inválidos"
                            401 -> "Token inválido. Inicie sesión nuevamente"
                            404 -> "Usuario no encontrado"
                            409 -> "El correo ya está en uso"
                            else -> "Error del servidor (${response.code()})"
                        }
                        showStatus("✗ $errorMsg", true)
                    }
                }
                override fun onFailure(call: Call<Usuario>, t: Throwable) {
                    showStatus("✗ Error de conexión: ${t.message}", true)
                }
            })
        }

        // ═══════════════════════════════════════════════
        // ELIMINAR USUARIO
        // ═══════════════════════════════════════════════
        btnDelete.setOnClickListener {
            val idStr = etId.text.toString().trim()
            if (idStr.isEmpty()) {
                showStatus("⚠ Ingresa un ID para eliminar", true)
                return@setOnClickListener
            }

            val id = idStr.toIntOrNull()
            if (id == null || id <= 0) {
                showStatus("⚠ ID inválido", true)
                return@setOnClickListener
            }

            // Diálogo de confirmación
            AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de eliminar al usuario #$id?\n\nEsta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ ->
                    showStatus("Eliminando usuario #$id...", false)

                    ApiClient.instance.deleteUsuario(id).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                clearForm()
                                showStatus("✓ Usuario #$id eliminado correctamente", false)
                                Toast.makeText(this@PerfilPruebaActivity, "¡Eliminado correctamente!", Toast.LENGTH_SHORT).show()
                            } else {
                                val errorMsg = when (response.code()) {
                                    401 -> "Token inválido. Inicie sesión nuevamente"
                                    404 -> "Usuario no encontrado"
                                    else -> "Error al eliminar (${response.code()})"
                                }
                                showStatus("✗ $errorMsg", true)
                            }
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            showStatus("✗ Error de conexión: ${t.message}", true)
                        }
                    })
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // ═══════════════════════════════════════════════
        // LIMPIAR FORMULARIO
        // ═══════════════════════════════════════════════
        btnClear.setOnClickListener {
            clearForm()
            showStatus("Formulario limpiado", false)
        }

        // VOLVER
        btnBack.setOnClickListener { finish() }
    }

    /**
     * Muestra un mensaje de estado debajo de las acciones.
     */
    private fun showStatus(message: String, isError: Boolean) {
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = message
        tvStatus.setTextColor(
            if (isError) resources.getColor(R.color.error_text, theme)
            else resources.getColor(R.color.primary, theme)
        )
    }

    /**
     * Limpia todos los campos del formulario.
     */
    private fun clearForm() {
        etId.setText("")
        etName.setText("")
        etEmail.setText("")
        etPassword.setText("")
        etDocType.setText("")
        etDocNum.setText("")
        etPhone.setText("")
        etAddress.setText("")
    }
}
