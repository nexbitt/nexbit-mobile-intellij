package com.example.nexbitmobile
import com.example.nexbitmobile.ui.EntregasActivity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.nexbitmobile.ui.LoginActivity
import com.example.nexbitmobile.ui.PerfilActivity
import com.example.nexbitmobile.ui.ProductosAdminActivity
import com.example.nexbitmobile.ui.CategoriasAdminActivity
import com.example.nexbitmobile.ui.UsuariosAdminActivity
import com.example.nexbitmobile.ui.PedidosAdminActivity
import com.example.nexbitmobile.ui.ProveedorActivity
import com.example.nexbitmobile.ui.ClientesActivity
import com.example.nexbitmobile.ui.CatalogoActivity
import com.example.nexbitmobile.ui.CarritoActivity
import com.example.nexbitmobile.ui.MisPedidosActivity
import com.example.nexbitmobile.ui.PruebasActivity
import com.example.nexbitmobile.ui.RolesAdminActivity
import com.example.nexbitmobile.ui.RepartidoresAdminActivity
import com.example.nexbitmobile.ui.AyudaActivity
import com.example.nexbitmobile.ui.ContactoActivity
import com.example.nexbitmobile.ui.PerfilRepartidorActivity
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
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_more)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val rolId = prefs.getInt("rolId", ROL_CLIENTE)
        val userName = prefs.getString("userName", "Usuario") ?: "Usuario"
        val isAdmin = rolId == ROL_ADMIN
        val isCliente = rolId == ROL_CLIENTE
        val isRepartidor = rolId == ROL_REPARTIDOR

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvRoleLabel = findViewById<TextView>(R.id.tvRoleLabel)
        tvWelcome.text = "¡Bienvenido, $userName!"
        tvRoleLabel.text = if (isAdmin) "Panel de Administración" else "Tienda Nexbit"

        val menu = navView.menu
        menu.findItem(R.id.nav_group_admin).isVisible = isAdmin
        menu.findItem(R.id.nav_group_cliente).isVisible = isCliente
        menu.findItem(R.id.nav_group_repartidor).isVisible = isRepartidor

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_inicio -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_entregas -> {
                    startActivity(Intent(this, EntregasActivity::class.java))
                }
                R.id.nav_productos -> {
                    startActivity(Intent(this, ProductosAdminActivity::class.java))
                }
                R.id.nav_categorias -> {
                    startActivity(Intent(this, CategoriasAdminActivity::class.java))
                }
                R.id.nav_usuarios -> {
                    startActivity(Intent(this, UsuariosAdminActivity::class.java))
                }
                R.id.nav_clientes -> {
                    startActivity(Intent(this, ClientesActivity::class.java))
                }
                R.id.nav_pedidos -> {
                    startActivity(Intent(this, PedidosAdminActivity::class.java))
                }
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                }
                R.id.nav_catalogo -> {
                    startActivity(Intent(this, CatalogoActivity::class.java))
                }
                R.id.nav_carrito -> {
                    startActivity(Intent(this, CarritoActivity::class.java))
                }
                R.id.nav_mis_pedidos -> {
                    startActivity(Intent(this, MisPedidosActivity::class.java))
                }
                R.id.nav_proveedor -> {
                    startActivity(Intent(this, ProveedorActivity::class.java))
                }
                R.id.nav_pruebas -> {
                    startActivity(Intent(this, PruebasActivity::class.java))
                }
                R.id.nav_roles -> {
                    startActivity(Intent(this, RolesAdminActivity::class.java))
                }
                R.id.nav_repartidores_admin -> {
                    startActivity(Intent(this, RepartidoresAdminActivity::class.java))
                }
                R.id.nav_ayuda -> {
                    startActivity(Intent(this, AyudaActivity::class.java))
                }
                R.id.nav_contacto -> {
                    startActivity(Intent(this, ContactoActivity::class.java))
                }
                R.id.nav_perfil_repartidor -> {
                    startActivity(Intent(this, PerfilRepartidorActivity::class.java))
                }
                R.id.nav_logout -> {
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

    companion object {
        const val ROL_ADMIN = 1
        const val ROL_CLIENTE = 2
        const val ROL_REPARTIDOR = 4
    }
}
