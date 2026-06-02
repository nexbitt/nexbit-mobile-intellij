package com.example.nexbitmobile.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Usuario
import com.example.nexbitmobile.model.UsuarioCreateRequest
import com.example.nexbitmobile.model.UsuarioUpdateRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PruebasActivity : AppCompatActivity() {

    private lateinit var etSearchId: TextInputEditText
    private lateinit var btnSearch: MaterialButton
    private lateinit var cardUserDetail: MaterialCardView
    private lateinit var tvId: TextView
    private lateinit var tvNombre: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRol: TextView
    private lateinit var tvDocumento: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var tvDireccion: TextView
    private lateinit var tvEstado: TextView
    private lateinit var btnEditUser: MaterialButton
    private lateinit var btnDeleteUser: MaterialButton
    private lateinit var btnCreateUser: MaterialButton
    private lateinit var tvMessage: TextView

    private var lastFoundUser: Usuario? = null

    private val roles = arrayOf("Administrador (1)", "Cliente (2)", "Repartidor (4)")
    private val rolesIds = arrayOf(1, 2, 4)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pruebas)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        etSearchId = findViewById(R.id.etSearchId)
        btnSearch = findViewById(R.id.btnSearch)
        cardUserDetail = findViewById(R.id.cardUserDetail)
        tvId = findViewById(R.id.tvId)
        tvNombre = findViewById(R.id.tvNombre)
        tvEmail = findViewById(R.id.tvEmail)
        tvRol = findViewById(R.id.tvRol)
        tvDocumento = findViewById(R.id.tvDocumento)
        tvTelefono = findViewById(R.id.tvTelefono)
        tvDireccion = findViewById(R.id.tvDireccion)
        tvEstado = findViewById(R.id.tvEstado)
        btnEditUser = findViewById(R.id.btnEditUser)
        btnDeleteUser = findViewById(R.id.btnDeleteUser)
        btnCreateUser = findViewById(R.id.btnCreateUser)
        tvMessage = findViewById(R.id.tvMessage)

        btnSearch.setOnClickListener {
            val idText = etSearchId.text.toString().trim()
            if (idText.isEmpty()) {
                tvMessage.text = "Ingresa un ID de usuario"
                return@setOnClickListener
            }
            searchUserById(idText.toInt())
        }

        btnEditUser.setOnClickListener {
            lastFoundUser?.let { showEditDialog(it) }
        }

        btnDeleteUser.setOnClickListener {
            lastFoundUser?.let { showDeleteDialog(it) }
        }

        btnCreateUser.setOnClickListener { showCreateDialog() }
    }

    private fun searchUserById(id: Int) {
        tvMessage.text = ""
        ApiClient.instance.getUsuario(id).enqueue(object : Callback<Usuario> {
            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        lastFoundUser = user
                        showUserDetails(user)
                    } else {
                        tvMessage.text = "Usuario no encontrado"
                        cardUserDetail.visibility = View.GONE
                    }
                } else {
                    tvMessage.text = "Error: Usuario no encontrado (${response.code()})"
                    cardUserDetail.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                tvMessage.text = "Error de conexión: ${t.message}"
                cardUserDetail.visibility = View.GONE
            }
        })
    }

    private fun showUserDetails(user: Usuario) {
        tvId.text = user.id_usuario.toString()
        tvNombre.text = user.nombre
        tvEmail.text = user.email

        val rolLabel = when (user.rol_id) {
            1 -> "Administrador"
            2 -> "Cliente"
            4 -> "Repartidor"
            else -> "Desconocido ($user.rol_id)"
        }
        tvRol.text = rolLabel

        tvDocumento.text = buildString {
            if (!user.tipo_documento.isNullOrEmpty()) {
                append("${user.tipo_documento}: ")
            }
            append(user.numero_documento ?: "—")
        }
        tvTelefono.text = user.telefono ?: "—"
        tvDireccion.text = user.direccion ?: "—"

        if (user.activo) {
            tvEstado.text = "Activo"
            tvEstado.setTextColor(getColor(R.color.success))
        } else {
            tvEstado.text = "Inactivo"
            tvEstado.setTextColor(getColor(R.color.error_text))
        }

        cardUserDetail.visibility = View.VISIBLE
    }

    private fun showCreateDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_usuario, null)
        val etNombre = view.findViewById<TextInputEditText>(R.id.etNombre)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val spRol = view.findViewById<Spinner>(R.id.spRol)

        spRol.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        AlertDialog.Builder(this)
            .setTitle("Crear Usuario")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val request = UsuarioCreateRequest(
                    rol_id = rolesIds[spRol.selectedItemPosition],
                    nombre = etNombre.text.toString(),
                    email = etEmail.text.toString(),
                    password = etPassword.text.toString()
                )
                ApiClient.instance.createUsuario(request).enqueue(object : Callback<com.example.nexbitmobile.model.UsuarioCreateResponse> {
                    override fun onResponse(call: Call<com.example.nexbitmobile.model.UsuarioCreateResponse>, response: Response<com.example.nexbitmobile.model.UsuarioCreateResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@PruebasActivity, "Usuario creado exitosamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@PruebasActivity, "Error al crear usuario", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<com.example.nexbitmobile.model.UsuarioCreateResponse>, t: Throwable) {
                        Toast.makeText(this@PruebasActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditDialog(user: Usuario) {
        val view = layoutInflater.inflate(R.layout.dialog_usuario, null)
        val etNombre = view.findViewById<TextInputEditText>(R.id.etNombre)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val spRol = view.findViewById<Spinner>(R.id.spRol)

        spRol.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        etNombre.setText(user.nombre)
        etEmail.setText(user.email)
        etPassword.hint = "(Dejar vacío para no cambiar)"

        val rolIndex = rolesIds.indexOf(user.rol_id)
        if (rolIndex >= 0) spRol.setSelection(rolIndex)

        AlertDialog.Builder(this)
            .setTitle("Editar Usuario #${user.id_usuario}")
            .setView(view)
            .setPositiveButton("Actualizar") { _, _ ->
                val password = etPassword.text.toString()
                val request = UsuarioUpdateRequest(
                    rol_id = rolesIds[spRol.selectedItemPosition],
                    nombre = etNombre.text.toString(),
                    email = etEmail.text.toString(),
                    password = if (password.isNotEmpty()) password else null
                )
                ApiClient.instance.updateUsuario(user.id_usuario, request).enqueue(object : Callback<Usuario> {
                    override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@PruebasActivity, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                            searchUserById(user.id_usuario)
                        } else {
                            Toast.makeText(this@PruebasActivity, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Usuario>, t: Throwable) {
                        Toast.makeText(this@PruebasActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteDialog(user: Usuario) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de eliminar a ${user.nombre} (ID: ${user.id_usuario})?")
            .setPositiveButton("Eliminar") { _, _ ->
                ApiClient.instance.deleteUsuario(user.id_usuario).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@PruebasActivity, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                            cardUserDetail.visibility = View.GONE
                            lastFoundUser = null
                            etSearchId.text?.clear()
                        } else {
                            Toast.makeText(this@PruebasActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@PruebasActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
