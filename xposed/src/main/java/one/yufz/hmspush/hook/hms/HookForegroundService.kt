package one.yufz.hmspush.hook.hms

import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.ServiceStartNotAllowedException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.PowerManager
import android.os.UserManager
import android.widget.Toast
import one.yufz.hmspush.common.HMS_PACKAGE_NAME
import one.yufz.hmspush.common.HmsCoreUtil
import one.yufz.hmspush.common.KEY_HMS_CORE_EXPLICIT_FOREGROUND
import one.yufz.hmspush.hook.I18n
import one.yufz.hmspush.hook.XLog
import one.yufz.xposed.findClass
import one.yufz.xposed.hookMethod

object HookForegroundService {
    private const val TAG = "HookForegroundService"

    fun hook(classLoader: ClassLoader) {
        Application::class.java.hookMethod("onCreate") {
            doAfter {
                val application = thisObject as Application

                //TODO remove this after 2023-03-01
                val userManager = application.getSystemService(UserManager::class.java)
                //SharedPreferences in credential encrypted storage are not available until after user is unlocked
                StorageContext.useDeviceProtectedStorageContext = migrateDataToDeviceProtectedStorage()

                if (!userManager.isUserUnlocked && !StorageContext.useDeviceProtectedStorageContext) return@doAfter

                if (Prefs.prefModel.keepAlive) {
                    tryStartForegroundService(application)
                }
            }
        }

        val classHMSCoreService = classLoader.findClass("com.huawei.hms.core.service.HMSCoreService")
        classHMSCoreService.hookMethod("onCreate") {
            doAfter {
                XLog.d(TAG, "onCreate() called")
                HmsPushService.notifyHmsPushServiceCreated()
                setupForegroundState(thisObject as Service)
            }
        }
        classHMSCoreService.hookMethod("onStartCommand", Intent::class.java, Int::class.java, Int::class.java) {
            doAfter {
                XLog.d(TAG, "onStartCommand() called")
                setupForegroundState(thisObject as Service, args[0] as Intent)
            }
        }
    }

    private fun setupForegroundState(service: Service, intent: Intent? = null) {
        XLog.d(TAG, "setupForeground() called")

        val userManager = service.getSystemService(UserManager::class.java)
        //SharedPreferences in credential encrypted storage are not available until after user is unlocked
        if (!userManager.isUserUnlocked && !StorageContext.useDeviceProtectedStorageContext) return

        if (Prefs.prefModel.keepAlive) {
            val explicitForeground = intent?.getBooleanExtra(KEY_HMS_CORE_EXPLICIT_FOREGROUND, false) == true
            if (explicitForeground) {
                makeServiceForeground(service)
            } else {
                tryStartForegroundService(service)
            }
        } else {
            stopForeground(service)
        }
    }

    private fun tryStartForegroundService(context: Context) {
        val ignoringBatteryOptimizations = context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(HMS_PACKAGE_NAME)
        XLog.d(TAG, "tryStartForegroundService() called: ignoringBatteryOptimizations = $ignoringBatteryOptimizations")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            HmsCoreUtil.startHmsCoreService(context, true)
            return
        }
        if (ignoringBatteryOptimizations) {
            HmsCoreUtil.startHmsCoreService(context, true)
            return
        }
        try {
            HmsCoreUtil.startHmsCoreService(context, true)
        } catch (e: ServiceStartNotAllowedException) {
            Toast.makeText(context, I18n.get(context).tipsOptimizeBattery, Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun makeServiceForeground(service: Service) {
        XLog.d(TAG, "startForeground() called")
        val channelId = "hms_core_service"
        val channel = NotificationChannel(channelId, I18n.get(service).hmsCoreRunningState, NotificationManager.IMPORTANCE_LOW)
        val manager = service.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val iconId = service.resources.getIdentifier("update_notification_icon", "drawable", HMS_PACKAGE_NAME)
            .takeIf { it != 0 } ?: android.R.drawable.ic_dialog_info

        val contentIntent = PendingIntent.getActivity(service, 1, HmsCoreUtil.createHmsCoreDummyActivityIntent(), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = Notification.Builder(service, channelId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }
        builder.setSmallIcon(Icon.createWithBitmap(drawableToGrayscaleBitmap(service.getDrawable(iconId)!!)))
            .setContentIntent(contentIntent)
            .setContentText(I18n.get(service).hmsCoreRunning)
            .setAutoCancel(false)
            .build()
        service.startForeground(11111, builder.build())
    }

    private fun stopForeground(service: Service) {
        XLog.d(TAG, "stopForeground() called")
        service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
    }

    private fun drawableToGrayscaleBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ALPHA_8)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}