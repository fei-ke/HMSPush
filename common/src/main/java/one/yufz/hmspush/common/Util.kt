package one.yufz.hmspush.common

import java.util.concurrent.ConcurrentHashMap

private val map: MutableMap<Any, Boolean> = ConcurrentHashMap()

fun doOnce(key: Any, action: () -> Unit) {
    synchronized(key) {
        if (map.containsKey(key)) {
            return
        }
        map[key] = true
    }
    action()
}