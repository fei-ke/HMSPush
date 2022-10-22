package one.yufz.hmspush.common.content

import android.content.ContentValues
import android.content.SharedPreferences
import android.database.Cursor
import android.database.MatrixCursor
import kotlin.reflect.KClass

private val CACHE = HashMap<KClass<out ContentModel>, ContentProperties<ContentModel>>()

fun KClass<out ContentModel>.getContentProperties(): ContentProperties<ContentModel> {
    @Suppress("UNCHECKED_CAST") return CACHE.getOrPut(this) { java.getField("PROPERTIES").get(null) as ContentProperties<ContentModel> }
}

fun ContentModel.getContentProperties(): ContentProperties<ContentModel> {
    return this::class.getContentProperties()
}

fun ContentModel.toContentValues(): ContentValues {
    val values = ContentValues()

    getContentProperties().properties.forEach { (name, prop) ->
        values.putValue(name, prop.type, prop.get(this))
    }

    return values
}

inline fun <reified T : ContentModel> ContentValues.toContent() = toContent(T::class)

fun <T : ContentModel> ContentValues.toContent(type: KClass<T>): T {
    val model = type.java.newInstance()

    type.getContentProperties().properties.forEach { (name, prop) ->
        prop.set(model, get(name))
    }
    return model
}

inline fun <reified T : ContentModel> Cursor.toContent() = toContent(T::class)

fun <T : ContentModel> Cursor.toContent(type: KClass<T>): T {
    val model = type.java.newInstance()

    if (position == -1) {
        if (!moveToNext()) {
            return model
        }
    }

    type.getContentProperties().properties.forEach { (name, prop) ->
        val columnIndex = getColumnIndex(name)

        if (columnIndex == -1) return@forEach
        if (isNull(columnIndex)) return@forEach
        prop.set(model, getValue(columnIndex, prop.type))
    }
    return model
}

fun ContentModel.toCursor(cursor: MatrixCursor = MatrixCursor(getContentProperties().properties.keys.toTypedArray())): Cursor {
    val newRow = cursor.newRow()
    getContentProperties().properties.forEach { (name, prop) ->
        val value = prop.get(this)

        if (value is Boolean) {
            newRow.add(name, if (value) 1 else 0)
        } else {
            newRow.add(name, value)
        }
    }
    return cursor
}

inline fun <reified T : ContentModel> Cursor.toContentList() = inflateCollection(ArrayList(), T::class)

inline fun <reified T : ContentModel> Cursor.toContentSet() = inflateCollection(HashSet(), T::class)

fun <T : ContentModel, C : MutableCollection<T>> Cursor.inflateCollection(collection: C, type: KClass<T>): C {
    while (moveToNext()) {
        collection.add(toContent(type))
    }
    return collection
}

inline fun <reified T : ContentModel> Iterable<T>.toCursor() = toCursor(T::class)

fun <T : ContentModel> Iterable<T>.toCursor(type: KClass<T>): Cursor {
    val cursor = MatrixCursor(type.getContentProperties().properties.keys.toTypedArray())
    forEach { it.toCursor(cursor) }
    return cursor
}

inline fun <reified T : ContentModel> SharedPreferences.toContent(): T {
    return toContent(T::class)
}

fun <T : ContentModel> SharedPreferences.toContent(type: KClass<T>): T {
    val model = type.java.newInstance()

    type.getContentProperties().properties.forEach { (name, prop) ->
        prop.set(model, getValue(name, prop.type))
    }

    return model
}

fun ContentModel.storeToSharedPreference(editor: SharedPreferences.Editor) {
    getContentProperties().properties.forEach { (name, prop) ->
        editor.putValue(name, prop.type, prop.get(this))
    }
}