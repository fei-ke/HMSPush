package one.yufz.hmspush.settings

import android.app.Activity
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import de.robv.android.xposed.XposedHelpers
import one.yufz.hmspush.FLAG_HMS_PUSH_SETTINGS
import one.yufz.hmspush.XLog
import one.yufz.hmspush.findClass
import one.yufz.hmspush.hookMethod


class HookSettings {
    companion object {
        private const val TAG = "HookSettingsUI"

        private const val KEY_IGNORE_FIRST_FINISH = "ignore_first_finish"
    }

    fun hook(classLoader: ClassLoader) {
        XLog.d(TAG, "hook() called")

        val classHmsSettingActivity = classLoader.findClass("com.huawei.hms.core.activity.JumpActivity")
        classHmsSettingActivity.hookMethod("onCreate", Bundle::class.java) {
            doBefore {
                XLog.d(TAG, "onCreate doBefore() called")
                XposedHelpers.setAdditionalInstanceField(thisObject, KEY_IGNORE_FIRST_FINISH, true)
                val activity = thisObject as Activity
                activity.setTheme(android.R.style.Theme_Material_Light_NoActionBar)
                if (args[0] != null) {
                    args[0] = null
                }
            }

            doAfter {
                val activity = this.thisObject as Activity
                val intent = activity.intent
                val hmsPushSetting = intent.getBooleanExtra(FLAG_HMS_PUSH_SETTINGS, false)

                XLog.d(TAG, "onCreate doAfter() called, hmsPushSetting = $hmsPushSetting")

                if (hmsPushSetting) {
                    makeActivityFullscreen(thisObject as Activity)

                    addHmsPushSetting(activity)
                }
            }
        }

        classHmsSettingActivity.hookMethod("finish") {
            doBefore {
                val activity = this.thisObject as Activity
                val hmsPushSetting = activity.intent.getBooleanExtra(FLAG_HMS_PUSH_SETTINGS, false)
                val ignoreFirstFinish = XposedHelpers.getAdditionalInstanceField(activity, KEY_IGNORE_FIRST_FINISH) != null

                XLog.d(TAG, "finish() called, hmsPushSetting = $hmsPushSetting , ignoreFirstFinish = $ignoreFirstFinish")

                if (hmsPushSetting && ignoreFirstFinish) {
                    result = null
                }

                if (ignoreFirstFinish) {
                    XposedHelpers.removeAdditionalInstanceField(activity, KEY_IGNORE_FIRST_FINISH)
                }
            }
        }
    }

    private fun makeActivityFullscreen(activity: Activity) {
        activity.window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility = decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun addHmsPushSetting(activity: Activity) {
        XLog.d(TAG, "addHmsPushSetting() called")

        try {
            activity.applicationContext.createModuleContext()
        } catch (e: NameNotFoundException) {
            XLog.e(TAG, "addHmsPushSetting abort", e)
            activity.fragmentManager.beginTransaction()
                .add(Window.ID_ANDROID_CONTENT, RestrictedFragment(), "restricted")
                .commitNowAllowingStateLoss()
            return
        }

        activity.fragmentManager.beginTransaction()
            .add(Window.ID_ANDROID_CONTENT, AppListFragment(), "hms_push_setting")
            .commitNowAllowingStateLoss()
    }
}