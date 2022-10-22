package one.yufz.hmspush.common.content

import android.database.Cursor
import kotlin.reflect.KClass

fun Cursor.getValue(columnIndex: Int, type: KClass<*>): Any {
    return when (type) {
        String::class -> getString(columnIndex)
        Short::class -> getShort(columnIndex)
        Int::class -> getInt(columnIndex)
        Long::class -> getLong(columnIndex)
        Float::class -> getFloat(columnIndex)
        Double::class -> getDouble(columnIndex)
        Boolean::class -> getInt(columnIndex) == 1
        ByteArray::class -> getBlob(columnIndex)
        else -> throw IllegalStateException("Unsupported type: $type")
    }
}