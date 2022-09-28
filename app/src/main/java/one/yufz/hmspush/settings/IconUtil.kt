package one.yufz.hmspush.settings

import android.app.AndroidAppHelper
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.drawable.Drawable
import one.yufz.hmspush.HMS_PACKAGE_NAME
import one.yufz.hmspush.R

object IconUtil {
    private val ID_MORE_ICON: Int by lazy { AndroidAppHelper.currentApplication().resources.getIdentifier("common_appbar_more", "mipmap", HMS_PACKAGE_NAME) }

    fun getMoreIcon(context: Context): Drawable? {
        return try {
            context.createModuleContext().getDrawable(R.drawable.ic_more)
        } catch (e: PackageManager.NameNotFoundException) {
            if (ID_MORE_ICON != 0) {
                context.getDrawable(ID_MORE_ICON)
            } else {
                context.getDrawable(android.R.drawable.ic_menu_more)
            }
        }
    }

    fun getSearchIcon(context: Context): Drawable? {
        return try {
            context.createModuleContext().getDrawable(R.drawable.ic_search)
        } catch (e: NameNotFoundException) {
            context.getDrawable(android.R.drawable.ic_menu_search)
        }
    }
}