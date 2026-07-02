package com.example.nexbitmobile
import com.example.nexbitmobile.ui.EntregasActivity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
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
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbarMain)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.adminDashboardFragment, R.id.catalogoFragment, R.id.carritoFragment),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val rolNombre = prefs.getString("userRole", "") ?: ""
        val userName = prefs.getString("userName", "Usuario") ?: "Usuario"
        val isAdmin = rolNombre == "Administrador"
        val isCliente = rolNombre == "Cliente"
        val isRepartidor = rolNombre == "Repartidor"

        val menu = navView.menu
        menu.findItem(R.id.nav_group_admin).isVisible = isAdmin
        menu.findItem(R.id.nav_group_cliente).isVisible = isCliente
        menu.findItem(R.id.nav_group_repartidor).isVisible = isRepartidor

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_inicio -> {
                    navController.navigate(R.id.adminDashboardFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_catalogo -> {
                    navController.navigate(R.id.catalogoFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_carrito -> {
                    navController.navigate(R.id.carritoFragment)
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
                R.id.nav_mis_pedidos -> {
                    startActivity(Intent(this, MisPedidosActivity::class.java))
                }
                R.id.nav_proveedor -> {
                    startActivity(Intent(this, ProveedorActivity::class.java))
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

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
