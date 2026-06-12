package com.example.nexbitmobile.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Proveedor
import com.example.nexbitmobile.model.ProveedorResponse
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProveedorActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProveedorAdapter
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proveedor)

        val toolbar = findViewById<Toolbar>(R.id.toolbarProveedores)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerViewProveedores)
        fabAdd = findViewById(R.id.fabAddProveedor)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProveedorAdapter(emptyList(), this::showEditDialog, this::deleteProveedor)
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener { showCreateDialog() }

        loadProveedores()
    }

    private fun loadProveedores() {
        ApiClient.instance.getProveedores().enqueue(object : Callback<List<Proveedor>> {
            override fun onResponse(call: Call<List<Proveedor>>, response: Response<List<Proveedor>>) {
                if (response.isSuccessful) {
                    adapter.updateData(response.body() ?: emptyList())
                } else {
                    Toast.makeText(this@ProveedorActivity, "Error al cargar proveedores", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Proveedor>>, t: Throwable) {
                Log.e("ProveedorActivity", "Error de conexión al cargar proveedores", t)
                Toast.makeText(this@ProveedorActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCreateDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_proveedor, null)
        val etNit = view.findViewById<EditText>(R.id.etNit)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etCorreo = view.findViewById<EditText>(R.id.etCorreo)
        val etTelefono = view.findViewById<EditText>(R.id.etTelefono)
        val etDireccion = view.findViewById<EditText>(R.id.etDireccion)

        AlertDialog.Builder(this)
            .setTitle("Nuevo Proveedor")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val nit = etNit.text.toString().trim()
                val nombre = etNombre.text.toString().trim()
                val correo = etCorreo.text.toString().trim()
                val telefono = etTelefono.text.toString().trim()
                val direccion = etDireccion.text.toString().trim()

                if (nit.isEmpty() || nombre.isEmpty()) {
                    Toast.makeText(this, "El NIT y Nombre son obligatorios", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val proveedor = Proveedor(
                    nit = nit,
                    nombre = nombre,
                    correo = correo,
                    telefono = telefono,
                    direccion = direccion
                )

                ApiClient.instance.createProveedor(proveedor).enqueue(object : Callback<ProveedorResponse> {
                    override fun onResponse(call: Call<ProveedorResponse>, response: Response<ProveedorResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ProveedorActivity, "Proveedor creado exitosamente", Toast.LENGTH_SHORT).show()
                            loadProveedores()
                        } else {
                            Toast.makeText(this@ProveedorActivity, "Error al crear proveedor", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ProveedorResponse>, t: Throwable) {
                        Log.e("ProveedorActivity", "Error de conexión al crear proveedor", t)
                        Toast.makeText(this@ProveedorActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditDialog(proveedor: Proveedor) {
        val view = layoutInflater.inflate(R.layout.dialog_proveedor, null)
        val etNit = view.findViewById<EditText>(R.id.etNit)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etCorreo = view.findViewById<EditText>(R.id.etCorreo)
        val etTelefono = view.findViewById<EditText>(R.id.etTelefono)
        val etDireccion = view.findViewById<EditText>(R.id.etDireccion)

        etNit.setText(proveedor.nit)
        etNombre.setText(proveedor.nombre)
        etCorreo.setText(proveedor.correo)
        etTelefono.setText(proveedor.telefono)
        etDireccion.setText(proveedor.direccion)

        AlertDialog.Builder(this)
            .setTitle("Editar Proveedor")
            .setView(view)
            .setPositiveButton("Actualizar") { _, _ ->
                val nit = etNit.text.toString().trim()
                val nombre = etNombre.text.toString().trim()
                val correo = etCorreo.text.toString().trim()
                val telefono = etTelefono.text.toString().trim()
                val direccion = etDireccion.text.toString().trim()

                if (nit.isEmpty() || nombre.isEmpty()) {
                    Toast.makeText(this, "El NIT y Nombre son obligatorios", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updatedProveedor = Proveedor(
                    id_proveedor = proveedor.id_proveedor,
                    nit = nit,
                    nombre = nombre,
                    correo = correo,
                    telefono = telefono,
                    direccion = direccion
                )

                val id = proveedor.id_proveedor ?: return@setPositiveButton

                ApiClient.instance.updateProveedor(id, updatedProveedor).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ProveedorActivity, "Proveedor actualizado exitosamente", Toast.LENGTH_SHORT).show()
                            loadProveedores()
                        } else {
                            Toast.makeText(this@ProveedorActivity, "Error al actualizar proveedor", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("ProveedorActivity", "Error de conexión al actualizar proveedor", t)
                        Toast.makeText(this@ProveedorActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteProveedor(proveedor: Proveedor) {
        val id = proveedor.id_proveedor ?: return
        val view = layoutInflater.inflate(R.layout.dialog_delete_confirm, null)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = "¿Deseas eliminar al proveedor ${proveedor.nombre}?"

        AlertDialog.Builder(this)
            .setView(view)
            .show()
            .apply {
                view.findViewById<View>(R.id.btnCancel).setOnClickListener { dismiss() }
                view.findViewById<View>(R.id.btnConfirm).setOnClickListener {
                    ApiClient.instance.deleteProveedor(id).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@ProveedorActivity, "Proveedor eliminado exitosamente", Toast.LENGTH_SHORT).show()
                                loadProveedores()
                            } else {
                                Toast.makeText(this@ProveedorActivity, "Error al eliminar proveedor", Toast.LENGTH_SHORT).show()
                            }
                            dismiss()
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Log.e("ProveedorActivity", "Error de conexión al eliminar proveedor", t)
                            Toast.makeText(this@ProveedorActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                    })
                }
            }
    }
}
