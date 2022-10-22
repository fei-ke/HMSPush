package one.yufz.hmspush.common.content

import android.content.ContentValues
import kotlin.reflect.KClass

fun ContentValues.putValue(key: String?, type: KClass<*>, value: Any?) {
    when (type) {
        String::class -> put(key, value as String?)
        Byte::class -> put(key, value as Byte?)
        Short::class -> put(key, value as Short?)
        Int::class -> put(key, value as Int?)
        Long::class -> put(key, value as Long?)
        Float::class -> put(key, value as Float?)
        Double::class -> put(key, value as Double?)
        Boolean::class -> put(key, value as Boolean?)
        ByteArray::class -> put(key, value as ByteArray?)
        else -> throw IllegalStateException("Unsupported type: $type")
    }
}