package one.yufz.hmspush.common.content

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1

open class ContentProperties<M>(val properties: Map<String, Property<M>>) {
    class Property<M>(val type: KClass<*>, val nullable: Boolean, private val property: KMutableProperty1<M, Any?>) {
        fun set(receiver: M, value: Any?) {
            if (value != null) {
                property.set(receiver, value)
            } else if (nullable) {
                property.set(receiver, null)
            }
        }

        inline fun <reified T> getValue(receiver: M): T? {
            return get(receiver) as T?
        }

        fun get(receiver: M): Any? {
            val value = property.get(receiver)
            if (!nullable && value == null) {
                throw IllegalStateException("Property [$property] is NonNull, but got null")
            }
            return value
        }
    }

    class Builder<M> {
        private val properties: HashMap<String, Property<M>> = LinkedHashMap()

        inline fun <reified T> property(name: String, property: KMutableProperty1<M, T>): Builder<M> {
            return property(name, T::class, false, property)
        }

        inline fun <reified T> nullableProperty(name: String, property: KMutableProperty1<M, T>): Builder<M> {
            return property(name, T::class, true, property)
        }

        fun property(name: String, type: KClass<*>, nullable: Boolean, property: KMutableProperty1<M, *>): Builder<M> {
            @Suppress("UNCHECKED_CAST")
            properties[name] = Property(type, nullable, property as KMutableProperty1<M, Any?>)
            return this
        }

        fun build(): ContentProperties<M> {
            return ContentProperties(properties)
        }
    }
}