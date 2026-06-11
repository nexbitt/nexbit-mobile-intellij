package com.example.nexbitmobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nexbitmobile.R

class CarouselAdapter(
    private val onPageClick: (Int) -> Unit
) : RecyclerView.Adapter<CarouselAdapter.PageViewHolder>() {

    private val pageLayouts = listOf(
        R.layout.carousel_page_sales,
        R.layout.carousel_page_inventory,
        R.layout.carousel_page_security,
        R.layout.carousel_page_carts,
        R.layout.carousel_page_logistics
    )

    override fun getItemCount() = pageLayouts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(pageLayouts[viewType], parent, false)
        return PageViewHolder(view, viewType, onPageClick)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {}

    override fun getItemViewType(position: Int) = position

    class PageViewHolder(
        itemView: View,
        position: Int,
        onPageClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private var expandedLayout: Int? = null

        init {
            // Map click to expand
            itemView.setOnClickListener { onPageClick(position) }
        }
    }
}