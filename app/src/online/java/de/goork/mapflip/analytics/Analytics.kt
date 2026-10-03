package de.goork.mapflip.analytics

import android.content.Context

/** Network access is used only for link resolution; this flavor has no telemetry. */
object Analytics {
    fun init(context: Context) {}
    fun trackEvent(eventName: String, properties: Map<String, Any> = emptyMap()) {}
}
