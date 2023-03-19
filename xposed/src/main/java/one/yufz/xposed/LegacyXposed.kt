package one.yufz.xposed

import android.app.Application
import android.content.pm.ApplicationInfo
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Member
import java.lang.reflect.Method

class LegacyXposed : Xposed {
    override fun currentApplication(): Application {
        return android.app.AndroidAppHelper.currentApplication()
    }

    override fun findMethodExact(clazz: Class<*>, methodName: String, parameterTypes: Array<Class<*>>): Method {
        return XposedHelpers.findMethodExact(clazz, methodName, *parameterTypes)
    }

    override fun findMethodsByExactParameters(clazz: Class<*>, returnType: Class<*>, parameterTypes: Array<Class<*>>): Array<Method> {
        return XposedHelpers.findMethodsByExactParameters(clazz, returnType, *parameterTypes)
    }

    override fun findConstructorExact(clazz: Class<*>, parameterTypes: Array<Any>): Constructor<*> {
        return XposedHelpers.findConstructorExact(clazz, *parameterTypes)
    }

    override fun callMethod(obj: Any, methodName: String, args: Array<Any>): Any? {
        try {
            return XposedHelpers.callMethod(obj, methodName, *args)
        } catch (e: XposedHelpers.InvocationTargetError) {
            throw InvocationTargetException(e)
        }
    }

    override fun callMethod(obj: Any, methodName: String, parameterTypes: Array<Class<*>>, args: Array<Any?>): Any? {
        return XposedHelpers.callMethod(obj, methodName, parameterTypes, *args)
    }

    override fun callStaticMethod(clazz: Class<*>, methodName: String, args: Array<Any>): Any? {
        return XposedHelpers.callStaticMethod(clazz, methodName, *args)
    }

    override fun callStaticMethod(clazz: Class<*>, methodName: String, parameterTypes: Array<Class<*>>, args: Array<Any?>): Any? {
        return XposedHelpers.callStaticMethod(clazz, methodName, parameterTypes, *args)
    }

    override fun hookConstructor(clazz: Class<*>, parameterTypes: Array<Class<*>>, callback: HookCallback): Unhook {
        return hookWithContext(callback) { XposedHelpers.findAndHookConstructor(clazz, parameterTypes, it.getMethodHook()) }
    }

    override fun hookConstructor(className: String, classLoader: ClassLoader, methodName: String, parameterTypes: Array<Class<*>>, callback: HookCallback): Unhook {
        return hookWithContext(callback) { XposedHelpers.findAndHookConstructor(className, classLoader, parameterTypes, it.getMethodHook()) }
    }

    override fun hookAllConstructor(clazz: Class<*>, callback: HookCallback): Set<Unhook> {
        return hookAllWithContext(callback) { XposedBridge.hookAllConstructors(clazz, it.getMethodHook()) }
    }

    override fun hookMethod(method: Method, callback: HookCallback): Unhook {
        return hookWithContext(callback) { XposedBridge.hookMethod(method, it.getMethodHook()) }
    }

    override fun hookMethod(clazz: Class<*>, methodName: String, parameterTypes: Array<Class<*>>, callback: HookCallback): Unhook {
        return hookWithContext(callback) { XposedHelpers.findAndHookMethod(clazz, methodName, *parameterTypes, it.getMethodHook()) }
    }

    override fun hookMethod(className: String, classLoader: ClassLoader, methodName: String, parameterTypes: Array<Class<*>>, callback: HookCallback): Unhook {
        return hookWithContext(callback) { XposedHelpers.findAndHookMethod(className, classLoader, methodName, *parameterTypes, it.getMethodHook()) }
    }

    override fun hookAllMethods(clazz: Class<*>, methodName: String, callback: HookCallback): Set<Unhook> {
        return hookAllWithContext(callback) { XposedBridge.hookAllMethods(clazz, methodName, it.getMethodHook()) }
    }

    override fun newInstance(clazz: Class<*>, args: Array<Any>): Any {
        return XposedHelpers.newInstance(clazz, *args)
    }

