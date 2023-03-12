package one.yufz.xposed

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import dalvik.system.BaseDexClassLoader
import one.yufz.hmspush.common.doOnce

private const val TAG = "AndroidHookUtils"

fun onApplicationAttachContext(callback: Application.() -> Unit) {
    ContextWrapper::class.java.hookMethod("attachBaseContext", Context::class.java) {
        doAfter {
            if (thisObject is Application) {
                unhook()
                callback(thisObject as Application)
            }

        }
    }
}

fun onDexClassLoaderLoaded(callback: ClassLoader.(unhook: () -> Unit) -> Unit) {
    var unhooks: Set<Unhook>? = null

    unhooks = BaseDexClassLoader::class.java.hookAllConstructor {
        doAfter {
            val hookContext = this@hookAllConstructor

            hookContext.doOnce(thisObject) {
                callback(thisObject as ClassLoader) {
                    unhooks?.forEach { it.unhook() }
                }
            }
        }
    }
}