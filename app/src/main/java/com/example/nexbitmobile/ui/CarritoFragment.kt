package com.example.nexbitmobile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.*
import com.example.nexbitmobile.util.SecurePrefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class CarritoFragment : Fragment() {

    private lateinit var rvCarrito: RecyclerView
    private lateinit var adapter: CarritoAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var llEmpty: LinearLayout
    private lateinit var llSummary: LinearLayout
    private lateinit var tvItemCount: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnGoToCatalog: Button
    private lateinit var btnCheckout: Button

    private var userId = 0
    private var token = ""
    private var cartItems = listOf<CarritoItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_carrito, container, false)

        rvCarrito = root.findViewById(R.id.rvCarrito)
        progressBar = root.findViewById(R.id.progressBar)
        llEmpty = root.findViewById(R.id.llEmpty)
        llSummary = root.findViewById(R.id.llSummary)
        tvItemCount = root.findViewById(R.id.tvItemCount)
        tvTotal = root.findViewById(R.id.tvTotal)
        btnGoToCatalog = root.findViewById(R.id.btnGoToCatalog)
        btnCheckout = root.findViewById(R.id.btnCheckout)

        val prefs = requireContext().getSharedPreferences("app", android.content.Context.MODE_PRIVATE)
        userId = prefs.getInt("userId", 0)
        token = SecurePrefs.getToken(requireContext()) ?: ""

        rvCarrito.layoutManager = LinearLayoutManager(requireContext())
        adapter = CarritoAdapter(mutableListOf(), { _, _ -> }, { removeItem(it) })
        rvCarrito.adapter = adapter

        btnGoToCatalog.setOnClickListener {
            findNavController().navigate(R.id.catalogoFragment)
        }

        btnCheckout.setOnClickListener {
            if (userId == 0 || token.isEmpty()) {
                Toast.makeText(requireContext(), "Inicia sesión para continuar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            realizarPedido()
        }

        loadCart()

        return root
    }

    private fun loadCart() {
        if (userId == 0 || token.isEmpty()) {
            showEmpty()
            return
        }
        progressBar.visibility = View.VISIBLE
        llEmpty.visibility = View.GONE
        ApiClient.instance.getCarrito(userId).enqueue(object : Callback<List<CarritoItem>> {
            override fun onResponse(call: Call<List<CarritoItem>>, response: Response<List<CarritoItem>>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    cartItems = items
                    if (items.isEmpty()) {
                        showEmpty()
                    } else {
                        showItems(items)
                    }
                } else {
                    showEmpty()
                }
            }
            override fun onFailure(call: Call<List<CarritoItem>>, t: Throwable) {
                progressBar.visibility = View.GONE
                showEmpty()
            }
        })
    }

    private fun showEmpty() {
        llEmpty.visibility = View.VISIBLE
        rvCarrito.visibility = View.GONE
        llSummary.visibility = View.GONE
    }

    private fun showItems(items: List<CarritoItem>) {
        llEmpty.visibility = View.GONE
        rvCarrito.visibility = View.VISIBLE
        llSummary.visibility = View.VISIBLE
        adapter = CarritoAdapter(items.toMutableList(), { _, _ -> }, { removeItem(it) })
        rvCarrito.adapter = adapter

        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        tvItemCount.text = "${items.size} items"
        val total = items.sumOf { it.precio * it.cantidad }
        tvTotal.text = formatter.format(total)
    }

    private fun removeItem(item: CarritoItem) {
        val newList = cartItems.toMutableList()
        newList.remove(item)
        if (newList.isEmpty()) showEmpty()
        else showItems(newList)
    }

    private fun realizarPedido() {
        Toast.makeText(requireContext(), "Procesando pedido...", Toast.LENGTH_SHORT).show()
    }
}
