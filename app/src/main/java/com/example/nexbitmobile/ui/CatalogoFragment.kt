package com.example.nexbitmobile.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.api.SocketManager
import com.example.nexbitmobile.model.*
import com.example.nexbitmobile.util.SecurePrefs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CatalogoFragment : Fragment() {

    private val viewModel: CatalogoViewModel by viewModels()

    private lateinit var rvProductos: RecyclerView
    private lateinit var adapter: ProductoAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvCartBadge: TextView
    private lateinit var etSearch: EditText
    private lateinit var ivProfileAvatar: ImageView
    private lateinit var btnEntrar: Button
    private var isLoggedIn = false
    private var isAdmin = false
    private var allProductos = listOf<Producto>()
    private var filteredProductos = listOf<Producto>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_catalogo, container, false)

        rvProductos = root.findViewById(R.id.rvProductos)
        progressBar = root.findViewById(R.id.progressBar)
        tvEmpty = root.findViewById(R.id.tvEmpty)
        tvCartBadge = root.findViewById(R.id.tvCartBadge)
        etSearch = root.findViewById(R.id.etSearch)
        ivProfileAvatar = root.findViewById(R.id.ivProfileAvatar)
        btnEntrar = root.findViewById(R.id.btnEntrar)

        rvProductos.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = ProductoAdapter(mutableListOf(), { }, { producto ->
            val intent = android.content.Intent(requireContext(), ProductDetailActivity::class.java)
            intent.putExtra("id_producto", producto.id_producto)
            requireContext().startActivity(intent)
        })
        rvProductos.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProductos(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        root.findViewById<FrameLayout>(R.id.btnCartContainer).setOnClickListener {
            if (isLoggedIn) {
                findNavController().navigate(R.id.carritoFragment)
            } else {
                startActivity(android.content.Intent(requireContext(), LoginActivity::class.java))
            }
        }

        checkAuthState()
        loadProductos()

        return root
    }

    override fun onResume() {
        super.onResume()
        checkAuthState()
    }

    private fun checkAuthState() {
        val context = requireContext()
        val token = SecurePrefs.getToken(context)
        isLoggedIn = !token.isNullOrEmpty()
        val rolNombre = context.getSharedPreferences("app", android.content.Context.MODE_PRIVATE)
            .getString("userRole", "") ?: ""
        isAdmin = rolNombre == "Administrador"

        if (isLoggedIn) {
            btnEntrar.visibility = View.GONE
            ivProfileAvatar.visibility = View.VISIBLE
            val avatarUrl = context.getSharedPreferences("app", android.content.Context.MODE_PRIVATE)
                .getString("userAvatar", "") ?: ""
            if (avatarUrl.isNotEmpty()) {
                Glide.with(this).load(avatarUrl).circleCrop().into(ivProfileAvatar)
            }
            ivProfileAvatar.setOnClickListener {
                if (isAdmin) {
                    startActivity(android.content.Intent(context, MainOrbixActivity::class.java))
                } else {
                    startActivity(android.content.Intent(context, PerfilActivity::class.java))
                }
            }
        } else {
            btnEntrar.visibility = View.VISIBLE
            ivProfileAvatar.visibility = View.GONE
            btnEntrar.setOnClickListener {
                startActivity(android.content.Intent(context, LoginActivity::class.java))
            }
        }
    }

    private fun loadProductos() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.productos.collect { productos ->
                allProductos = productos
                filteredProductos = productos
                adapter.updateList(productos.toMutableList())
                progressBar.visibility = View.GONE
                tvEmpty.visibility = if (productos.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        viewModel.loadProductos()
    }

    private fun filterProductos(query: String) {
        filteredProductos = if (query.isEmpty()) allProductos
        else allProductos.filter {
            it.nombre.lowercase().contains(query.lowercase()) ||
            (it.categoria_nombre?.lowercase()?.contains(query.lowercase()) ?: false)
        }
        adapter.updateList(filteredProductos.toMutableList())
        tvEmpty.visibility = if (filteredProductos.isEmpty()) View.VISIBLE else View.GONE
    }

}
