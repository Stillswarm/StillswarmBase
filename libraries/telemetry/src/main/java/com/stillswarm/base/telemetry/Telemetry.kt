package com.stillswarm.base.telemetry

sealed interface TelemetryValue {
    data class Text(val value: String) : TelemetryValue {
        init {
            require(value.length <= 100) { "Telemetry text must be at most 100 characters" }
        }
    }

    data class Integer(val value: Long) : TelemetryValue

    data class Decimal(val value: Double) : TelemetryValue {
        init {
            require(value.isFinite())
        }
    }

    data class Flag(val value: Boolean) : TelemetryValue
}

/** Use stable app-owned names and categorical values; never pass free-form user content. */
class TelemetryEvent(name: String, parameters: Map<String, TelemetryValue> = emptyMap()) {
    val name = name.also(::validateTelemetryName)
    val parameters = parameters.toMap()

    init {
        require(name != "screen_view") { "Use screen() for screen views" }
        require(parameters.size <= 25) { "At most 25 parameters per event" }
        parameters.keys.forEach(::validateTelemetryName)
    }
}

internal fun validateTelemetryName(name: String) {
    require(name.matches(Regex("[A-Za-z][A-Za-z0-9_]{0,39}"))) { "Invalid telemetry name: $name" }
    require(listOf("firebase_", "google_", "ga_").none(name::startsWith)) {
        "Reserved telemetry prefix"
    }
}

interface Analytics {
    val analyticsEnabled: Boolean

    fun setAnalyticsEnabled(enabled: Boolean)

    fun track(event: TelemetryEvent)

    /** Explicit screen impression; call on entry, not every recomposition. */
    fun screen(name: String)
}

interface CrashReporter {
    val crashReportingEnabled: Boolean

    fun setCrashReportingEnabled(enabled: Boolean)

    fun breadcrumb(message: String)

    fun recordException(error: Throwable)
}

interface Telemetry : Analytics, CrashReporter

/** Safe default for previews or apps that do not collect telemetry. */
object NoOpTelemetry : Telemetry {
    override val analyticsEnabled = false
    override val crashReportingEnabled = false

    override fun setAnalyticsEnabled(enabled: Boolean) = Unit

    override fun setCrashReportingEnabled(enabled: Boolean) = Unit

    override fun track(event: TelemetryEvent) = Unit

    override fun screen(name: String) = Unit

    override fun breadcrumb(message: String) = Unit

    override fun recordException(error: Throwable) = Unit
}
