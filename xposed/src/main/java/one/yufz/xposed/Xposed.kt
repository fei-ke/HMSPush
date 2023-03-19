package one.yufz.xposed

import android.app.Application
import android.content.pm.ApplicationInfo
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method

interface LoadPackageParam {
    val packageName: String

    val processName: String

    val appInfo: ApplicationInfo

    val classLoader: ClassLoader

    val isFirstPackage: Boolean
}

interface Unhook {
    fun unhook()
}

interface MethodHookParam {
    val args: Array<Any?>

    val thisObject: Any

    val method: Member

    var result: Any?

    var throwable: Throwable?

    fun hasThrowable(): Boolean = throwable != null
}

class HookContext {
    internal var beforeAction: HookAction? = null
        private set

    internal var afterAction: HookAction? = null
        private set

    internal var replaceAction: ReplaceAction? = null
        private set

    internal var unhook: Unhook? = null

    fun doBefore(action: HookAction) {
        this.beforeAction = action
    }

    fun doAfter(action: HookAction) {
        this.afterAction = action
    }

    fun replace(action: ReplaceAction) {
        this.replaceAction = action
    }

    fun unhook() {
        unhook?.unhook()
    }
}

interface Xposed {
    fun currentApplication(): Application
    fun findMethodExact(clazz: Class<*>, methodName: String, parameterTypes: Array<Class<*>>): Method
    fun findMethodsByExactParameters(clazz: Class<*>, returnType: Class<*>, parameterTypes: Array<Class<*>>): Array<Method>
    fun findConstructorExact(clazz: Class<*>, parameterTypes: Array<Any>): Constructor<*>
    fun callMethod(obj: Any, methodName: String, args: Array<Any>): Any?
    fun callMethod(obj: Any, methodName: String, parameterTypes: Array<Class<*>>, args: Array<Any?>): Any?
    fun callStaticMethod(clazz: Class<*>, methodName: String, args: Array<Any>): Any?
    fun callStaticMethod(clazz: Class<*>, methodName: String, parameterTypes: Array<Class<*>>, args: Array<Any?>): Any?
    fun hookConstructor(clazz: Class<*>, parameterTypes: Array<Class<*>>, callback: HookCallback): Unhook
    fun hookConstructor(className: String, classLoader: ClassLoader, methodName: String, parameterTypes: Array<Class<*>>, callback: HookCallback): Unhook
    fun hookAllConstructor(clazz: Class<*>, callback: HookCallback): Set<Unhook>

    fun hookMethod(method: Method, callback: HookCallback): Unhook
    fun hookMethod(clazz: Class<*>, methodName: String, parameterTypes: Array<Class<*>>, callback: HookCallback): Unhook
    fun hookMethod(className: String, classLoader: ClassLoader, methodName: String, parameterTypes: Array<Class<*>>, callback: HookCallback): Unhook
    fun hookAllMethods(clazz: Class<*>, methodName: String, callback: HookCallback): Set<Unhook>

    fun newInstance(clazz: Class<*>, args: Array<Any>): Any
    fun newInstance(clazz: Class<*>, parameterTypes: Array<Class<*>>, args: Array<Any?>): Any

    fun findClass(className: String, classLoader: ClassLoader?): Class<*>
    fun findField(clazz: Class<*>, fieldName: String): Field

    fun setAdditionalInstanceField(obj: Any, name: String, value: Any?): Any?
    fun getAdditionalInstanceField(obj: Any, name: String): Any?
    fun removeAdditionalInstanceField(obj: Any, name: String): Any?

    fun log(message: String)
}