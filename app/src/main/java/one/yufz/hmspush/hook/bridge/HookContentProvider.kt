package one.yufz.hmspush.hook.bridge

import android.app.AndroidAppHelper
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import one.yufz.hmspush.BuildConfig
import one.yufz.xposed.findClass
import one.yufz.xposed.hookMethod

class HookContentProvider {
    fun hook(classLoader: ClassLoader) {
        val classModuleQueryProvider = classLoader.findClass("com.huawei.hms.dynamic.module.manager.query.ModuleQueryProvider")

        val bridge = BridgeContentProvider()
        //    public abstract @Nullable Cursor query(@NonNull Uri uri, @Nullable String[] projection,
        //            @Nullable String selection, @Nullable String[] selectionArgs,
        //            @Nullable String sortOrder);
        classModuleQueryProvider.hookMethod("query", Uri::class.java, Array<String>::class.java, String::class.java, Array<String>::class.java, String::class.java) {
            doBefore {
                if (fromHmsPush()) {
                    result = bridge.query(args[0] as Uri, args[1] as Array<String>?, args[2] as String?, args[3] as Array<String>?, args[4] as String?)
                }
            }
        }

        //public @Nullable Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras)
        classModuleQueryProvider.hookMethod("call", String::class.java, String::class.java, Bundle::class.java) {
            doBefore {
                if (fromHmsPush()) {
                    result = bridge.call(args[0] as String, args[1] as String?, args[2] as Bundle?)
                }
            }
        }
    }

    private fun fromHmsPush() = try {
        val callingUid = Binder.getCallingUid()
        callingUid == AndroidAppHelper.currentApplication().packageManager.getPackageUid(BuildConfig.APPLICATION_ID, 0)
                || callingUid == 2000 || callingUid == 0
    } catch (e: Throwable) {
        false
    }
}