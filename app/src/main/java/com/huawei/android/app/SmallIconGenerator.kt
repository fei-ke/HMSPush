package com.huawei.android.app

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Icon

import de.robv.android.xposed.XposedHelpers

import one.yufz.hmspush.XLog

import com.notxx.icon.IconCache

object SmallIconGenerator {
    private val T = "SmallIconGenerator"

    fun generate(context: Context, packageName: String, notification: android.app.Notification) {
        val cache = IconCache.getInstance()
        var icon:Icon? = null; var color:Int? = null

        var resContext = context.createPackageContext(IconCache.RES_PACKAGE)
        if (resContext != null) { // res-icon
            icon = cache.getExtIcon(resContext, packageName)
            if (icon != null) {
                //n.setSmallIcon(icon)
                XposedHelpers.setObjectField(notification, "mSmallIcon", icon)
            }
            color = cache.getExtColor(resContext, packageName)
            if (color != null) {
                notification.color = color
            }
        }

        val appContext = context.createPackageContext(packageName)
        if (icon != null) { // do nothing
        } else if (appContext != null) {
            icon = cache.getMiPushIcon(appContext, packageName)
            if (icon != null) { // has embed icon
                //n.setSmallIcon(icon)
                XposedHelpers.setObjectField(notification, "mSmallIcon", icon)
            }
        }

        if (icon == null) {
            icon = cache.getIcon(context, packageName)
            if (icon != null) {
                //n.setSmallIcon(icon)
                XposedHelpers.setObjectField(notification, "mSmallIcon", icon)
            } else {
                //n.setSmallIcon(defIcon)
                //XposedHelpers.setObjectField(notification, "mSmallIcon", defIcon)
            }
        }
        if (color != null) { // do nothing
        } else {
            notification.color = cache.getAppColor(context, packageName) ?: Color.BLACK
        }
    }
}

fun Context.createPackageContext(packageName:String):Context? {
    try {
        return this.createPackageContext(packageName, 0)
    } catch (ign:IllegalArgumentException) {
        // Log.d("SmallIconGenerator", "ex " + packageName)
        return null
    } catch (ign:PackageManager.NameNotFoundException) {
        // Log.d("SmallIconGenerator", "ex " + packageName)
        return null
    }
}