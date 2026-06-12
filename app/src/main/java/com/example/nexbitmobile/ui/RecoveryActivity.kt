package com.example.nexbitmobile.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.nexbitmobile.R
import com.example.nexbitmobile.api.ApiClient
import com.example.nexbitmobile.model.AuthResponse
import com.example.nexbitmobile.model.RecoverPasswordRequest
import com.example.nexbitmobile.model.ResetPasswordRequest
import com.example.nexbitmobile.model.VerifyOtpRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecoveryActivity : AppCompatActivity() {

    companion object {
        private const val RESEND_DELAY_MS = 60000L // 60 seconds
        private const val COUNTDOWN_INTERVAL = 1000L
    }

    // Steps
    private lateinit var stepEmail: View
    private lateinit var stepOtp: View
    private lateinit var stepPassword: View

    // Step 1
    private lateinit var etEmail: EditText
    private lateinit var tvMessageStep1: TextView
    private lateinit var btnSendCode: Button
    private lateinit var btnBackStep1: ImageButton

    // Step 2
    private lateinit var otpBoxes: Array<EditText>
    private lateinit var tvOtpDescription: TextView
    private lateinit var tvMessageStep2: TextView
    private lateinit var btnVerifyOtp: Button
    private lateinit var tvResend: TextView
    private lateinit var btnBackStep2: ImageButton

    // Step 3
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var btnToggleConfirm: ImageButton
    private lateinit var tvMessageStep3: TextView
    private lateinit var btnResetPassword: Button
    private lateinit var btnBackStep3: ImageButton

    private var email = ""
    private var resetToken = ""
    private var isPasswordVisible = false
    private var isConfirmVisible = false
    private var countDownTimer: CountDownTimer? = null
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery)

        initViews()
        setupStep1()
    }

    private fun initViews() {
        stepEmail = findViewById(R.id.stepEmail)
        stepOtp = findViewById(R.id.stepOtp)
        stepPassword = findViewById(R.id.stepPassword)

        // Step 1
        etEmail = findViewById(R.id.etEmail)
        tvMessageStep1 = findViewById(R.id.tvMessageStep1)
        btnSendCode = findViewById(R.id.btnSendCode)
        btnBackStep1 = findViewById(R.id.btnBackStep1)

        // Step 2
        tvOtpDescription = findViewById(R.id.tvOtpDescription)
        tvMessageStep2 = findViewById(R.id.tvMessageStep2)
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)
        tvResend = findViewById(R.id.tvResend)
        btnBackStep2 = findViewById(R.id.btnBackStep2)
        otpBoxes = arrayOf(
            findViewById(R.id.otp0), findViewById(R.id.otp1), findViewById(R.id.otp2),
            findViewById(R.id.otp3), findViewById(R.id.otp4), findViewById(R.id.otp5)
        )

        // Step 3
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        btnToggleConfirm = findViewById(R.id.btnToggleConfirm)
        tvMessageStep3 = findViewById(R.id.tvMessageStep3)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        btnBackStep3 = findViewById(R.id.btnBackStep3)
    }

    // ─── Navigation ────────────────────────────────────────────
    private fun goToStep(step: Int) {
        stepEmail.visibility = if (step == 1) View.VISIBLE else View.GONE
        stepOtp.visibility = if (step == 2) View.VISIBLE else View.GONE
        stepPassword.visibility = if (step == 3) View.VISIBLE else View.GONE

        if (step == 2) {
            tvOtpDescription.text = "Hemos enviado un código de 6 dígitos a $email"
            otpBoxes[0].requestFocus()
            startResendCountdown()
        }
    }

    // ─── Loading state ──────────────────────────────────────────
    private fun setLoading(loading: Boolean) {
        isLoading = loading
        etEmail.isEnabled = !loading
        btnSendCode.isEnabled = !loading
        btnVerifyOtp.isEnabled = !loading
        btnResetPassword.isEnabled = !loading
        otpBoxes.forEach { it.isEnabled = !loading }

        btnSendCode.background = getDrawable(
            if (loading) R.drawable.bg_recovery_btn_disabled
            else R.drawable.bg_recovery_btn_primary
        )
        btnVerifyOtp.background = getDrawable(
            if (loading) R.drawable.bg_recovery_btn_disabled
            else R.drawable.bg_recovery_btn_primary
        )
        btnResetPassword.background = getDrawable(
            if (loading) R.drawable.bg_recovery_btn_disabled
            else R.drawable.bg_recovery_btn_primary
        )

        if (loading) {
            btnSendCode.text = ""
            btnSendCode.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            btnVerifyOtp.text = ""
            btnResetPassword.text = ""
        } else {
            btnSendCode.text = "Enviar Código"
            btnVerifyOtp.text = "Validar Código"
            btnResetPassword.text = "Actualizar Contraseña"
        }
    }

    private fun showError(step: Int, message: String) {
        when (step) {
            1 -> { tvMessageStep1.text = message; tvMessageStep1.visibility = View.VISIBLE }
            2 -> { tvMessageStep2.text = message; tvMessageStep2.visibility = View.VISIBLE }
            3 -> { tvMessageStep3.text = message; tvMessageStep3.visibility = View.VISIBLE }
        }
    }

    private fun clearErrors() {
        tvMessageStep1.visibility = View.GONE
        tvMessageStep2.visibility = View.GONE
        tvMessageStep3.visibility = View.GONE
    }

    // ─── Resend countdown ──────────────────────────────────────
    private fun startResendCountdown() {
        countDownTimer?.cancel()
        tvResend.text = "¿No recibiste el código? Reenviar en 00:59"
        tvResend.setTextColor(getColor(R.color.recovery_text_secondary))
        tvResend.setOnClickListener(null)

        countDownTimer = object : CountDownTimer(RESEND_DELAY_MS, COUNTDOWN_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val formatted = String.format("00:%02d", seconds)
                tvResend.text = "¿No recibiste el código? Reenviar en $formatted"
            }

            override fun onFinish() {
                tvResend.text = "¿No recibiste el código? Reenviar"
                tvResend.setTextColor(getColor(R.color.recovery_primary))
                tvResend.setOnClickListener { resendOtp() }
            }
        }.start()
    }

    // ─── STEP 1: Request OTP ──────────────────────────────────
    private fun setupStep1() {
        btnBackStep1.setOnClickListener { finish() }

        etEmail.setOnFocusChangeListener { _, hasFocus ->
            etEmail.background = getDrawable(
                if (hasFocus) R.drawable.bg_recovery_input_focus
                else R.drawable.bg_recovery_input
            )
        }

        etEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                sendCode()
                true
            } else false
        }

        btnSendCode.setOnClickListener { sendCode() }

        // Send button press feedback
        btnSendCode.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> btnSendCode.animate().scaleX(0.97f).scaleY(0.97f).alpha(0.85f).setDuration(80).start()
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> btnSendCode.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(80).start()
            }
            false
        }
    }

    private fun sendCode() {
        clearErrors()
        email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            showError(1, "Ingresa tu correo electrónico.")
            etEmail.background = getDrawable(R.drawable.bg_recovery_input_error)
            return
        }
        etEmail.background = getDrawable(R.drawable.bg_recovery_input)

        setLoading(true)
        ApiClient.instance.recoverPassword(RecoverPasswordRequest(email))
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        goToStep(2)
                    } else {
                        val body = response.body()
                        showError(1, body?.message ?: "Error al enviar el código.")
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    setLoading(false)
                    showError(1, "Error de conexión. Verifica tu internet.")
                }
            })
    }

    // ─── STEP 2: Verify OTP ────────────────────────────────────
    private fun setupStep2() {
        btnBackStep2.setOnClickListener { goToStep(1) }

        otpBoxes.forEachIndexed { index, box ->
            box.filters = arrayOf(InputFilter.LengthFilter(1))
            box.inputType = InputType.TYPE_CLASS_NUMBER

            box.setOnFocusChangeListener { _, hasFocus ->
                box.background = getDrawable(
                    if (hasFocus) R.drawable.bg_recovery_otp_box_focus
                    else R.drawable.bg_recovery_otp_box
                )
            }

            box.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && index < 5) {
                        otpBoxes[index + 1].requestFocus()
                    }
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })

            box.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && box.text.isEmpty() && index > 0) {
                    otpBoxes[index - 1].requestFocus()
                    otpBoxes[index - 1].text.clear()
                }
                false
            }
        }

        btnVerifyOtp.setOnClickListener { verifyOtp() }

        // Phone-style press feedback
        btnVerifyOtp.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> btnVerifyOtp.animate().scaleX(0.97f).scaleY(0.97f).alpha(0.85f).setDuration(80).start()
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> btnVerifyOtp.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(80).start()
            }
            false
        }
    }

    private fun resendOtp() {
        setLoading(true)
        ApiClient.instance.recoverPassword(RecoverPasswordRequest(email))
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        otpBoxes.forEach { it.text.clear() }
                        otpBoxes[0].requestFocus()
                        startResendCountdown()
                    } else {
                        val body = response.body()
                        showError(2, body?.message ?: "Error al reenviar el código.")
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    setLoading(false)
                    showError(2, "Error de conexión.")
                }
            })
    }

    private fun verifyOtp() {
        clearErrors()
        val code = otpBoxes.joinToString("") { it.text.toString() }
        if (code.length != 6) {
            showError(2, "Ingresa el código completo de 6 dígitos.")
            return
        }

        setLoading(true)
        ApiClient.instance.verifyOtp(VerifyOtpRequest(email, code))
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        val body = response.body()
                        resetToken = body?.data?.token ?: ""
                        goToStep(3)
                    } else {
                        val body = response.body()
                        showError(2, body?.message ?: "Código incorrecto o expirado.")
                        otpBoxes.forEach { it.text.clear() }
                        otpBoxes[0].requestFocus()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    setLoading(false)
                    showError(2, "Error de conexión.")
                }
            })
    }

    // ─── STEP 3: Reset Password ────────────────────────────────
    private fun setupStep3() {
        btnBackStep3.setOnClickListener { goToStep(2) }

        btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            togglePasswordVisibility(etNewPassword, btnTogglePassword, isPasswordVisible)
        }
        btnToggleConfirm.setOnClickListener {
            isConfirmVisible = !isConfirmVisible
            togglePasswordVisibility(etConfirmPassword, btnToggleConfirm, isConfirmVisible)
        }

        btnResetPassword.setOnClickListener { resetPassword() }

        btnResetPassword.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> btnResetPassword.animate().scaleX(0.97f).scaleY(0.97f).alpha(0.85f).setDuration(80).start()
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> btnResetPassword.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(80).start()
            }
            false
        }
    }

    private fun togglePasswordVisibility(editText: EditText, button: ImageButton, visible: Boolean) {
        if (visible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            button.setImageResource(R.drawable.ic_eye_on)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            button.setImageResource(R.drawable.ic_eye_off)
        }
        editText.setSelection(editText.text.length)
    }

    private fun resetPassword() {
        clearErrors()
        val password = etNewPassword.text.toString()
        val confirm = etConfirmPassword.text.toString()

        if (password.length < 8) {
            showError(3, "La contraseña debe tener al menos 8 caracteres.")
            return
        }
        if (password != confirm) {
            showError(3, "Las contraseñas no coinciden.")
            return
        }

        setLoading(true)
        ApiClient.instance.resetPassword(ResetPasswordRequest(email, resetToken, password))
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        Toast.makeText(this@RecoveryActivity,
                            "Contraseña actualizada correctamente.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RecoveryActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val body = response.body()
                        showError(3, body?.message ?: "Error al actualizar la contraseña.")
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    setLoading(false)
                    showError(3, "Error de conexión.")
                }
            })
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
