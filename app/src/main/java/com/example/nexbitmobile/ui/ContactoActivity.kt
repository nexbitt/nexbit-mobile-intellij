package com.example.nexbitmobile.ui

<<<<<<< HEAD
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nexbitmobile.R

class ContactoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contacto)

        val toolbar = findViewById<Toolbar>(R.id.toolbarContacto)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        ViewCompat.setOnApplyWindowInsetsListener(toolbar.rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btnEnviarMensaje).setOnClickListener {
            val nombre = findViewById<EditText>(R.id.etContactoNombre).text.toString().trim()
            val email = findViewById<EditText>(R.id.etContactoEmail).text.toString().trim()
            val asunto = findViewById<EditText>(R.id.etContactoAsunto).text.toString().trim()
            val mensaje = findViewById<EditText>(R.id.etContactoMensaje).text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || asunto.isEmpty() || mensaje.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Correo electrónico inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simular envío
            Toast.makeText(this, "Mensaje enviado con éxito", Toast.LENGTH_SHORT).show()
            finish()
=======
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.nexbitmobile.R
import com.example.nexbitmobile.util.LanguageHelper

class ContactoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacto)

        findViewById<TextView>(R.id.tvTitle).text = LanguageHelper.getString(this, "soporte_title")
        findViewById<TextView>(R.id.tvDesc).text = LanguageHelper.getString(this, "soporte_desc")
        findViewById<TextView>(R.id.tvEmail).text = LanguageHelper.getString(this, "soporte_email")
        findViewById<TextView>(R.id.tvTel).text = LanguageHelper.getString(this, "soporte_tel")
        findViewById<TextView>(R.id.btnChat).text = LanguageHelper.getString(this, "soporte_chat")

        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<TextView>(R.id.btnChat).setOnClickListener {
            // Opens ChatActivity (generic) - could be enhanced with a specific support conversation
            startActivity(Intent(this, ChatActivity::class.java))
>>>>>>> origin/main
        }
    }
}
