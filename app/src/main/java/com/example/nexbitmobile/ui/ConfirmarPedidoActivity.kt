package com.example.nexbitmobile.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.Banco
import com.example.nexbitmobile.model.Pedido
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

class ConfirmarPedidoActivity : AppCompatActivity() {

    private var pedidoId: Int = -1
    private var pedido: Pedido? = null
    private var selectedImageUri: Uri? = null
    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    private lateinit var progressBar: ProgressBar
    private lateinit var tvOrderId: TextView
    private lateinit var tvOrderStatus: TextView
    private lateinit var tvOrderTotal: TextView
    private lateinit var tvOrderDate: TextView
    private lateinit var tvOrderAddress: TextView
    private lateinit var tvReceiptStatus: TextView
    private lateinit var ivReceiptPreview: ImageView
    private lateinit var btnUploadReceipt: Button
    private lateinit var containerBancos: LinearLayout
    private lateinit var tvBancosLoading: TextView
    private lateinit var cardReceiptStatus: View
    private lateinit var cardBancos: View

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivReceiptPreview.visibility = View.VISIBLE
            Glide.with(this).load(it).into(ivReceiptPreview)
            tvReceiptStatus.text = "Imagen seleccionada. Presiona 'Subir Comprobante' para enviar."
            tvReceiptStatus.setTextColor(resources.getColor(R.color.info, theme))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirmar_pedido)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pedidoId = intent.getIntExtra("pedido_id", -1)
        if (pedidoId == -1) {
            Toast.makeText(this, "Pedido inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        setupListeners()
        loadPedido()
        loadBancos()
    }

    private fun bindViews() {
        progressBar = findViewById(R.id.progressBar)
        tvOrderId = findViewById(R.id.tvOrderId)
        tvOrderStatus = findViewById(R.id.tvOrderStatus)
        tvOrderTotal = findViewById(R.id.tvOrderTotal)
        tvOrderDate = findViewById(R.id.tvOrderDate)
        tvOrderAddress = findViewById(R.id.tvOrderAddress)
        tvReceiptStatus = findViewById(R.id.tvReceiptStatus)
        ivReceiptPreview = findViewById(R.id.ivReceiptPreview)
        btnUploadReceipt = findViewById(R.id.btnUploadReceipt)
        containerBancos = findViewById(R.id.containerBancos)
        tvBancosLoading = findViewById(R.id.tvBancosLoading)
        cardReceiptStatus = findViewById(R.id.cardReceiptStatus)
        cardBancos = findViewById(R.id.cardBancos)
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        btnUploadReceipt.setOnClickListener {
            if (selectedImageUri != null) {
                uploadReceipt()
            } else {
                selectImageLauncher.launch("image/*")
            }
        }
    }

    private fun loadPedido() {
        progressBar.visibility = View.VISIBLE

        ApiClient.instance.getPedido(pedidoId).enqueue(object : Callback<Pedido> {
            override fun onResponse(call: Call<Pedido>, response: Response<Pedido>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    pedido = response.body()
                    pedido?.let { populateOrderInfo(it) }
                } else {
                    Toast.makeText(this@ConfirmarPedidoActivity, "Error al cargar pedido", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Pedido>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ConfirmarPedidoActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateOrderInfo(pedido: Pedido) {
        tvOrderId.text = "Pedido #${pedido.id_pedido}"
        tvOrderStatus.text = "Estado: ${pedido.estado}"
        tvOrderTotal.text = "Total: ${formatter.format(pedido.total)}"
        tvOrderDate.text = "Fecha: ${pedido.fecha_pedido?.take(16)?.replace("T", " ") ?: pedido.fecha?.take(16)?.replace("T", " ") ?: "N/A"}"
        tvOrderAddress.text = "Dirección: ${pedido.direccion_entrega ?: "No especificada"}"

        updateReceiptStatus(pedido.estado)
    }

    private fun updateReceiptStatus(estado: String) {
        when (estado) {
            "PENDIENTE", "CONFIRMADO" -> {
                tvReceiptStatus.text = "Pendiente de pago. Sube tu comprobante aquí."
                tvReceiptStatus.setTextColor(resources.getColor(R.color.warning, theme))
                btnUploadReceipt.isEnabled = true
                btnUploadReceipt.text = "Seleccionar Comprobante"
            }
            "EN_REVISION" -> {
                tvReceiptStatus.text = "Comprobante recibido. Esperando confirmación del administrador."
                tvReceiptStatus.setTextColor(resources.getColor(R.color.info, theme))
                btnUploadReceipt.isEnabled = false
                btnUploadReceipt.text = "Comprobante en revisión"
            }
            "APROBADO", "ASIGNADO", "EN_CAMINO" -> {
                tvReceiptStatus.text = "Pago aprobado. Tu pedido está en proceso."
                tvReceiptStatus.setTextColor(resources.getColor(R.color.success, theme))
                btnUploadReceipt.isEnabled = false
                btnUploadReceipt.text = "Pago aprobado"
            }
            "RECHAZADO" -> {
                tvReceiptStatus.text = "Pago rechazado. Sube un nuevo comprobante."
                tvReceiptStatus.setTextColor(resources.getColor(R.color.error_text, theme))
                btnUploadReceipt.isEnabled = true
                btnUploadReceipt.text = "Subir nuevo comprobante"
            }
            "ENTREGADO" -> {
                tvReceiptStatus.text = "Pedido entregado. Gracias por tu compra."
                tvReceiptStatus.setTextColor(resources.getColor(R.color.success, theme))
                btnUploadReceipt.isEnabled = false
                btnUploadReceipt.text = "Entregado"
            }
            "CANCELADO" -> {
                tvReceiptStatus.text = "Pedido cancelado."
                tvReceiptStatus.setTextColor(resources.getColor(R.color.error_text, theme))
                btnUploadReceipt.isEnabled = false
                btnUploadReceipt.text = "Cancelado"
            }
            else -> {
                tvReceiptStatus.text = "Estado: $estado"
                tvReceiptStatus.setTextColor(resources.getColor(R.color.text_secondary, theme))
            }
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
            androidx.appcompat.app.AlertDialog.Builder(this)
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
        btnUploadReceipt.isEnabled = false

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
                        btnUploadReceipt.isEnabled = true
                        if (response.isSuccessful) {
                            tvReceiptStatus.text = "Comprobante recibido. Esperando confirmación del administrador."
                            tvReceiptStatus.setTextColor(resources.getColor(R.color.info, theme))
                            btnUploadReceipt.text = "Comprobante en revisión"
                            btnUploadReceipt.isEnabled = false
                            Toast.makeText(this@ConfirmarPedidoActivity, "Comprobante subido con éxito", Toast.LENGTH_SHORT).show()
                            loadPedido()
                        } else {
                            val errorMsg = response.errorBody()?.string() ?: "Error al subir comprobante"
                            Toast.makeText(this@ConfirmarPedidoActivity, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        btnUploadReceipt.isEnabled = true
                        Toast.makeText(this@ConfirmarPedidoActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            btnUploadReceipt.isEnabled = true
            Toast.makeText(this, "Error al procesar imagen: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