    override fun newInstance(clazz: Class<*>, parameterTypes: Array<Class<*>>, args: Array<Any?>): Any {
        return XposedHelpers.newInstance(clazz, parameterTypes, *args)
    }

    override fun findClass(className: String, classLoader: ClassLoader?): Class<*> {
        try {
            return XposedHelpers.findClass(className, classLoader)
        } catch (e: XposedHelpers.ClassNotFoundError) {
            throw ClassNotFoundException(e.message, e.cause)
        }
    }

    override fun findField(clazz: Class<*>, fieldName: String): Field {
        return XposedHelpers.findField(clazz, fieldName)
    }

    override fun setAdditionalInstanceField(obj: Any, name: String, value: Any?): Any? {
        return XposedHelpers.setAdditionalInstanceField(obj, name, value)
    }

    override fun getAdditionalInstanceField(obj: Any, name: String): Any? {
        return XposedHelpers.getAdditionalInstanceField(obj, name)
    }

    override fun removeAdditionalInstanceField(obj: Any, name: String): Any? {
        return XposedHelpers.removeAdditionalInstanceField(obj, name)
    }

    override fun log(message: String) {
        XposedBridge.log(message)
    }
}

private inline fun hookWithContext(callback: HookCallback, hookAction: (HookContext) -> XC_MethodHook.Unhook): Unhook {
    val context = HookContext()
    callback.invoke(context)
    val unhook = hookAction(context).toUnhook()
    context.unhook = unhook
    return unhook
}

private inline fun hookAllWithContext(callback: HookCallback, hookAction: (HookContext) -> Set<XC_MethodHook.Unhook>): Set<Unhook> {
    val context = HookContext()
    callback.invoke(context)
    val unhooks = hookAction(context)
    context.unhook = object : Unhook {
        override fun unhook() {
            unhooks.forEach { it.unhook() }
        }
    }
    return unhooks.map { it.toUnhook() }.toSet()
}

fun HookContext.getMethodHook() = MethodHook(this)

class LegacyUnhook(val origin: XC_MethodHook.Unhook) : Unhook {
    override fun unhook() = origin.unhook()
}

fun XC_MethodHook.Unhook.toUnhook(): Unhook = LegacyUnhook(this)
fun XC_MethodHook.MethodHookParam.toMethodHookParam() = LegacyMethodHookParam(this)

class LegacyMethodHookParam(val origin: XC_MethodHook.MethodHookParam) : MethodHookParam {
    override val args: Array<Any?>
        get() = origin.args
    override val thisObject: Any
        get() = origin.thisObject
    override val method: Member
        get() = origin.method
    override var result: Any?
        get() = origin.result
        set(value) {
            origin.result = value
        }
    override var throwable: Throwable?
        get() = origin.throwable
        set(value) {
            origin.throwable = value
        }
}

class MethodHook(private val context: HookContext) : XC_MethodHook() {
    override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
        super.beforeHookedMethod(param)

        context.replaceAction?.let {
            try {
                param.result = it.invoke(param.toMethodHookParam())
            } catch (t: Throwable) {
                param.throwable = t
            }
            return
        }

        context.beforeAction?.invoke(param.toMethodHookParam())
    }

    override fun afterHookedMethod(param: MethodHookParam) {
        super.afterHookedMethod(param)
        context.afterAction?.invoke(param.toMethodHookParam())
    }

}

fun XC_LoadPackage.LoadPackageParam.toLegacyLoadPackageParam(): LoadPackageParam = LegacyLoadPackageParam(this)

class LegacyLoadPackageParam(val origin: XC_LoadPackage.LoadPackageParam) : LoadPackageParam {
    override val packageName: String
        get() = origin.packageName
    override val processName: String
        get() = origin.processName
    override val appInfo: ApplicationInfo
        get() = origin.appInfo
    override val classLoader: ClassLoader
        get() = origin.classLoader
    override val isFirstPackage: Boolean
        //todo same?
        get() = origin.isFirstApplication

}