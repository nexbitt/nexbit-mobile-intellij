package com.example.nexbitmobile.ui.components

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.nexbitmobile.R
import com.example.nexbitmobile.ui.CarouselAdapter

class MainCarouselComponent(private val activity: AppCompatActivity) {

    lateinit var carouselPager: ViewPager2
    lateinit var pageIndicator: LinearLayout
    lateinit var expandedContainer: FrameLayout
    lateinit var carouselCard: com.google.android.material.card.MaterialCardView
    var isExpanded = false
    var pageIndicatorDots = mutableListOf<View>()
    val carouselLayouts = listOf(
        R.layout.expanded_sales,
        R.layout.expanded_inventory,
        R.layout.expanded_security,
        R.layout.expanded_carts,
        R.layout.expanded_logistics
    )
    private var autoRotateRunnable: Runnable? = null
    private val carouselHandler = Handler(Looper.getMainLooper())
    private var onPageClick: (() -> Unit)? = null

    fun setOnPageClick(callback: () -> Unit) {
        onPageClick = callback
    }

    fun setup(rootView: View) {
        carouselPager = rootView.findViewById(R.id.carouselPager)
        pageIndicator = rootView.findViewById(R.id.pageIndicator)
        expandedContainer = rootView.findViewById(R.id.expandedContainer)
        carouselCard = rootView.findViewById(R.id.carouselCard)

        setupCarousel()
        setupPageIndicator()
    }

    private fun setupCarousel() {
        carouselPager.adapter = CarouselAdapter { position: Int -> onPageClick?.invoke() }
        carouselPager.offscreenPageLimit = 5
        carouselPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageIndicator(position)
                stopAutoRotate()
                startAutoRotate()
            }
        })

        carouselPager.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) stopAutoRotate()
            if (event.action == MotionEvent.ACTION_UP) startAutoRotate()
            false
        }

        startAutoRotate()
    }

    private fun setupPageIndicator() {
        pageIndicator.removeAllViews()
        pageIndicatorDots.clear()
        for (i in 0 until 5) {
            val dot = View(activity)
            val size = if (i == 0) 12 else 6
            val lp = LinearLayout.LayoutParams(dp(size), 6)
            lp.marginEnd = 4
            dot.layoutParams = lp
            dot.background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 3f
                setColor(
                    if (i == 0) activity.resources.getColor(R.color.nav_active, activity.theme)
                    else activity.resources.getColor(R.color.tab_inactive, activity.theme)
                )
            }
            pageIndicator.addView(dot)
            pageIndicatorDots.add(dot)
        }
    }

    private fun updatePageIndicator(active: Int) {
        for ((i, dot) in pageIndicatorDots.withIndex()) {
            val isActive = i == active
            val size = if (isActive) 12 else 6
            val lp = dot.layoutParams
            lp.width = dp(size)
            lp.height = 6
            dot.layoutParams = lp
            val bg = dot.background as android.graphics.drawable.GradientDrawable
            bg.setColor(
                if (isActive) activity.resources.getColor(R.color.nav_active, activity.theme)
                else activity.resources.getColor(R.color.tab_inactive, activity.theme)
            )
        }
    }

    fun startAutoRotate() {
        stopAutoRotate()
        if (isExpanded) return
        autoRotateRunnable = Runnable {
            val next = (carouselPager.currentItem + 1) % 5
            carouselPager.setCurrentItem(next, true)
        }
        carouselHandler.postDelayed(autoRotateRunnable!!, 5000)
    }

    fun stopAutoRotate() {
        autoRotateRunnable?.let { carouselHandler.removeCallbacks(it) }
        autoRotateRunnable = null
    }

    fun cleanup() {
        stopAutoRotate()
    }

    private fun dp(value: Int): Int {
        return (value * activity.resources.displayMetrics.density).toInt()
    }
}
