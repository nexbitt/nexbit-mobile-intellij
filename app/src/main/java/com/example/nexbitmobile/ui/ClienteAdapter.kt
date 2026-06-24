package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Usuario

class ClienteAdapter(
    private var clientes: List<Usuario>,
    private val onEdit: (Usuario) -> Unit,
    private val onDelete: (Usuario) -> Unit
) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    class ClienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClienteNombre: TextView = view.findViewById(R.id.tvClienteNombre)
        val tvClienteEmail: TextView = view.findViewById(R.id.tvClienteEmail)
        val btnEditCliente: View = view.findViewById(R.id.btnEditCliente)
        val btnDeleteCliente: View = view.findViewById(R.id.btnDeleteCliente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]
        holder.tvClienteNombre.text = cliente.nombre
        holder.tvClienteEmail.text = cliente.email
        holder.btnEditCliente.setOnClickListener { onEdit(cliente) }
        holder.btnDeleteCliente.setOnClickListener { onDelete(cliente) }
    }

    override fun getItemCount() = clientes.size

    fun updateData(newClientes: List<Usuario>) {
        this.clientes = newClientes
        notifyDataSetChanged()
    }
}
