package com.example.nexbitmobile.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Banco
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

class PagoTransferenciaActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PEDIDO_ID = "pedido_id"
        const val EXTRA_TOTAL = "total"
        private const val EXTRA_OPEN_SCREEN = "open_screen"
    }

    private fun openMainScreen() {
        val intent = Intent(this, ClientMainActivity::class.java)
        intent.putExtra(EXTRA_OPEN_SCREEN, "mispedidos")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private var pedidoId: Int = -1
    private var total: Double = 0.0
    private var selectedImageUri: Uri? = null
    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    private lateinit var progressBar: ProgressBar
    private lateinit var tvMontoPagar: TextView
    private lateinit var tvSuccessTitle: TextView
    private lateinit var containerBancos: LinearLayout
    private lateinit var tvBancosLoading: TextView
    private lateinit var llUploadArea: View
    private lateinit var ivUploadIcon: ImageView
    private lateinit var tvUploadText: TextView
    private lateinit var ivReceiptPreview: ImageView
    private lateinit var btnEnviarComprobante: Button
    private lateinit var btnVerPedidos: Button

    private var bancosLoaded = false
    private var imageSelected = false

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imageSelected = true
            ivReceiptPreview.visibility = View.VISIBLE
            Glide.with(this).load(it).into(ivReceiptPreview)
            tvUploadText.text = "Comprobante seleccionado"
            tvUploadText.setTextColor(resources.getColor(R.color.text_main, theme))
            btnEnviarComprobante.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pago_transferencia)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pedidoId = intent.getIntExtra(EXTRA_PEDIDO_ID, -1)
        total = intent.getDoubleExtra(EXTRA_TOTAL, 0.0)

        if (pedidoId == -1) {
            Toast.makeText(this, "Pedido invalido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        setupListeners()
        loadBancos()
    }

    private fun bindViews() {
        progressBar = findViewById(R.id.progressBar)
        tvMontoPagar = findViewById(R.id.tvMontoPagar)
        tvSuccessTitle = findViewById(R.id.tvSuccessTitle)
        containerBancos = findViewById(R.id.containerBancos)
        tvBancosLoading = findViewById(R.id.tvBancosLoading)
        llUploadArea = findViewById(R.id.llUploadArea)
        ivUploadIcon = findViewById(R.id.ivUploadIcon)
        tvUploadText = findViewById(R.id.tvUploadText)
        ivReceiptPreview = findViewById(R.id.ivReceiptPreview)
        btnEnviarComprobante = findViewById(R.id.btnEnviarComprobante)
        btnVerPedidos = findViewById(R.id.btnVerPedidos)

        tvSuccessTitle.text = "Pedido #$pedidoId creado"
        tvMontoPagar.text = formatter.format(total)
    }

    private fun setupListeners() {
        llUploadArea.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        btnEnviarComprobante.setOnClickListener {
            if (selectedImageUri != null) {
                uploadReceipt()
            }
        }

        btnVerPedidos.setOnClickListener {
            openMainScreen()
        }
    }

    private fun loadBancos() {
        tvBancosLoading.visibility = View.VISIBLE

        ApiClient.instance.getBancos().enqueue(object : Callback<List<Banco>> {
            override fun onResponse(call: Call<List<Banco>>, response: Response<List<Banco>>) {
                tvBancosLoading.visibility = View.GONE
                if (response.isSuccessful) {
                    val bancos = response.body() ?: emptyList()
                    if (bancos.isEmpty()) {
                        addEmptyBancosMessage()
                    } else {
                        bancos.forEach { banco -> addBancoCard(banco) }
                    }
                    bancosLoaded = true
                } else {
                    addEmptyBancosMessage()
                }
            }

            override fun onFailure(call: Call<List<Banco>>, t: Throwable) {
                tvBancosLoading.visibility = View.GONE
                addEmptyBancosMessage()
            }
        })
    }

    private fun addBancoCard(banco: Banco) {
        val card = LayoutInflater.from(this).inflate(R.layout.item_banco_card, containerBancos, false)
        card.findViewById<TextView>(R.id.tvBancoNombre).text = banco.banco
        card.findViewById<TextView>(R.id.tvBancoTipoCuenta).text = banco.tipo_cuenta
        card.findViewById<TextView>(R.id.tvBancoNumeroCuenta).text = banco.numero_cuenta
        card.findViewById<TextView>(R.id.tvBancoTitular).text = banco.titular
        card.findViewById<TextView>(R.id.tvBancoDocumento).text = banco.documento ?: ""
        card.findViewById<TextView>(R.id.tvBancoDescripcion).text = banco.descripcion ?: ""

        card.setOnClickListener {
            val info = "${banco.banco}\n" +
                       "${banco.tipo_cuenta}: ${banco.numero_cuenta}\n" +
                       "Titular: ${banco.titular}"
            AlertDialog.Builder(this)
                .setTitle("Datos Bancarios")
                .setMessage(info)
                .setPositiveButton("Cerrar", null)
                .show()
        }

        containerBancos.addView(card)
    }

    private fun addEmptyBancosMessage() {
        val tv = TextView(this).apply {
            text = "No hay cuentas bancarias configuradas. Contacta al administrador."
            textSize = 13f
            setTextColor(resources.getColor(R.color.text_light, theme))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8 }
        }
        containerBancos.addView(tv)
    }

    private fun uploadReceipt() {
        val imageUri = selectedImageUri ?: return

        progressBar.visibility = View.VISIBLE
        btnEnviarComprobante.isEnabled = false

        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val file = File.createTempFile("comprobante_", ".jpg", cacheDir)
            inputStream?.use { input -> input.copyTo(FileOutputStream(file)) }
            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
            val imagePart = MultipartBody.Part.createFormData(
                "comprobante",
                file.name,
                file.asRequestBody(mimeType.toMediaTypeOrNull())
            )

            ApiClient.instance.subirComprobante(pedidoId, imagePart)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        progressBar.visibility = View.GONE
                        btnEnviarComprobante.isEnabled = true
                        if (response.isSuccessful) {
                            AlertDialog.Builder(this@PagoTransferenciaActivity)
                                .setTitle("Comprobante enviado")
                                .setMessage("Tu comprobante ha sido recibido. El administrador lo revisara y confirmara el pago.")
                                .setPositiveButton("Ver mis pedidos") { _, _ ->
                                    openMainScreen()
                                }
                                .setCancelable(false)
                                .show()
                        } else {
                            val errorMsg = response.errorBody()?.string() ?: "Error al subir comprobante"
                            Toast.makeText(this@PagoTransferenciaActivity, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        btnEnviarComprobante.isEnabled = true
                        Toast.makeText(this@PagoTransferenciaActivity, "Error de conexion: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            btnEnviarComprobante.isEnabled = true
            Toast.makeText(this, "Error al procesar imagen: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
