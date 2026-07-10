package com.example.nexbitmobile.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Usuario
import com.example.nexbitmobile.model.UsuarioCreateRequest
import com.example.nexbitmobile.model.UsuarioCreateResponse
import com.example.nexbitmobile.model.UsuarioUpdateRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsuariosAdminActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UsuarioAdapter
    private lateinit var fabAdd: FloatingActionButton

    // Roles alineados con la base de datos: 1 = Admin, 2 = Cliente, 4 = Repartidor
    private val roles = arrayOf("Administrador (1)", "Cliente (2)", "Repartidor (4)")
    private val rolesIds = arrayOf(1, 2, 4)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuarios_admin)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UsuarioAdapter(emptyList(), this::showEditDialog, this::deleteUsuario)
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener { showCreateDialog() }

        loadUsuarios()
    }

    private fun loadUsuarios() {
        ApiClient.instance.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful) {
                    adapter.updateData(response.body() ?: emptyList())
                } else {
                    Toast.makeText(this@UsuariosAdminActivity, "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                Toast.makeText(this@UsuariosAdminActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCreateDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_usuario, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val spRol = view.findViewById<Spinner>(R.id.spRol)

        spRol.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        AlertDialog.Builder(this)
            .setTitle("Nuevo Usuario")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val rolId = rolesIds[spRol.selectedItemPosition]
                val request = UsuarioCreateRequest(
                    rol_id = rolId,
                    nombre = etNombre.text.toString(),
                    email = etEmail.text.toString(),
                    password = etPassword.text.toString()
                )
                
                ApiClient.instance.createUsuario(request).enqueue(object : Callback<UsuarioCreateResponse> {
                    override fun onResponse(call: Call<UsuarioCreateResponse>, response: Response<UsuarioCreateResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@UsuariosAdminActivity, "Usuario creado", Toast.LENGTH_SHORT).show()
                            loadUsuarios()
                        } else {
                            Toast.makeText(this@UsuariosAdminActivity, "Error al crear", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<UsuarioCreateResponse>, t: Throwable) {}
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditDialog(usuario: Usuario) {
        val view = layoutInflater.inflate(R.layout.dialog_usuario, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val spRol = view.findViewById<Spinner>(R.id.spRol)

        spRol.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        
        etNombre.setText(usuario.nombre)
        etEmail.setText(usuario.email)
        etPassword.hint = "(Dejar vacío para no cambiar)"
        
        val rolIndex = rolesIds.indexOf(usuario.rol_id)
        if (rolIndex >= 0) spRol.setSelection(rolIndex)

        AlertDialog.Builder(this)
            .setTitle("Editar Usuario")
            .setView(view)
            .setPositiveButton("Actualizar") { _, _ ->
                val rolId = rolesIds[spRol.selectedItemPosition]
                val password = etPassword.text.toString()
                val request = UsuarioUpdateRequest(
                    rol_id = rolId,
                    nombre = etNombre.text.toString(),
                    email = etEmail.text.toString(),
                    password = if (password.isNotEmpty()) password else null
                )
                ApiClient.instance.updateUsuarioByAdmin(usuario.id_usuario, request).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@UsuariosAdminActivity, "Usuario actualizado", Toast.LENGTH_SHORT).show()
                            loadUsuarios()
                        } else {
                            try {
                                val errorBody = response.errorBody()?.string()
                                Toast.makeText(this@UsuariosAdminActivity, errorBody ?: "Error (${response.code()})", Toast.LENGTH_SHORT).show()
                            } catch (_: Exception) {
                                Toast.makeText(this@UsuariosAdminActivity, "Error (${response.code()})", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@UsuariosAdminActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUsuario(usuario: Usuario) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Deseas eliminar al usuario ${usuario.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                ApiClient.instance.deleteUsuario(usuario.id_usuario).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@UsuariosAdminActivity, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                            loadUsuarios()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
            }
            .setNegativeButton("No", null)
            .show()
    }
}
