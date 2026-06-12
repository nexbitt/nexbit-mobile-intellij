package com.example.nexbitmobile.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.nexbitmobile.model.Categoria
import com.example.nexbitmobile.model.CategoriaCreateResponse
import com.example.nexbitmobile.model.CategoriaRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoriasAdminActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoriaAdapter
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var etSearch: EditText
    private lateinit var tvEmpty: TextView

    private var allCategorias = listOf<Categoria>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias_admin)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
        etSearch = findViewById(R.id.etSearch)
        tvEmpty = findViewById(R.id.tvEmpty)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CategoriaAdapter(emptyList(), this::showEditDialog, this::deleteCategoria)
        recyclerView.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterCategorias()
            }
        })

        fabAdd.setOnClickListener { showCreateDialog() }

        loadCategorias()
    }

    private fun filterCategorias() {
        val query = etSearch.text.toString().trim().lowercase()
        val filtered = if (query.isEmpty()) {
            allCategorias
        } else {
            allCategorias.filter { c ->
                (c.nombre.lowercase().contains(query)) ||
                (c.descripcion?.lowercase()?.contains(query) == true)
            }
        }
        adapter.updateData(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun loadCategorias() {
        ApiClient.instance.getCategorias().enqueue(object : Callback<List<Categoria>> {
            override fun onResponse(call: Call<List<Categoria>>, response: Response<List<Categoria>>) {
                if (response.isSuccessful) {
                    allCategorias = response.body() ?: emptyList()
                    filterCategorias()
                } else {
                    Toast.makeText(this@CategoriasAdminActivity, "Error al cargar categorías", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Categoria>>, t: Throwable) {
                Toast.makeText(this@CategoriasAdminActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showCreateDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_categoria, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Nueva Categoría")
            .setView(view)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            if (nombre.isEmpty()) {
                etNombre.error = "El nombre es obligatorio"
                return@setOnClickListener
            }
            val request = CategoriaRequest(nombre, etDescripcion.text.toString().trim().ifEmpty { null })
            ApiClient.instance.createCategoria(request).enqueue(object : Callback<CategoriaCreateResponse> {
                override fun onResponse(call: Call<CategoriaCreateResponse>, response: Response<CategoriaCreateResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CategoriasAdminActivity, "Categoría creada", Toast.LENGTH_SHORT).show()
                        loadCategorias()
                        dialog.dismiss()
                    } else {
                        val errorMsg = when (response.code()) {
                            400 -> "La categoría ya existe o datos inválidos"
                            401 -> "No autorizado"
                            else -> "Error al crear categoría (${response.code()})"
                        }
                        Toast.makeText(this@CategoriasAdminActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<CategoriaCreateResponse>, t: Throwable) {
                    Toast.makeText(this@CategoriasAdminActivity, "Error de conexión al crear categoría", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun showEditDialog(categoria: Categoria) {
        val view = layoutInflater.inflate(R.layout.dialog_categoria, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etDescripcion = view.findViewById<EditText>(R.id.etDescripcion)

        etNombre.setText(categoria.nombre)
        etDescripcion.setText(categoria.descripcion)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar Categoría")
            .setView(view)
            .setPositiveButton("Actualizar", null)
            .setNegativeButton("Cancelar", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            if (nombre.isEmpty()) {
                etNombre.error = "El nombre es obligatorio"
                return@setOnClickListener
            }
            val request = CategoriaRequest(nombre, etDescripcion.text.toString().trim().ifEmpty { null })
            ApiClient.instance.updateCategoria(categoria.id_categoria, request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CategoriasAdminActivity, "Categoría actualizada", Toast.LENGTH_SHORT).show()
                        loadCategorias()
                        dialog.dismiss()
                    } else {
                        val errorMsg = when (response.code()) {
                            400 -> "Datos inválidos"
                            404 -> "Categoría no encontrada"
                            else -> "Error al actualizar (${response.code()})"
                        }
                        Toast.makeText(this@CategoriasAdminActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@CategoriasAdminActivity, "Error de conexión al actualizar", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun deleteCategoria(categoria: Categoria) {
        val view = layoutInflater.inflate(R.layout.dialog_delete_confirm, null)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = "¿Deseas eliminar la categoría ${categoria.nombre}?"

        AlertDialog.Builder(this)
            .setView(view)
            .show()
            .apply {
                view.findViewById<View>(R.id.btnCancel).setOnClickListener { dismiss() }
                view.findViewById<View>(R.id.btnConfirm).setOnClickListener {
                    ApiClient.instance.deleteCategoria(categoria.id_categoria).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@CategoriasAdminActivity, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                                loadCategorias()
                            } else {
                                Log.e("CategoriasAdmin", "Delete error: ${response.code()}")
                            }
                            dismiss()
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Log.e("CategoriasAdmin", "Delete category failed", t)
                            Toast.makeText(this@CategoriasAdminActivity, "Error al eliminar categoría", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                    })
                }
            }
    }
}
