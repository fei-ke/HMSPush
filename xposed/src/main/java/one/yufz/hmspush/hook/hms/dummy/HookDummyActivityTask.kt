package one.yufz.hmspush.hook.hms.dummy

import android.app.Activity
import android.app.ActivityManager
import one.yufz.xposed.AndroidAppHelper
import android.app.Application
import android.os.Bundle
import one.yufz.hmspush.common.HMS_CORE_DUMMY_ACTIVITY
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.hookMethod


object HookDummyActivityTask {
    private const val TAG = "HookDummyActivityTask"

    fun hook(classLoader: ClassLoader) {
        Application::class.java.hookMethod("onCreate") {
            doAfter {
                val application = thisObject as Application
                registerActivityLifecycle(application)
            }
        }
    }

    private fun registerActivityLifecycle(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity.javaClass.name == HMS_CORE_DUMMY_ACTIVITY) {
                    val activityManager = AndroidAppHelper.currentApplication().getSystemService(ActivityManager::class.java)
                    activityManager.appTasks.find { it.taskInfo.id == activity.taskId }?.let {
                        it.setExcludeFromRecents(false)
                        XLog.d(TAG, "task: ${it.taskInfo}")
                    }
                }
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}