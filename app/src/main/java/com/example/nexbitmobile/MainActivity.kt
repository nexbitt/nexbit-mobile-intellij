package com.example.nexbitmobile

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.nexbitmobile.ui.CarritoActivity
import com.example.nexbitmobile.ui.CatalogoActivity
import com.example.nexbitmobile.ui.LoginActivity
import com.example.nexbitmobile.ui.PerfilActivity
import com.example.nexbitmobile.ui.PerfilPruebaActivity
import com.example.nexbitmobile.ui.ProveedorActivity
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbarMain)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_inicio -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_catalogo -> {
                    startActivity(Intent(this, CatalogoActivity::class.java))
                }
                R.id.nav_carrito -> {
                    startActivity(Intent(this, CarritoActivity::class.java))
                }
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                }
                R.id.nav_perfil_prueba -> {
                    startActivity(Intent(this, PerfilPruebaActivity::class.java))
                }
                R.id.nav_proveedor -> {
                    startActivity(Intent(this, ProveedorActivity::class.java))
                }
                R.id.nav_logout -> {
                    // Clear session and go to login
                    val prefs = getSharedPreferences("app", MODE_PRIVATE)
                    prefs.edit().clear().apply()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            true
        }
    }
}
