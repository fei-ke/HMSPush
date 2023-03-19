package one.yufz.hmspush.hook

import io.github.libxposed.api.XposedContext
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import one.yufz.xposed.LibXposedLoadPackageParam

class LibXposedEntry(base: XposedContext, val moduleLoadedParam: XposedModuleInterface.ModuleLoadedParam) : XposedModule(base, moduleLoadedParam) {
    companion object {
        lateinit var xposedInterface: XposedInterface
    }

    init {
        xposedInterface = this
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        if (param.isFirstPackage) {
            HookEntry.onHook(
                LibXposedLoadPackageParam(
                    packageName = param.packageName,
                    processName = moduleLoadedParam.processName,
                    appInfo = param.appInfo,
                    classLoader = param.classLoader,
                    isFirstPackage = param.isFirstPackage
                )
            )
        }
    }

    override fun onSystemServerLoaded(param: XposedModuleInterface.SystemServerLoadedParam) {
    }
}