package one.yufz.hmspush.common.content

import android.content.SharedPreferences
import kotlin.reflect.KClass

fun SharedPreferences.getValue(key: String, type: KClass<*>): Any? {
    if (!contains(key)) return null

    return when (type) {
        String::class -> getString(key, null)
        Short::class -> getInt(key, 0).toShort()
        Int::class -> getInt(key, 0)
        Long::class -> getLong(key, 0L)
        Float::class -> getFloat(key, 0F)
        Boolean::class -> getBoolean(key, false)
        else -> throw IllegalStateException("Unsupported type: $type")
    }
}

fun SharedPreferences.Editor.putValue(key: String, type: KClass<*>, value: Any?): SharedPreferences.Editor {
    when (type) {
        String::class -> putString(key, value as String?)
        Short::class -> putInt(key, value as Int)
        Int::class -> putInt(key, value as Int)
        Long::class -> putLong(key, value as Long)
        Float::class -> putFloat(key, value as Float)
        Boolean::class -> putBoolean(key, value as Boolean)
        else -> throw IllegalStateException("Unsupported type: $type")
    }
    return this
}