package com.example.nexbitmobile.api

import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketManager {
    var socket: Socket? = null
    private var listeners = mutableListOf<SocketEventListener>()

    interface SocketEventListener {
        fun onEvent(event: String, data: JSONObject)
    }

    fun connectToServer(userId: String, userRole: String) {
        if (socket?.connected() == true) return

        val options = IO.Options().apply {
            forceNew = true
            reconnection = true
            reconnectionAttempts = 20
            reconnectionDelay = 1000
            reconnectionDelayMax = 15000
            transports = arrayOf("websocket")
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
