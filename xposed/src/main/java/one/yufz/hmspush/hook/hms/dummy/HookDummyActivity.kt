package one.yufz.hmspush.hook.hms.dummy

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import one.yufz.hmspush.common.FLAG_HMS_DUMMY_HOOKED
import one.yufz.hmspush.common.HMS_CORE_DUMMY_ACTIVITY
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.findClass
import one.yufz.xposed.getAdditionalInstanceField
import one.yufz.xposed.hookMethod
import one.yufz.xposed.removeAdditionalInstanceField
import one.yufz.xposed.setAdditionalInstanceField


object HookDummyActivity {
    private const val TAG = "HookDummyActivity"

    private const val KEY_IGNORE_FIRST_FINISH = "ignore_first_finish"

    fun hook(classLoader: ClassLoader) {
        XLog.d(TAG, "hook() called")

        HookDummyActivityTask.hook(classLoader)

        val classDummyActivity = classLoader.findClass(HMS_CORE_DUMMY_ACTIVITY)
        classDummyActivity.hookMethod("onCreate", Bundle::class.java) {
            doBefore {
                XLog.d(TAG, "onCreate doBefore() called")
                thisObject.setAdditionalInstanceField(KEY_IGNORE_FIRST_FINISH, true)
                val activity = thisObject as Activity
                activity.setTheme(android.R.style.Theme_Material_Light_NoActionBar)
                if (args[0] != null) {
                    args[0] = null
                }
            }

            doAfter {
                val activity = this.thisObject as Activity
                val intent = activity.intent
                val hooked = intent.getBooleanExtra(FLAG_HMS_DUMMY_HOOKED, false)

                XLog.d(TAG, "onCreate doAfter() called, hooked = $hooked")

                if (hooked) {
                    makeActivityFullscreen(thisObject as Activity)

                    addDummyFragment(activity)
                }
            }
        }

        classDummyActivity.hookMethod("finish") {
            doBefore {
                val activity = this.thisObject as Activity
                val hooked = activity.intent.getBooleanExtra(FLAG_HMS_DUMMY_HOOKED, false)
                val ignoreFirstFinish = activity.getAdditionalInstanceField(KEY_IGNORE_FIRST_FINISH) != null

                XLog.d(TAG, "finish() called, hooked = $hooked , ignoreFirstFinish = $ignoreFirstFinish")

                if (hooked && ignoreFirstFinish) {
                    result = null
                }

                if (ignoreFirstFinish) {
                    activity.removeAdditionalInstanceField(KEY_IGNORE_FIRST_FINISH)
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

    private fun addDummyFragment(activity: Activity) {
        XLog.d(TAG, "addHmsDummyFragment() called")
        activity.fragmentManager.beginTransaction()
            .add(Window.ID_ANDROID_CONTENT, DummyFragment(), "hms_push_dummy")
            .commitNowAllowingStateLoss()
    }
}