package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
<<<<<<< HEAD
import android.view.ViewGroup
=======
>>>>>>> origin/main
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.*
import com.example.nexbitmobile.util.LanguageHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PerfilActivity : AppCompatActivity() {

    private var currentUserId = 0
    private lateinit var tabContainer: LinearLayout
    private lateinit var tabContent: FrameLayout
    private val tabs = listOf(
        Triple("tabDatosPersonales", "Datos Personales", R.id.tabDatosPersonales),
        Triple("tabPapelera", "Papelera", R.id.tabPapelera),
        Triple("tabSeguridad", "Seguridad", R.id.tabSeguridad),
        Triple("tabConfiguracion", "Configuración", R.id.tabConfiguracion)
    )
    private var currentTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        val toolbar = findViewById<Toolbar>(R.id.toolbarPerfil)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        currentUserId = prefs.getInt("userId", 0)

        tabContainer = findViewById(R.id.tabContainer)
        tabContent = findViewById(R.id.tabContent)

        setupTabs()
        showTab(0)
    }

    private fun setupTabs() {
        val tabViews = listOf(
            findViewById<TextView>(R.id.tabDatosPersonales),
            findViewById<TextView>(R.id.tabPapelera),
            findViewById<TextView>(R.id.tabSeguridad),
            findViewById<TextView>(R.id.tabConfiguracion)
        )
        for ((i, tv) in tabViews.withIndex()) {
            tv.setOnClickListener { showTab(i) }
        }
    }

    private fun showTab(index: Int) {
        currentTab = index
        val tabViews = listOf(
            findViewById<TextView>(R.id.tabDatosPersonales),
            findViewById<TextView>(R.id.tabPapelera),
            findViewById<TextView>(R.id.tabSeguridad),
            findViewById<TextView>(R.id.tabConfiguracion)
        )
        for ((i, tv) in tabViews.withIndex()) {
            tv.setBackgroundResource(if (i == index) R.drawable.bg_chip_selected else R.drawable.bg_chip)
            tv.setTextColor(ContextCompat.getColor(this,
                if (i == index) R.color.chip_selected_text else R.color.chip_text))
        }
        tabContent.removeAllViews()
        when (index) {
            0 -> showDatosPersonales()
            1 -> showPapelera()
            2 -> showSeguridad()
            3 -> showConfiguracion()
        }
    }

    private fun showDatosPersonales() {
        val scroll = ScrollView(this)
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        val userName = prefs.getString("userName", "Usuario") ?: "Usuario"

        // Avatar
        val avatarCard = com.google.android.material.card.MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 16 }
            radius = 12f; cardElevation = 3f; setContentPadding(16, 16, 16, 16)
        }
        val avatarRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL }
        avatarRow.addView(TextView(this).apply {
            text = userName.first().uppercase()
            textSize = 32f; setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@PerfilActivity, R.color.primary))
            layoutParams = LinearLayout.LayoutParams(72, 72).apply { marginEnd = 16 }
            gravity = android.view.Gravity.CENTER
            setBackgroundResource(R.drawable.bg_avatar_circle)
        })
        avatarRow.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@PerfilActivity).apply { text = userName; textSize = 18f; setTypeface(null, android.graphics.Typeface.BOLD) })
            addView(TextView(this@PerfilActivity).apply {
                text = prefs.getString("userEmail", "") ?: ""
                textSize = 13f; setTextColor(ContextCompat.getColor(this@PerfilActivity, R.color.text_secondary))
            })
        })
        avatarCard.addView(avatarRow)
        container.addView(avatarCard)

        // Fields
        val fieldsCard = com.google.android.material.card.MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 16 }
            radius = 12f; cardElevation = 2f; setContentPadding(16, 16, 16, 16)
        }
        val fieldsContent = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        fieldsContent.addView(TextView(this).apply { text = "Información Personal"; setTypeface(null, android.graphics.Typeface.BOLD); textSize = 16f; setPadding(0, 0, 0, 12) })

        val etNombre = createField(fieldsContent, "Nombre Completo", prefs.getString("userName", "") ?: "")
        val etEmail = createField(fieldsContent, "Correo Electrónico", prefs.getString("userEmail", "") ?: "")
        val etTelefono = createField(fieldsContent, "Teléfono", prefs.getString("userPhone", "") ?: "")
        val etDireccion = createField(fieldsContent, "Dirección", prefs.getString("userAddress", "") ?: "")

        fieldsContent.addView(Button(this).apply {
            text = "Guardar Cambios"
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 48).apply { topMargin = 16 }
            setBackgroundResource(R.drawable.bg_btn_primary)
            setTextColor(ContextCompat.getColor(this@PerfilActivity, R.color.white))
            setOnClickListener {
                guardarPerfil(etNombre.text.toString(), etEmail.text.toString(), etTelefono.text.toString(), etDireccion.text.toString())
            }
        })
        fieldsCard.addView(fieldsContent)
        container.addView(fieldsCard)

        container.addView(Button(this).apply {
            text = "Cerrar Sesión"
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 48)
            setBackgroundResource(R.drawable.bg_logout_btn)
            setOnClickListener {
                prefs.edit().clear().apply()
                startActivity(Intent(this@PerfilActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        })

        scroll.addView(container)
        tabContent.addView(scroll)
    }

    private fun showPapelera() {
        val scroll = ScrollView(this)
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 8, 0, 0) }
        container.addView(TextView(this).apply {
            text = "Pedidos Cancelados"
            textSize = 16f; setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0, 0, 0, 12)
        })

        ApiClient.instance.getMisPedidos(currentUserId).enqueue(object : Callback<List<Pedido>> {
            override fun onResponse(call: Call<List<Pedido>>, response: Response<List<Pedido>>) {
                val cancelados = (response.body() ?: emptyList()).filter { it.estado == "CANCELADO" }
                if (cancelados.isEmpty()) {
                    container.addView(TextView(this@PerfilActivity).apply {
                        text = "No hay pedidos cancelados"
                        setTextColor(ContextCompat.getColor(this@PerfilActivity, R.color.text_secondary))
                        setPadding(0, 24, 0, 0); gravity = android.view.Gravity.CENTER
                    })
                } else {
                    for (p in cancelados) {
                        val card = com.google.android.material.card.MaterialCardView(this@PerfilActivity).apply {
                            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 8 }
                            radius = 8f; cardElevation = 2f; setContentPadding(12, 12, 12, 12)
                        }
                        val row = LinearLayout(this@PerfilActivity).apply { orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL }
                        row.addView(LinearLayout(this@PerfilActivity).apply {
                            orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            addView(TextView(this@PerfilActivity).apply { text = "#${p.id_pedido}"; setTypeface(null, android.graphics.Typeface.BOLD) })
                            addView(TextView(this@PerfilActivity).apply {
                                text = "${p.fecha.take(10)} - ${"$"}${String.format("%,.2f", p.total)}"
                                textSize = 12f; setTextColor(ContextCompat.getColor(this@PerfilActivity, R.color.text_secondary))
                            })
                        })
                        row.addView(Button(this@PerfilActivity).apply {
                            text = "Restaurar"
                            textSize = 11f
                            setOnClickListener {
                                ApiClient.instance.cancelarPedido(p.id_pedido).enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                        Toast.makeText(this@PerfilActivity, "Pedido restaurado", Toast.LENGTH_SHORT).show()
                                        showPapelera()
                                    }
                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                        Toast.makeText(this@PerfilActivity, "Error", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                        })
                        card.addView(row)
                        container.addView(card)
                    }
                }
            }
            override fun onFailure(call: Call<List<Pedido>>, t: Throwable) {
                container.addView(TextView(this@PerfilActivity).apply {
                    text = "Error de conexión"; setPadding(0, 24, 0, 0)
                })
            }
        })

        scroll.addView(container)
        tabContent.addView(scroll)
    }

    private fun showSeguridad() {
        val scroll = ScrollView(this)
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 8, 0, 0) }

        val card = com.google.android.material.card.MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 16 }
            radius = 12f; cardElevation = 2f; setContentPadding(16, 16, 16, 16)
        }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        content.addView(TextView(this).apply { text = "Cambiar Contraseña"; setTypeface(null, android.graphics.Typeface.BOLD); textSize = 16f; setPadding(0, 0, 0, 12) })
        val etActual = createField(content, "Contraseña actual", "")
        val etNueva = createField(content, "Nueva contraseña", "")
        val etConfirmar = createField(content, "Confirmar contraseña", "")

        content.addView(Button(this).apply {
            text = "Actualizar Contraseña"
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 44).apply { topMargin = 8; bottomMargin = 24 }
            setBackgroundResource(R.drawable.bg_btn_primary)
            setTextColor(ContextCompat.getColor(this@PerfilActivity, R.color.white))
            setOnClickListener {
                Toast.makeText(this@PerfilActivity, "Contraseña actualizada (demo)", Toast.LENGTH_SHORT).show()
            }
        })

        // Toggles
        content.addView(createToggle("Autenticación 2 Pasos", false))
        content.addView(createToggle("Notificaciones", true))

        card.addView(content)
        container.addView(card)
        scroll.addView(container)
        tabContent.addView(scroll)
    }

    private fun showConfiguracion() {
        val scroll = ScrollView(this)
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 8, 0, 0) }

        val card = com.google.android.material.card.MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            radius = 12f; cardElevation = 2f; setContentPadding(16, 16, 16, 16)
        }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(TextView(this).apply { text = "Configuración"; setTypeface(null, android.graphics.Typeface.BOLD); textSize = 16f; setPadding(0, 0, 0, 12) })

        content.addView(createToggle("Modo Oscuro", false))

        content.addView(Button(this).apply {
            text = "Exportar datos"
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 44).apply { topMargin = 12 }
            setBackgroundResource(R.drawable.bg_btn_secondary)
            setOnClickListener { Toast.makeText(this@PerfilActivity, "Exportar datos (demo)", Toast.LENGTH_SHORT).show() }
        })

        content.addView(Button(this).apply {
            text = "Configurar preferencias"
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 44).apply { topMargin = 8 }
            setBackgroundResource(R.drawable.bg_btn_secondary)
            setOnClickListener { Toast.makeText(this@PerfilActivity, "Preferencias (demo)", Toast.LENGTH_SHORT).show() }
        })

        card.addView(content)
        container.addView(card)
        scroll.addView(container)
        tabContent.addView(scroll)
    }

    private fun createField(container: LinearLayout, hint: String, value: String): EditText {
        val et = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 48).apply { bottomMargin = 12 }
            setBackgroundResource(R.drawable.bg_input)
            setPadding(14, 0, 14, 0)
            this.hint = hint
            setText(value)
            textSize = 14f
        }
        container.addView(et)
        return et
    }

    private fun createToggle(label: String, defaultState: Boolean): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 8 }
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        row.addView(TextView(this).apply {
            text = label
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            textSize = 14f
        })
        val sw = Switch(this).apply {
            isChecked = defaultState
            setOnCheckedChangeListener { _, isChecked ->
                Toast.makeText(this@PerfilActivity, "$label: ${if (isChecked) "ON" else "OFF"}", Toast.LENGTH_SHORT).show()
            }
        }
        row.addView(sw)
        return row
    }

    private fun guardarPerfil(nombre: String, email: String, telefono: String, direccion: String) {
        if (nombre.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nombre y correo son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }
        val req = UsuarioUpdateRequest(
            nombre = nombre,
            email = email,
            telefono = telefono.ifEmpty { null },
            direccion = direccion.ifEmpty { null }
        )
        if (currentUserId != 0) {
            ApiClient.instance.updateUsuario(currentUserId, req).enqueue(object : Callback<Usuario> {
                override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                    if (response.isSuccessful) {
                        val u = response.body()
                        if (u != null) {
                            getSharedPreferences("app", MODE_PRIVATE).edit()
                                .putString("userName", u.nombre)
                                .putString("userEmail", u.email)
                                .putString("userPhone", u.telefono)
                                .putString("userAddress", u.direccion)
                                .apply()
                        }
                        Toast.makeText(this@PerfilActivity, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@PerfilActivity, "Error (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Usuario>, t: Throwable) {
                    Toast.makeText(this@PerfilActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
<<<<<<< HEAD
=======
        } else {
            tvLoadingStatus.text = "No se encontró ID de usuario"
            tvLoadingStatus.setTextColor(resources.getColor(R.color.error_text, theme))
        }

        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email = etCorreo.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Nombre y correo son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val req = UsuarioUpdateRequest(
                nombre = nombre,
                email = email,
                tipo_documento = etDocType.text.toString().trim().takeIf { it.isNotEmpty() },
                numero_documento = etDocNum.text.toString().trim().takeIf { it.isNotEmpty() },
                telefono = etTelefono.text.toString().trim().takeIf { it.isNotEmpty() },
                direccion = etDireccion.text.toString().trim().takeIf { it.isNotEmpty() }
            )

            if (currentUserId != 0) {
                ApiClient.instance.updateUsuario(currentUserId, req).enqueue(object : Callback<Usuario> {
                    override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                        if (response.isSuccessful) {
                            val u = response.body()
                            if (u != null) {
                                prefs.edit()
                                    .putString("userName", u.nombre)
                                    .putString("userEmail", u.email)
                                    .putString("userDocType", u.tipo_documento)
                                    .putString("userDocNum", u.numero_documento)
                                    .putString("userPhone", u.telefono)
                                    .putString("userAddress", u.direccion)
                                    .apply()

                                tvHeaderName.text = u.nombre
                                tvHeaderEmail.text = u.email
                                if (u.nombre.isNotEmpty()) {
                                    tvAvatarInitial.text = u.nombre.first().uppercase()
                                }
                            }
                            Toast.makeText(this@PerfilActivity, "Perfil actualizado con éxito!", Toast.LENGTH_SHORT).show()
                        } else {
                            val errorMsg = when (response.code()) {
                                400 -> "Datos inválidos"
                                409 -> "El correo ya está en uso"
                                else -> "Error al actualizar (${response.code()})"
                            }
                            Toast.makeText(this@PerfilActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Usuario>, t: Throwable) {
                        Toast.makeText(this@PerfilActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "No se encontró ID de usuario. Vuelve a iniciar sesión.", Toast.LENGTH_SHORT).show()
            }
        }

        // ─── Idioma / Language Toggle ───────────────────────────────────────
        val switchLang = findViewById<Switch>(R.id.switchLang)
        val tvLangLabel = findViewById<TextView>(R.id.tvLangLabel)
        val tvLangSub = findViewById<TextView>(R.id.tvLangSub)

        val isSpanish = LanguageHelper.isSpanish(this)
        switchLang.isChecked = !isSpanish
        tvLangLabel.text = if (isSpanish) "Idioma / Language" else "Language / Idioma"
        tvLangSub.text = if (isSpanish) "Español" else "English"

        switchLang.setOnCheckedChangeListener { _, isChecked ->
            val newLocale = if (isChecked) "en" else "es"
            LanguageHelper.setLocale(this, newLocale)
            tvLangLabel.text = if (isChecked) "Language / Idioma" else "Idioma / Language"
            tvLangSub.text = if (isChecked) "English" else "Español"
            Toast.makeText(this, if (isChecked) "Language changed to English" else "Idioma cambiado a Español", Toast.LENGTH_SHORT).show()
        }

        // ─── Logout ─────────────────────────────────────────────────────────
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
>>>>>>> origin/main
        }
    }
}
