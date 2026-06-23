package com.example.nexbitmobile.ui

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.nexbitmobile.R
import com.example.nexbitmobile.util.LanguageHelper

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvTitle).text = LanguageHelper.getString(this, "faq_title")

        val container = findViewById<LinearLayout>(R.id.faqContainer)

        for (i in 1..4) {
            val qKey = "faq_q$i"
            val aKey = "faq_a$i"

            val tvQ = TextView(this).apply {
                text = LanguageHelper.getString(this@HelpActivity, qKey)
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 16, 0, 4)
            }
            val tvA = TextView(this).apply {
                text = LanguageHelper.getString(this@HelpActivity, aKey)
                textSize = 13f
                setTextColor(ContextCompat.getColor(this@HelpActivity, R.color.menu_divider))
                setPadding(0, 0, 0, 16)
            }
            container.addView(tvQ)
            container.addView(tvA)

            if (i < 4) {
                val divider = View(this).apply {
                    setBackgroundColor(ContextCompat.getColor(this@HelpActivity, R.color.menu_divider))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    )
                }
                container.addView(divider)
            }
        }
    }
}
