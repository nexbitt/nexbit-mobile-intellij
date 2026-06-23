package com.example.nexbitmobile.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

class ConfirmacionPagoActivity : AppCompatActivity() {

    private var pedidoId: Int = 0
    private var total: Double = 0.0
    private var selectedImageUri: Uri? = null

    private lateinit var tvPedidoId: TextView
    private lateinit var tvTotalPagar: TextView
    private lateinit var layoutUploadBox: LinearLayout
    private lateinit var layoutPreview: LinearLayout
    private lateinit var ivPreview: ImageView
    private lateinit var btnRemoveImage: TextView
    private lateinit var btnSubirComprobante: TextView
    private lateinit var btnSkip: TextView
    private lateinit var progressBar: ProgressBar

    private val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirmacion_pago)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pedidoId = intent.getIntExtra("pedidoId", 0)
        total = intent.getDoubleExtra("total", 0.0)

        tvPedidoId = findViewById(R.id.tvPedidoId)
        tvTotalPagar = findViewById(R.id.tvTotalPagar)
        layoutUploadBox = findViewById(R.id.layoutUploadBox)
        layoutPreview = findViewById(R.id.layoutPreview)
        ivPreview = findViewById(R.id.ivPreview)
        btnRemoveImage = findViewById(R.id.btnRemoveImage)
        btnSubirComprobante = findViewById(R.id.btnSubirComprobante)
        btnSkip = findViewById(R.id.btnSkip)
        progressBar = findViewById(R.id.progressBar)

        tvPedidoId.text = "Pedido #$pedidoId"
        tvTotalPagar.text = "Total a pagar: ${formatter.format(total)}"

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { irAMisPedidos() }

        layoutUploadBox.setOnClickListener { abrirGaleria() }
        btnRemoveImage.setOnClickListener { eliminarImagen() }
        btnSubirComprobante.setOnClickListener { subirComprobante() }
        btnSkip.setOnClickListener { irAMisPedidos() }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data?.data != null) {
            selectedImageUri = data.data
            mostrarPreview()
        }
    }

    private fun mostrarPreview() {
        selectedImageUri?.let { uri ->
            ivPreview.setImageURI(uri)
            layoutPreview.visibility = android.view.View.VISIBLE
            layoutUploadBox.visibility = android.view.View.GONE
            btnSubirComprobante.visibility = android.view.View.VISIBLE
        }
    }

    private fun eliminarImagen() {
        selectedImageUri = null
        layoutPreview.visibility = android.view.View.GONE
        layoutUploadBox.visibility = android.view.View.VISIBLE
        btnSubirComprobante.visibility = android.view.View.GONE
    }

    private fun subirComprobante() {
        val uri = selectedImageUri ?: return

        progressBar.visibility = android.view.View.VISIBLE
        btnSubirComprobante.isEnabled = false

        val inputStream = contentResolver.openInputStream(uri)
        val fileName = getFileName(uri) ?: "comprobante_$pedidoId.jpg"
        val file = File(cacheDir, fileName)
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        val requestBody = file.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("comprobante", fileName, requestBody)

        ApiClient.instance.subirComprobante(pedidoId, imagePart)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    progressBar.visibility = android.view.View.GONE
                    btnSubirComprobante.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@ConfirmacionPagoActivity, "Comprobante enviado. Tu pedido está en revisión.", Toast.LENGTH_LONG).show()
                        irAMisPedidos()
                    } else {
                        Toast.makeText(this@ConfirmacionPagoActivity, "Error al subir comprobante", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    progressBar.visibility = android.view.View.GONE
                    btnSubirComprobante.isEnabled = true
                    Toast.makeText(this@ConfirmacionPagoActivity, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            if (nameIndex >= 0) it.getString(nameIndex) else null
        }
    }

    private fun irAMisPedidos() {
        val intent = Intent(this, MisPedidosActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
