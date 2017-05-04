package ru.c57m7a.utils

import org.apache.logging.log4j.util.MessageSupplier

open class ObjectCache<K, V>(val default: (K) -> V) {
    protected val map = HashMap<K, V>()

    operator fun get(key: K): V = map.getOrPut(key) {
        val v = default(key)
        logger.debug("created $v at ${Thread.currentThread().stackTrace[2]}")
        v
    }
}

open class ForeignKeyObjectCache<in K, FK, V>(val default: (K) -> V, val select: (K) -> FK) {
    protected val map = HashMap<FK, V>()

    operator fun get(key: K): V = map.getOrPut(select(key)) { default(key) }
}