package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.model.Usuario

class ClienteAdapter(
    private var clientes: List<Usuario>,
    private val onDelete: (Usuario) -> Unit
) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    class ClienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvClienteId: TextView = view.findViewById(R.id.tvClienteId)
        val tvClienteNombre: TextView = view.findViewById(R.id.tvClienteNombre)
        val tvClienteDocumento: TextView = view.findViewById(R.id.tvClienteDocumento)
        val tvClienteEmail: TextView = view.findViewById(R.id.tvClienteEmail)
        val tvClienteTelefono: TextView = view.findViewById(R.id.tvClienteTelefono)
        val tvClienteDireccion: TextView = view.findViewById(R.id.tvClienteDireccion)
        val btnDeleteCliente: ImageButton = view.findViewById(R.id.btnDeleteCliente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]
        holder.tvClienteId.text = "#${cliente.id_usuario}"
        holder.tvClienteNombre.text = cliente.nombre
        
        val docType = cliente.tipo_documento ?: ""
        val docNum = cliente.numero_documento ?: ""
        holder.tvClienteDocumento.text = if (docType.isNotEmpty() || docNum.isNotEmpty()) {
            "Documento: $docType $docNum".trim()
        } else {
            "Documento: N/A"
        }
        
        holder.tvClienteEmail.text = cliente.email
        holder.tvClienteTelefono.text = "Teléfono: ${cliente.telefono ?: "N/A"}"
        holder.tvClienteDireccion.text = "Dirección: ${cliente.direccion ?: "N/A"}"

        holder.btnDeleteCliente.setOnClickListener { onDelete(cliente) }
    }

    override fun getItemCount() = clientes.size

    fun updateData(newClientes: List<Usuario>) {
        this.clientes = newClientes
        notifyDataSetChanged()
    }
}
