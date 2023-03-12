package one.yufz.hmspush.hook

import io.github.libxposed.api.XposedContext
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import one.yufz.xposed.toLoadPackageParam

class LibXposedEntry(base: XposedContext, param: XposedModuleInterface.ModuleLoadedParam) : XposedModule(base, param) {
    companion object {
        lateinit var xposedInterface: XposedInterface
    }

    init {
        xposedInterface = this
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        if (param.isFirstPackage) {
            HookEntry.onHook(param.toLoadPackageParam())
        }
    }

    override fun onSystemServerLoaded(param: XposedModuleInterface.SystemServerLoadedParam) {
    }
}