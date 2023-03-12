@file:Suppress("UNCHECKED_CAST")

package one.yufz.xposed


import java.lang.reflect.Constructor
import java.lang.reflect.Method

private val libXposed: Boolean = false

val xposed: Xposed = if (libXposed) LibXposed() else LegacyXposed()

fun Class<*>.findMethodExact(methodName: String, vararg parameterTypes: Class<*>): Method =
    xposed.findMethodExact(this, methodName, parameterTypes as Array<Class<*>>)

fun Class<*>.findMethodsByExactParameters(returnType: Class<*>, vararg parameterTypes: Class<*>): Array<Method> =
    xposed.findMethodsByExactParameters(this, returnType, parameterTypes as Array<Class<*>>)

fun Class<*>.findConstructorExact(vararg parameterTypes: Any): Constructor<*> =
    xposed.findConstructorExact(this, parameterTypes as Array<Any>)

fun Any.callMethod(methodName: String, vararg args: Any): Any? =
    xposed.callMethod(this, methodName, args as Array<Any>)

fun Any.callMethod(methodName: String, parameterTypes: Array<Class<*>>, vararg args: Any?): Any? =
    xposed.callMethod(this, methodName, parameterTypes, args as Array<Any?>)

fun Class<*>.callStaticMethod(methodName: String, vararg args: Any): Any? =
    xposed.callStaticMethod(this, methodName, args as Array<Any>)

fun Class<*>.callStaticMethod(
    methodName: String,
    parameterTypes: Array<Class<*>>,
    vararg args: Any?
): Any? = xposed.callStaticMethod(this, methodName, parameterTypes, args as Array<Any?>)

typealias HookAction = MethodHookParam.() -> Unit
typealias ReplaceAction = MethodHookParam.() -> Any?
typealias HookCallback = HookContext.() -> Unit

fun Class<*>.hookMethod(methodName: String, vararg parameterTypes: Class<*>, callback: HookCallback): Unhook =
    xposed.hookMethod(this, methodName, arrayOf(*parameterTypes), callback)

fun Class<*>.hookConstructor(vararg parameterTypes: Class<*>, callback: HookCallback): Unhook =
    xposed.hookConstructor(this, parameterTypes as Array<Class<*>>, callback)

fun Class<*>.hookAllConstructor(callback: HookCallback): Set<Unhook> =
    xposed.hookAllConstructor(this, callback)

fun hookMethod(className: String, classLoader: ClassLoader, methodName: String, vararg parameterTypes: Class<*>, callback: HookCallback): Unhook =
    xposed.hookMethod(className, classLoader, methodName, parameterTypes as Array<Class<*>>, callback)

fun hookConstructor(className: String, classLoader: ClassLoader, methodName: String, vararg parameterTypes: Class<*>, callback: HookCallback): Unhook =
    xposed.hookConstructor(className, classLoader, methodName, parameterTypes as Array<Class<*>>, callback)

fun Method.hook(callback: HookCallback): Unhook =
    xposed.hookMethod(this, callback)

fun Class<*>.hookAllMethods(methodName: String, callback: HookCallback): Set<Unhook> =
    xposed.hookAllMethods(this, methodName, callback)


fun Class<*>.newInstance(vararg args: Any): Any =
    xposed.newInstance(this, args as Array<Any>)

fun Class<*>.newInstance(parameterTypes: Array<Class<*>>, vararg args: Any?): Any =
    xposed.newInstance(this, parameterTypes, args as Array<Any?>)

@Throws(ClassNotFoundException::class)
fun findClass(className: String): Class<*> =
    xposed.findClass(className, null)

@Throws(ClassNotFoundException::class)
fun ClassLoader.findClass(className: String): Class<*> =
    xposed.findClass(className, this)

inline fun <reified T> Any.getOrNull(name: String): T? = getField(name, T::class.java)

inline operator fun <reified T> Any.get(name: String): T = getField(name, T::class.java)!!

inline operator fun <reified T> Any.set(name: String, value: T?) = setField(name, value, T::class.java)

fun <T> Any.getField(name: String, fieldClazz: Class<T>): T? {
    val obj = if (this is Class<*>) null else this
    val thisClass = if (this is Class<*>) this else this.javaClass
    val field = findField(thisClass, name)

    val value = when (fieldClazz) {
        Boolean::class.java -> field.getBoolean(obj)
        Byte::class.java -> field.getByte(obj)
        Char::class.java -> field.getChar(obj)
        Double::class.java -> field.getDouble(obj)
        Float::class.java -> field.getFloat(obj)
        Int::class.java -> field.getInt(obj)
        Long::class.java -> field.getLong(obj)
        Short::class.java -> field.getShort(obj)
        else -> field.get(obj)
    }
    return value as? T?
}

fun <T> Any.setField(name: String, value: T?, fieldClass: Class<T>) {
    val obj = if (this is Class<*>) null else this
    val thisClass = if (this is Class<*>) this else this.javaClass

    val field = findField(thisClass, name)

    when (fieldClass) {
        Boolean::class.java -> field.setBoolean(obj, value as Boolean)
        Byte::class.java -> field.setByte(obj, value as Byte)
        Char::class.java -> field.setChar(obj, value as Char)
        Double::class.java -> field.setDouble(obj, value as Double)
        Float::class.java -> field.setFloat(obj, value as Float)
        Int::class.java -> field.setInt(obj, value as Int)
        Long::class.java -> field.setLong(obj, value as Long)
        Short::class.java -> field.setShort(obj, value as Short)
        else -> field.set(obj, value as T)
    }
}

private fun findField(clazz: Class<*>, fieldName: String) =
    xposed.findField(clazz, fieldName)

fun Any.setAdditionalInstanceField(name: String, value: Any?): Any? =
    xposed.setAdditionalInstanceField(this, name, value)

fun Any.getAdditionalInstanceField(name: String): Any? =
    xposed.getAdditionalInstanceField(this, name)

fun Any.removeAdditionalInstanceField(name: String): Any? =
    xposed.removeAdditionalInstanceField(this, name)

fun log(message: String) = xposed.log(message)