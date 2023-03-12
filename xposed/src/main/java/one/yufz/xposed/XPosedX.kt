package one.yufz.xposed


import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.InvocationTargetError
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

fun Class<*>.findMethodExact(methodName: String, vararg parameterTypes: Class<*>): Method =
    XposedHelpers.findMethodExact(this, methodName, *parameterTypes)

fun Class<*>.findMethodsByExactParameters(returnType: Class<*>, vararg parameterTypes: Class<*>): Array<Method> =
    XposedHelpers.findMethodsByExactParameters(this, returnType, *parameterTypes)

fun Class<*>.findConstructorExact(vararg parameterTypes: Any?): Constructor<*> =
    XposedHelpers.findConstructorExact(this, *parameterTypes)

fun Any.callMethod(methodName: String, vararg args: Any): Any? =
    try {
        XposedHelpers.callMethod(this, methodName, *args)
    } catch (e: InvocationTargetError) {
        throw InvocationTargetException(e)
    }

fun Any.callMethod(methodName: String, parameterTypes: Array<Class<*>>, vararg args: Any): Any? =
    XposedHelpers.callMethod(this, methodName, parameterTypes, *args)

fun Class<*>.callStaticMethod(methodName: String, vararg args: Any): Any? =
    XposedHelpers.callStaticMethod(this, methodName, *args)

fun Class<*>.callStaticMethod(
    methodName: String,
    parameterTypes: Array<Class<*>>,
    vararg args: Any
): Any? = XposedHelpers.callStaticMethod(this, methodName, parameterTypes, *args)

typealias HookAction = XC_MethodHook.MethodHookParam.() -> Unit
typealias ReplaceAction = XC_MethodHook.MethodHookParam.() -> Any?
typealias HookCallback = HookContext.() -> Unit

fun Class<*>.hookMethod(methodName: String, vararg parameterTypes: Class<*>, callback: HookCallback) =
    XposedHelpers.findAndHookMethod(this, methodName, *parameterTypes, MethodHook(callback))

fun Class<*>.hookConstructor(vararg parameterTypes: Class<*>, callback: HookCallback) =
    XposedHelpers.findAndHookConstructor(this, *parameterTypes, MethodHook(callback))

fun Class<*>.hookAllConstructor(callback: HookCallback) =
    XposedBridge.hookAllConstructors(this, MethodHook(callback))

fun hookMethod(className: String, classLoader: ClassLoader, methodName: String, vararg parameterTypes: Class<*>, callback: HookCallback) =
    XposedHelpers.findAndHookMethod(className, classLoader, methodName, *parameterTypes, MethodHook(callback))

fun hookConstructor(className: String, classLoader: ClassLoader, methodName: String, vararg parameterTypes: Class<*>, callback: HookCallback) =
    XposedHelpers.findAndHookConstructor(className, classLoader, methodName, *parameterTypes, MethodHook(callback))

fun Method.hook(callback: HookCallback) = XposedBridge.hookMethod(this, MethodHook(callback))

fun Class<*>.hookAllMethods(methodName: String, callback: HookCallback) =
    XposedBridge.hookAllMethods(this, methodName, MethodHook(callback))

class MethodHook(callback: HookCallback) : XC_MethodHook() {
    private val context = HookContext(this).apply(callback)

    override fun beforeHookedMethod(param: MethodHookParam) {
        super.beforeHookedMethod(param)

        context.replaceAction?.let {
            try {
                param.result = it.invoke(param)
            } catch (t: Throwable) {
                param.throwable = t
            }
            return
        }

        context.beforeAction?.invoke(param)
    }

    override fun afterHookedMethod(param: MethodHookParam) {
        super.afterHookedMethod(param)
        context.afterAction?.invoke(param)
    }

}

class HookContext(private val methodHook: MethodHook) {
    internal var beforeAction: HookAction? = null
        private set

    internal var afterAction: HookAction? = null
        private set

    internal var replaceAction: ReplaceAction? = null
        private set

    fun doBefore(action: HookAction) {
        this.beforeAction = action
    }

    fun doAfter(action: HookAction) {
        this.afterAction = action
    }

    fun replace(action: ReplaceAction) {
        this.replaceAction = action
    }

    fun XC_MethodHook.MethodHookParam.unhook() {
        XposedBridge.unhookMethod(this.method, methodHook)
    }
}

fun Class<*>.newInstance(vararg args: Any): Any = XposedHelpers.newInstance(this, *args)

fun Class<*>.newInstance(parameterTypes: Array<Class<*>>, vararg args: Any): Any =
    XposedHelpers.newInstance(this, parameterTypes, *args)

@Throws(ClassNotFoundException::class)
fun findClass(className: String): Class<*> = try {
    XposedHelpers.findClass(className, null)
} catch (e: XposedHelpers.ClassNotFoundError) {
    throw ClassNotFoundException(e.message, e.cause)
}

@Throws(ClassNotFoundException::class)
fun ClassLoader.findClass(className: String): Class<*> = try {
    XposedHelpers.findClass(className, this)
} catch (e: XposedHelpers.ClassNotFoundError) {
    throw ClassNotFoundException(e.message, e.cause)
}

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

private fun findField(clazz: Class<*>, fieldName: String) = XposedHelpers.findField(clazz, fieldName)

fun Any.setAdditionalInstanceField(name: String, value: Any?): Any? =
    XposedHelpers.setAdditionalInstanceField(this, name, value)

fun Any.getAdditionalInstanceField(name: String): Any? =
    XposedHelpers.getAdditionalInstanceField(this, name)

fun Any.removeAdditionalInstanceField(name: String): Any? =
    XposedHelpers.removeAdditionalInstanceField(this, name)

fun log(message: String) = XposedBridge.log(message)