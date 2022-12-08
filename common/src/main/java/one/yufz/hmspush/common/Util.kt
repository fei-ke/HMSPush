package one.yufz.hmspush.common

import java.util.*

private val objMap: MutableMap<Any, HashMap<Any, Boolean>> = WeakHashMap()

fun Any.doOnce(action: () -> Unit) {
    doOnce(this, action)
}

fun Any.doOnce(key: Any, action: () -> Unit) {
    val keyMap = synchronized(objMap) {
        objMap.getOrPut(this) { HashMap() }
    }

    synchronized(keyMap) {
        if (keyMap.containsKey(key)) {
            return
        }
        keyMap[key] = true
    }
    action()
}