package com.example.nexbitmobile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.nexbitmobile.R

class AdminDashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_admin_dashboard, container, false)

        val prefs = requireContext().getSharedPreferences("app", android.content.Context.MODE_PRIVATE)
        val userName = prefs.getString("userName", "Usuario") ?: "Usuario"
        val rolNombre = prefs.getString("userRole", "") ?: ""

        root.findViewById<TextView>(R.id.tvWelcome).text = "¡Bienvenido, $userName!"
        root.findViewById<TextView>(R.id.tvRoleLabel).text =
            if (rolNombre == "Administrador") "Panel de Administración" else "Tienda Nexbit"

        return root
    }
}
