package com.example.nexbitmobile.ui.admin.helpers

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.*
import com.example.nexbitmobile.ui.CategoriaAdapter
import com.example.nexbitmobile.ui.MainOrbixActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoriasAdminHelper(private val activity: MainOrbixActivity) {

    fun showCategorias(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.recyclerView)
        val btnAdd = root.findViewById<View>(R.id.btnAddHeader)
        val tvEmpty = root.findViewById<TextView>(R.id.tvEmpty)

        root.findViewById<TextView>(R.id.tvSectionTitle).text = "Categorías"

        rv.layoutManager = LinearLayoutManager(activity)
        val adapter = CategoriaAdapter(emptyList(), { c -> editCategoria(c, rv, tvEmpty) }, { c -> deleteCategoria(c, rv, tvEmpty) })
        rv.adapter = adapter

        btnAdd.setOnClickListener { createCategoria(rv, tvEmpty, adapter) }

        ApiClient.instance.getCategorias().enqueue(object : Callback<List<Categoria>> {
            override fun onResponse(c: Call<List<Categoria>>, res: Response<List<Categoria>>) {
                if (res.isSuccessful) {
                    val list = res.body() ?: emptyList()
                    adapter.updateData(list)
                    rv.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            override fun onFailure(c: Call<List<Categoria>>, t: Throwable) {}
        })
    }

    private fun createCategoria(rv: RecyclerView, tvEmpty: TextView, adapter: CategoriaAdapter) {
        showCategoriaDialog(null, "Nueva Categoría", "Guardar") { nombre, desc ->
            ApiClient.instance.createCategoria(CategoriaRequest(nombre, desc)).enqueue(object : Callback<CategoriaCreateResponse> {
                override fun onResponse(c: Call<CategoriaCreateResponse>, res: Response<CategoriaCreateResponse>) {
                    if (res.isSuccessful) {
                        Toast.makeText(activity, "Categoría creada", Toast.LENGTH_SHORT).show()
                        reloadCategorias(rv, tvEmpty, adapter)
                    } else Toast.makeText(activity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(c: Call<CategoriaCreateResponse>, t: Throwable) {
                    Toast.makeText(activity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun editCategoria(cat: Categoria, rv: RecyclerView, tvEmpty: TextView) {
        showCategoriaDialog(cat, "Editar Categoría", "Actualizar") { nombre, desc ->
            ApiClient.instance.updateCategoria(cat.id_categoria, CategoriaRequest(nombre, desc)).enqueue(object : Callback<Void> {
                override fun onResponse(c: Call<Void>, res: Response<Void>) {
                    if (res.isSuccessful) {
                        Toast.makeText(activity, "Categoría actualizada", Toast.LENGTH_SHORT).show()
                        val adapter = rv.adapter as CategoriaAdapter
                        reloadCategorias(rv, tvEmpty, adapter)
                    } else Toast.makeText(activity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(c: Call<Void>, t: Throwable) {
                    Toast.makeText(activity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun showCategoriaDialog(existing: Categoria?, title: String, btnText: String, onSave: (String, String?) -> Unit) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_categoria, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etDesc = view.findViewById<EditText>(R.id.etDescripcion)

        if (existing != null) {
            etNombre.setText(existing.nombre)
            etDesc.setText(existing.descripcion ?: "")
        }

        AlertDialog.Builder(activity)
            .setTitle(title).setView(view)
            .setPositiveButton(btnText) { _, _ ->
                val nombre = etNombre.text.toString().trim()
                if (nombre.isEmpty()) { Toast.makeText(activity, "Nombre obligatorio", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                onSave(nombre, etDesc.text.toString().trim().ifEmpty { null })
            }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun deleteCategoria(cat: Categoria, rv: RecyclerView, tvEmpty: TextView) {
        AlertDialog.Builder(activity)
            .setTitle("Eliminar").setMessage("¿Eliminar ${cat.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                ApiClient.instance.deleteCategoria(cat.id_categoria).enqueue(object : Callback<Void> {
                    override fun onResponse(c: Call<Void>, res: Response<Void>) {
                        Toast.makeText(activity, "Eliminado", Toast.LENGTH_SHORT).show()
                        val adapter = rv.adapter as CategoriaAdapter
                        reloadCategorias(rv, tvEmpty, adapter)
                    }
                    override fun onFailure(c: Call<Void>, t: Throwable) {
                        Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("No", null).show()
    }

    private fun reloadCategorias(rv: RecyclerView, tvEmpty: TextView, adapter: CategoriaAdapter) {
        ApiClient.instance.getCategorias().enqueue(object : Callback<List<Categoria>> {
            override fun onResponse(c: Call<List<Categoria>>, res: Response<List<Categoria>>) {
                if (res.isSuccessful) {
                    val list = res.body() ?: emptyList()
                    adapter.updateData(list)
                    rv.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            override fun onFailure(c: Call<List<Categoria>>, t: Throwable) {}
        })
    }
}
