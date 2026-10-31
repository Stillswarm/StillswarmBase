package com.stillswarm.base.config

/**
 * Declare each key once and register it with the session. Validators must be pure and non-throwing.
 */
class ConfigKey<T : Any>
private constructor(
    val name: String,
    val defaultValue: T,
    private val decode: (String) -> T?,
    private val encode: (T) -> String,
    private val validate: (T) -> Boolean,
) {
    init {
        require(name.matches(Regex("[A-Za-z][A-Za-z0-9_.-]*"))) { "Invalid config key name" }
        require(validate(defaultValue)) { "Invalid default for $name" }
    }

    internal fun parse(raw: String): T? = decode(raw)?.takeIf(validate)

    internal fun accepts(raw: String): Boolean = parse(raw) != null

    internal fun serialize(value: T): String {
        require(validate(value)) { "Invalid value for $name" }
        return encode(value)
    }

    companion object {
        fun boolean(name: String, defaultValue: Boolean) =
            ConfigKey(
                name = name,
                defaultValue = defaultValue,
                decode = { it.toBooleanStrictOrNull() },
                encode = { it.toString() },
                validate = { true },
            )

        fun string(name: String, defaultValue: String, validate: (String) -> Boolean = { true }) =
            ConfigKey(
                name = name,
                defaultValue = defaultValue,
                decode = { it },
                encode = { it },
                validate = validate,
            )

        fun int(name: String, defaultValue: Int, validate: (Int) -> Boolean = { true }) =
            ConfigKey(
                name = name,
                defaultValue = defaultValue,
                decode = { it.toIntOrNull() },
                encode = { it.toString() },
                validate = validate,
            )

        fun long(name: String, defaultValue: Long, validate: (Long) -> Boolean = { true }) =
            ConfigKey(
                name = name,
                defaultValue = defaultValue,
                decode = { it.toLongOrNull() },
                encode = { it.toString() },
                validate = validate,
            )

        fun double(name: String, defaultValue: Double, validate: (Double) -> Boolean = { true }) =
            ConfigKey(
                name = name,
                defaultValue = defaultValue,
                decode = { it.toDoubleOrNull() },
                encode = { it.toString() },
                validate = { it.isFinite() && validate(it) },
            )
    }
}
