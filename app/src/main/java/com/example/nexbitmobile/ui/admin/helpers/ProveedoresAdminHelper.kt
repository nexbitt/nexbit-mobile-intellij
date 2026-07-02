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
import com.example.nexbitmobile.ui.MainOrbixActivity
import com.example.nexbitmobile.ui.ProveedorAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProveedoresAdminHelper(private val activity: MainOrbixActivity) {

    fun showProveedores(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.recyclerView)
        val btnAdd = root.findViewById<View>(R.id.btnAddHeader)
        val tvEmpty = root.findViewById<TextView>(R.id.tvEmpty)

        root.findViewById<TextView>(R.id.tvSectionTitle).text = "Proveedores"

        rv.layoutManager = LinearLayoutManager(activity)
        lateinit var adapter: ProveedorAdapter
        val deleteHandler: (Proveedor) -> Unit = { p ->
            AlertDialog.Builder(activity).setTitle("Eliminar")
                .setMessage("¿Eliminar ${p.nombre}?").setPositiveButton("Sí") { _, _ ->
                    ApiClient.instance.deleteProveedor(p.id_proveedor!!).enqueue(object : Callback<Void> {
                        override fun onResponse(c: Call<Void>, res: Response<Void>) {
                            Toast.makeText(activity, "Eliminado", Toast.LENGTH_SHORT).show()
                            reloadProveedores(rv, tvEmpty, adapter)
                        }
                        override fun onFailure(c: Call<Void>, t: Throwable) {}
                    })
                }.setNegativeButton("No", null).show()
        }
        adapter = ProveedorAdapter(emptyList(),
            { p ->
                AlertDialog.Builder(activity).setTitle(p.nombre)
                    .setMessage("NIT: ${p.nit}\nTel: ${p.telefono}\nCorreo: ${p.correo}")
                    .setPositiveButton("OK", null).show()
            },
            deleteHandler)
        rv.adapter = adapter

        btnAdd.setOnClickListener {
            val view = LayoutInflater.from(activity).inflate(R.layout.dialog_proveedor, null)
            val etNombre = view.findViewById<EditText>(R.id.etNombre)
            val etNit = view.findViewById<EditText>(R.id.etNit)
            val etTelefono = view.findViewById<EditText>(R.id.etTelefono)
            val etCorreo = view.findViewById<EditText>(R.id.etCorreo)
            val etDireccion = view.findViewById<EditText>(R.id.etDireccion)

            etNit.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            etNit.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    val digits = s?.filter { it.isDigit() } ?: ""
                    if (digits != s.toString()) {
                        etNit.setText(digits)
                        etNit.setSelection(digits.length)
                    }
                }
            })

            AlertDialog.Builder(activity).setTitle("Nuevo Proveedor").setView(view)
                .setPositiveButton("Guardar") { _, _ ->
                    val n = etNombre.text.toString().trim()
                    if (n.isEmpty()) { Toast.makeText(activity, "Nombre obligatorio", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                    ApiClient.instance.createProveedor(Proveedor(
                        nit = etNit.text.toString().trim().ifEmpty { "N/A" },
                        nombre = n,
                        telefono = etTelefono.text.toString().trim().ifEmpty { "N/A" },
                        correo = etCorreo.text.toString().trim().ifEmpty { "N/A" },
                        direccion = etDireccion.text.toString().trim().ifEmpty { "N/A" }
                    )).enqueue(object : Callback<ProveedorResponse> {
                        override fun onResponse(c: Call<ProveedorResponse>, res: Response<ProveedorResponse>) {
                            if (res.isSuccessful) { Toast.makeText(activity, "Creado", Toast.LENGTH_SHORT).show(); reloadProveedores(rv, tvEmpty, adapter) }
                            else Toast.makeText(activity, "Error (${res.code()})", Toast.LENGTH_SHORT).show()
                        }
                        override fun onFailure(c: Call<ProveedorResponse>, t: Throwable) { Toast.makeText(activity, "Error conexión", Toast.LENGTH_SHORT).show() }
                    })
                }.setNegativeButton("Cancelar", null).show()
        }

        reloadProveedores(rv, tvEmpty, adapter)
    }

    private fun reloadProveedores(rv: RecyclerView, tvEmpty: TextView, adapter: ProveedorAdapter) {
        ApiClient.instance.getProveedores().enqueue(object : Callback<List<Proveedor>> {
            override fun onResponse(c: Call<List<Proveedor>>, res: Response<List<Proveedor>>) {
                if (res.isSuccessful) {
                    val list = res.body() ?: emptyList()
                    adapter.updateData(list)
                    rv.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
            override fun onFailure(c: Call<List<Proveedor>>, t: Throwable) {}
        })
    }
}
