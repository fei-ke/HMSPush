# Proguard for Xposed.
-keep class * implements de.robv.android.xposed.IXposedHookZygoteInit
-keep class * implements de.robv.android.xposed.IXposedHookLoadPackage
-keep class * implements de.robv.android.xposed.IXposedHookInitPackageResources

-keep class one.yufz.hmspush.hook.XposedMod{
    *;
}
-keep class com.huawei.android.app.NotificationManagerEx{
    *;
}