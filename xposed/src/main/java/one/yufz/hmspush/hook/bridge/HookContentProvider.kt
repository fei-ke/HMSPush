package one.yufz.hmspush.hook.bridge

import android.app.AndroidAppHelper
import android.net.Uri
import android.os.Binder
import one.yufz.hmspush.common.APPLICATION_ID
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
                result = bridge.query(args[0] as Uri, args[1] as Array<String>?, args[2] as String?, args[3] as Array<String>?, args[4] as String?)
            }
        }
    }

    private fun fromHmsPush() = try {
        val callingUid = Binder.getCallingUid()
        callingUid == AndroidAppHelper.currentApplication().packageManager.getPackageUid(APPLICATION_ID, 0)
                || callingUid == 2000 || callingUid == 0
    } catch (e: Throwable) {
        false
    }
}