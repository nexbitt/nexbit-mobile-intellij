package com.example.nexbitmobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.nexbitmobile.ui.LoginActivity
import com.example.nexbitmobile.ui.PerfilActivity
import com.example.nexbitmobile.ui.PerfilPruebaActivity
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val btnOpenMenu = findViewById<Button>(R.id.btnOpenMenu)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        btnOpenMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_inicio -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                }
                R.id.nav_perfil_prueba -> {
                    startActivity(Intent(this, PerfilPruebaActivity::class.java))
                }
                R.id.nav_logout -> {
                    // Cerrar sesión
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            true
        }
    }
}
