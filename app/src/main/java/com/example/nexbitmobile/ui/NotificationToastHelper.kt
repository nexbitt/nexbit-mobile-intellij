package com.example.nexbitmobile.ui

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.example.nexbitmobile.R
import com.example.nexbitmobile.NexbitApplication

object NotificationToastHelper {

    private var currentToast: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null

    fun show(title: String, body: String, icon: String = "🔔") {
        val context = NexbitApplication.appContext
        val activity = getCurrentActivity() ?: return

        activity.runOnUiThread {
            val rootView = activity.window.decorView.findViewById<FrameLayout>(android.R.id.content)
            if (rootView == null) return@runOnUiThread

            currentToast?.let { rootView.removeView(it) }

            val toastView = LayoutInflater.from(context).inflate(R.layout.notification_toast, rootView, false)

            toastView.findViewById<TextView>(R.id.tvNotificationIcon).text = icon
            toastView.findViewById<TextView>(R.id.tvNotificationTitle).text = title
            toastView.findViewById<TextView>(R.id.tvNotificationBody).text = body

            toastView.setOnClickListener {
                rootView.removeView(toastView)
                currentToast = null
            }

            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            rootView.addView(toastView, params)
            currentToast = toastView

            playSound(context)

            handler.postDelayed({
                rootView.removeView(toastView)
                currentToast = null
            }, 15000)
        }
    }

    private fun playSound(context: android.content.Context) {
        try {
            mediaPlayer?.release()
            val soundUri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
            mediaPlayer = MediaPlayer.create(context, soundUri)
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCurrentActivity(): android.app.Activity? {
        try {
            val activityClass = Class.forName("android.app.Activity")
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
            val activityListField = activityThreadClass.getDeclaredField("mActivities")
            activityListField.isAccessible = true
            val activities = activityListField.get(activityThread) as? Map<*, *>
            if (activities != null) {
                for ((_, value) in activities) {
                    val activityRef = value?.javaClass?.getDeclaredField("activity")
                    activityRef?.isAccessible = true
                    val activity = activityRef?.get(value) as? android.app.Activity
                    if (activity != null && !activity.isFinishing) {
                        return activity
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
