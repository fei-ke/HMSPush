package one.yufz.hmspush.settings

import android.app.Activity
import android.os.Bundle
import android.view.Window
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
            }

            doAfter {
                val activity = this.thisObject as Activity
                val intent = activity.intent
                val hmsPushSetting = intent.getBooleanExtra(FLAG_HMS_PUSH_SETTINGS, false)

                XLog.d(TAG, "onCreate doAfter() called, hmsPushSetting = $hmsPushSetting")

                if (hmsPushSetting) {
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

    private fun addHmsPushSetting(activity: Activity) {
        XLog.d(TAG, "addHmsPushSetting() called")
        activity.fragmentManager.beginTransaction()
            .add(Window.ID_ANDROID_CONTENT, AppListFragment(), "hms_push_setting")
            .commitNowAllowingStateLoss()
    }
}