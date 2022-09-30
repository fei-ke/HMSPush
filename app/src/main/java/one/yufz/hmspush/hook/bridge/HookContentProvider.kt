package one.yufz.hmspush.hook.bridge

import android.app.AndroidAppHelper
import android.content.ContentValues
import android.net.Uri
import android.os.Binder
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
                result = bridge.query(args[0] as Uri, args[1] as Array<String>?, args[2] as String?, args[3] as Array<String>?, args[4] as String?)
            }
        }

        // public  int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs);
        classModuleQueryProvider.hookMethod("update", Uri::class.java, ContentValues::class.java, String::class.java, Array<String>::class.java) {
            doBefore {
                if (fromHmsPush()) {
                    result = bridge.update(args[0] as Uri, args[1] as ContentValues?, args[2] as String?, args[3] as Array<out String>?)
                }
            }
        }

        //int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs);
        classModuleQueryProvider.hookMethod("delete", Uri::class.java, String::class.java, Array<String>::class.java) {
            doBefore {
                if (fromHmsPush()) {
                    result = bridge.delete(args[0] as Uri, args[1] as String?, args[2] as Array<String>?)
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