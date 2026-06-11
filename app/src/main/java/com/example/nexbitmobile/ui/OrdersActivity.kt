package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Pedido
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrdersActivity : AppCompatActivity() {

    private lateinit var rvOrders: RecyclerView
    private lateinit var adapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_orders)

        rvOrders = findViewById(R.id.rvOrders)
        rvOrders.layoutManager = LinearLayoutManager(this)

        adapter = OrderAdapter(emptyList()) { pedido ->
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("pedido_id", pedido.id_pedido)
            startActivity(intent)
        }
        rvOrders.adapter = adapter

        loadOrders()
    }

    private fun loadOrders() {
        ApiClient.instance.getPedidos().enqueue(object : Callback<List<Pedido>> {
            override fun onResponse(call: Call<List<Pedido>>, response: Response<List<Pedido>>) {
                if (response.isSuccessful) {
                    adapter.updateList(response.body() ?: emptyList())
                }
            }
            override fun onFailure(call: Call<List<Pedido>>, t: Throwable) {}
        })
    }
}
