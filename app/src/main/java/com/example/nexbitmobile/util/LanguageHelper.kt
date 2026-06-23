package com.example.nexbitmobile.util

import android.content.Context
import android.content.SharedPreferences

object LanguageHelper {
    private const val PREFS_NAME = "lang_prefs"
    private const val KEY_LANG = "locale"

    private val strings = mapOf(
        "es" to mapOf(
            "app_name" to "Nexbit",
            "catalogo" to "Catálogo",
            "pedidos" to "Mis Pedidos",
            "perfil" to "Perfil",
            "inicio_sesion" to "Iniciar Sesión",
            "cerrar_sesion" to "Cerrar Sesión",
            "carrito" to "Carrito",
            "total" to "Total",
            "comprar" to "Comprar",
            "buscar" to "Buscar productos...",
            "bienvenido" to "Bienvenido",
            "cargando" to "Cargando...",
            "error_red" to "Error de red",
            "sin_resultados" to "Sin resultados",
            "idioma" to "Idioma",
            "ayuda" to "Ayuda",
            "contacto" to "Contacto",
            "faq_title" to "Preguntas Frecuentes",
            "faq_q1" to "¿Cómo puedo hacer un pedido?",
            "faq_a1" to "Navega por el catálogo, agrega productos al carrito y finaliza la compra.",
            "faq_q2" to "¿Cómo puedo rastrear mi pedido?",
            "faq_a2" to "Ve a la sección Mis Pedidos y selecciona el pedido activo para ver su estado.",
            "faq_q3" to "¿Cómo contacto con soporte?",
            "faq_a3" to "Usa la sección de Contacto para enviar un mensaje o llamar directamente.",
            "faq_q4" to "¿Cuáles son los métodos de pago?",
            "faq_a4" to "Aceptamos transferencias bancarias y pago contra entrega.",
            "soporte_title" to "Soporte y Contacto",
            "soporte_desc" to "Puedes contactarnos a través de los siguientes canales:",
            "soporte_email" to "Correo: soporte@nexbit.com",
            "soporte_tel" to "Teléfono: +57 300 123 4567",
            "soporte_chat" to "Abrir Chat de Soporte",
            "home" to "Inicio",
            "configuracion" to "Configuración",
            "reportes" to "Reportes",
            "admin_panel" to "Panel Admin"
        ),
        "en" to mapOf(
            "app_name" to "Nexbit",
            "catalogo" to "Catalog",
            "pedidos" to "My Orders",
            "perfil" to "Profile",
            "inicio_sesion" to "Sign In",
            "cerrar_sesion" to "Sign Out",
            "carrito" to "Cart",
            "total" to "Total",
            "comprar" to "Buy",
            "buscar" to "Search products...",
            "bienvenido" to "Welcome",
            "cargando" to "Loading...",
            "error_red" to "Network error",
            "sin_resultados" to "No results",
            "idioma" to "Language",
            "ayuda" to "Help",
            "contacto" to "Contact",
            "faq_title" to "Frequently Asked Questions",
            "faq_q1" to "How do I place an order?",
            "faq_a1" to "Browse the catalog, add products to cart, and checkout.",
            "faq_q2" to "How do I track my order?",
            "faq_a2" to "Go to My Orders and select the active order to see its status.",
            "faq_q3" to "How do I contact support?",
            "faq_a3" to "Use the Contact section to send a message or call directly.",
            "faq_q4" to "What payment methods are available?",
            "faq_a4" to "We accept bank transfers and cash on delivery.",
            "soporte_title" to "Support & Contact",
            "soporte_desc" to "You can reach us through the following channels:",
            "soporte_email" to "Email: soporte@nexbit.com",
            "soporte_tel" to "Phone: +57 300 123 4567",
            "soporte_chat" to "Open Support Chat",
            "home" to "Home",
            "configuracion" to "Settings",
            "reportes" to "Reports",
            "admin_panel" to "Admin Panel"
        )
    )

    fun getSavedLocale(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, "es") ?: "es"
    }

    fun setLocale(context: Context, locale: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, locale).apply()
    }

    fun getString(context: Context, key: String): String {
        val locale = getSavedLocale(context)
        return strings[locale]?.get(key) ?: strings["es"]?.get(key) ?: key
    }

    fun isSpanish(context: Context) = getSavedLocale(context) == "es"
}
