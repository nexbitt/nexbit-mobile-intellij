package com.example.nexbitmobile.api

import android.content.Context
import com.example.nexbitmobile.NexbitApplication
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketManager {

    private var socket: Socket? = null
    private var listeners = mutableListOf<SocketEventListener>()

    interface SocketEventListener {
        fun onEvent(event: String, data: JSONObject)
    }

    fun connect() {
        if (socket?.connected() == true) return

        val prefs = NexbitApplication.appContext.getSharedPreferences("app", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)
        val userRole = prefs.getString("userRole", "") ?: ""

        if (userId == 0) return

        try {
            val options = IO.Options().apply {
                forceNew = true
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 3000
                query = "userId=$userId&userRole=$userRole"
            }

            socket = IO.socket("http://10.0.2.2:3000", options)

            socket?.on("notificacion:nuevo-pedido") { args ->
                val data = args[0] as? JSONObject ?: JSONObject()
                notifyListeners("nuevo-pedido", data)
            }

            socket?.on("notificacion:nuevo-comprobante") { args ->
                val data = args[0] as? JSONObject ?: JSONObject()
                notifyListeners("nuevo-comprobante", data)
            }

            socket?.on("pedido:disponible-nuevo") { args ->
                val data = args[0] as? JSONObject ?: JSONObject()
                notifyListeners("pedido-disponible", data)
            }

            socket?.on("pedido:estado") { args ->
                val data = args[0] as? JSONObject ?: JSONObject()
                notifyListeners("pedido-estado", data)
            }

            socket?.on("notificacion:pago-aprobado") { args ->
                val data = args[0] as? JSONObject ?: JSONObject()
                notifyListeners("pago-aprobado", data)
            }

            socket?.on("notificacion:pago-rechazado") { args ->
                val data = args[0] as? JSONObject ?: JSONObject()
                notifyListeners("pago-rechazado", data)
            }

            socket?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    fun addListener(listener: SocketEventListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: SocketEventListener) {
        listeners.remove(listener)
    }

    fun joinChat(conversacionId: Int) {
        socket?.emit("chat:join", conversacionId)
    }

    fun leaveChat(conversacionId: Int) {
        socket?.emit("chat:leave", conversacionId)
    }

    private fun notifyListeners(event: String, data: JSONObject) {
        for (listener in listeners.toList()) {
            listener.onEvent(event, data)
        }
    }
}
