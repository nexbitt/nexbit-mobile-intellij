package com.example.nexbitmobile.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Usuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClienteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clientes)

        val toolbar = findViewById<Toolbar>(R.id.toolbarClientes)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerViewClientes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ClienteAdapter(
            emptyList(),
            onEdit = { cliente -> Toast.makeText(this, "Editar: ${cliente.nombre}", Toast.LENGTH_SHORT).show() },
            onDelete = this::deleteCliente
        )
        recyclerView.adapter = adapter

        loadClientes()
    }

    private fun loadClientes() {
        ApiClient.instance.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful) {
                    val allUsers = response.body() ?: emptyList()
                    val clientes = allUsers.filter { it.rol_id == 2 || it.rol_nombre?.equals("Cliente", ignoreCase = true) == true }
                    adapter.updateData(clientes)
                } else {
                    Toast.makeText(this@ClientesActivity, "Error al cargar clientes", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                Log.e("ClientesActivity", "Error de conexión al cargar clientes", t)
                Toast.makeText(this@ClientesActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteCliente(cliente: Usuario) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Cliente")
            .setMessage("¿Deseas eliminar al cliente ${cliente.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                ApiClient.instance.deleteUsuario(cliente.id_usuario).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ClientesActivity, "Cliente eliminado", Toast.LENGTH_SHORT).show()
                            loadClientes()
                        } else {
                            Toast.makeText(this@ClientesActivity, "Error al eliminar cliente", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("ClientesActivity", "Error de conexión al eliminar cliente", t)
                        Toast.makeText(this@ClientesActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("No", null)
            .show()
    }
}
