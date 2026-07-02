package com.example.nexbitmobile

import android.app.Application
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey

class NexbitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // Configure Glide with disk caching for product images
        Glide.init(this, GlideBuilder()
            .setDefaultRequestOptions(
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
            )
        )

        // Socket se conecta bajo demanda tras login exitoso (ver LoginActivity)
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}
