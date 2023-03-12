package one.yufz.hmspush.hook.hms

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.CursorWindow
import android.os.Build
import com.huawei.android.app.NotificationManagerEx
import dalvik.system.DexClassLoader
import one.yufz.hmspush.hook.XLog
import one.yufz.hmspush.hook.bridge.HookContentProvider
import one.yufz.hmspush.hook.hms.dummy.HookDummyActivity
import one.yufz.xposed.*

class HookHMS {
    companion object {
        private const val TAG = "HookHMS"
    }

    fun hook(classLoader: ClassLoader) {
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
                if (dexPath.contains("com.huawei.hms.push")) {
                    XLog.d(TAG, "load push related dex path: $dexPath")

                    val paths = dexPath.split("/")
                    val version = paths.getOrNull(paths.size - 2)?.toIntOrNull() ?: 0

                    XLog.d(TAG, "load push version: $version")

                    val classLoader = thisObject as ClassLoader

                    if (HookPushNC.canHook(classLoader)) {
                        HookPushNC.hook(classLoader)
                    } else {
                        hookLegacyPush(classLoader)
                    }
                } else if (dexPath.contains("com.huawei.hms.runtimekit")) {
                    RuntimeKitHook.hook(thisObject as ClassLoader)
                    HookLegacyTokenRequest.hook(thisObject as ClassLoader)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= 33) {
            CursorWindow::class.java["sCursorWindowSize"] = 1024 * 1024 * 8
        }

        HookContentProvider().hook(classLoader)
        fakeFingerprint(classLoader)

        HookForegroundService.hook(classLoader)

        HookDummyActivity.hook(classLoader)
    }

    private fun hookLegacyPush(classLoader: ClassLoader) {
        XLog.d(TAG, "hookLegacyPush() called with: classLoader = $classLoader")

        try {
            classLoader.findClass("com.huawei.hms.pushnc.entity.PushSelfShowMessage")
        } catch (e: ClassNotFoundException) {
            XLog.d(TAG, "PushSelfShowMessage not Found, stop hook")
            return
        }

        PushSignWatcher.watch()

        Class::class.java.hookMethod("forName", String::class.java, Boolean::class.java, ClassLoader::class.java) {
            doBefore {
                if (args[0] == NotificationManagerEx::class.java.name) {
                    result = NotificationManagerEx::class.java
                }
            }
        }
    }

    private fun fakeFingerprint(classLoader: ClassLoader) {
        classLoader.findClass("com.huawei.hms.auth.api.CheckFingerprintRequest")
            .hookMethod("parseEntity", String::class.java) {
                doBefore {
                    if (Prefs.isDisableSignature()) {
                        val request = args[0] as String
                        if (request.contains("auth.checkFingerprint")) {
                            val response = """{"header":{"auth_rtnCode":"0"},"body":{}}"""
                            thisObject.callMethod("call", response)
                            result = null
                        }
                    }
                }
            }
    }
}