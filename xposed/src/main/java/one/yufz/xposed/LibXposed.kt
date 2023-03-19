package one.yufz.xposed

import android.app.Application
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

class LibXposed : Xposed {
    override fun currentApplication(): Application {
        TODO("Not yet implemented")
    }

    override fun findMethodExact(clazz: Class<*>, methodName: String, parameterTypes: Array<Class<*>>): Method {
        TODO("Not yet implemented")
    }

    override fun findMethodsByExactParameters(clazz: Class<*>, returnType: Class<*>, parameterTypes: Array<Class<*>>): Array<Method> {
        TODO("Not yet implemented")
    }

    override fun findConstructorExact(clazz: Class<*>, parameterTypes: Array<Any>): Constructor<*> {
        TODO("Not yet implemented")
    }

    override fun callMethod(obj: Any, methodName: String, args: Array<Any>): Any? {
        TODO("Not yet implemented")
    }

    override fun callMethod(obj: Any, methodName: String, parameterTypes: Array<Class<*>>, args: Array<Any?>): Any? {
        TODO("Not yet implemented")
    }

    override fun callStaticMethod(clazz: Class<*>, methodName: String, args: Array<Any>): Any? {
        TODO("Not yet implemented")
    }

    override fun callStaticMethod(clazz: Class<*>, methodName: String, parameterTypes: Array<Class<*>>, args: Array<Any?>): Any? {
        TODO("Not yet implemented")
    }

    override fun hookConstructor(clazz: Class<*>, parameterTypes: Array<Class<*>>, callback: HookCallback): Unhook {
        TODO("Not yet implemented")
    }

    override fun hookConstructor(className: String, classLoader: ClassLoader, methodName: String, parameterTypes: Array<Class<*>>, callback: HookCallback): Unhook {
        TODO("Not yet implemented")
    }

    override fun hookAllConstructor(clazz: Class<*>, callback: HookCallback): Set<Unhook> {
        TODO("Not yet implemented")
    }

    override fun hookMethod(method: Method, callback: HookCallback): Unhook {
        TODO("Not yet implemented")
    }

    override fun hookMethod(clazz: Class<*>, methodName: String, parameterTypes: Array<Class<*>>, callback: HookCallback): Unhook {
        TODO("Not yet implemented")
    }

    override fun hookMethod(className: String, classLoader: ClassLoader, methodName: String, parameterTypes: Array<Class<*>>, callback: HookCallback): Unhook {
        TODO("Not yet implemented")
    }

    override fun hookAllMethods(clazz: Class<*>, methodName: String, callback: HookCallback): Set<Unhook> {
        TODO("Not yet implemented")
    }

    override fun newInstance(clazz: Class<*>, args: Array<Any>): Any {
        TODO("Not yet implemented")
    }

    override fun newInstance(clazz: Class<*>, parameterTypes: Array<Class<*>>, args: Array<Any?>): Any {
        TODO("Not yet implemented")
    }

    override fun findClass(className: String, classLoader: ClassLoader?): Class<*> {
        TODO("Not yet implemented")
    }

    override fun findField(clazz: Class<*>, fieldName: String): Field {
        TODO("Not yet implemented")
    }

    override fun setAdditionalInstanceField(obj: Any, name: String, value: Any?): Any? {
        TODO("Not yet implemented")
    }

    override fun getAdditionalInstanceField(obj: Any, name: String): Any? {
        TODO("Not yet implemented")
    }

    override fun removeAdditionalInstanceField(obj: Any, name: String): Any? {
        TODO("Not yet implemented")
    }

    override fun log(message: String) {
        TODO("Not yet implemented")
    }
}