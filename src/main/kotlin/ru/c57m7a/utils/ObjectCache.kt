package ru.c57m7a.utils

open class ObjectCache<K, V>(val default: (K) -> V) {
    protected val map = HashMap<K, V>()

    operator fun get(key: K): V = map.getOrPut(key) { default(key) }
}

open class ForeignKeyObjectCache<in K, FK, V>(val default: (K) -> V, val select: (K) -> FK) {
    protected val map = HashMap<FK, V>()

    operator fun get(key: K): V = map.getOrPut(select(key)) { default(key) }
}