package one.yufz.hmspush

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.huawei.android.app.NotificationManagerEx
import dalvik.system.DexClassLoader
import de.robv.android.xposed.callbacks.XC_LoadPackage
import one.yufz.hmspush.settings.HookSettings

class HookHMS {
    companion object {
        private const val TAG = "HookHMS"
    }

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        //android.app.PendingIntent.getActivity(android.content.Context, int, android.content.Intent, int)
        PendingIntent::class.java.hookMethod("getActivity", Context::class.java, Int::class.java, Intent::class.java, Int::class.java) {
            doBefore {
                val intent = args[2] as Intent
                if (intent.component?.className == "com.huawei.hms.runtimekit.stubexplicit.PushEarthquakeActivity") {
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                }
            }
        }

        DexClassLoader::class.java.hookAllConstructor {
            doAfter {
                val dexPath = args[0] as String
                if (dexPath.contains("push")) {
                    XLog.d(TAG, "load push related dex path: $dexPath")

                    val paths = dexPath.split("/")
                    val version = paths.getOrNull(paths.size - 2)?.toIntOrNull() ?: 0

                    XLog.d(TAG, "load push version: $version")

                    val classLoader = thisObject as ClassLoader

                    if (dexPath.endsWith(HMS_PUSH_NC)) {
                        if (version >= 60600300) {
                            HookPushNC.hook(classLoader)
                        } else {
                            hookLegacyPush(classLoader)
                        }
                    } else if (version <= 60300301) {
                        hookLegacyPush(classLoader)
                    }
                }
            }
        }

        HookSettings().hook(lpparam.classLoader)
    }


    private fun hookLegacyPush(classLoader: ClassLoader) {
        XLog.d(TAG, "hookLegacyPush() called with: classLoader = $classLoader")

        PushSignWatcher.watch()

        Class::class.java.hookMethod("forName", String::class.java, Boolean::class.java, ClassLoader::class.java) {
            doBefore {
                if (args[0] == NotificationManagerEx::class.java.name) {
                    result = NotificationManagerEx::class.java
                }
            }
        }
    }
}