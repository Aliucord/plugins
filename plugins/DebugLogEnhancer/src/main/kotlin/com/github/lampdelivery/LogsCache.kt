package com.github.lampdelivery

object LogsCache {
    private val lock = Any()
    private val _entries = mutableListOf<String>()
    private val listeners = mutableSetOf<(String) -> Unit>()
    private val clearListeners = mutableSetOf<() -> Unit>()
    var maxEntries = 5000

    fun add(entry: String) {
        val toNotify: List<(String) -> Unit>
        synchronized(lock) {
            _entries.add(entry)
            if (_entries.size > maxEntries) _entries.removeAt(0)
            toNotify = listeners.toList()
        }
        for (l in toNotify) {
            try { l(entry) } catch (_: Throwable) {}
        }
    }

    fun snapshot(): List<String> = synchronized(lock) { _entries.toList() }

    fun clear() {
        val toNotify: List<() -> Unit>
        synchronized(lock) {
            _entries.clear()
            toNotify = clearListeners.toList()
        }
        for (l in toNotify) {
            try { l() } catch (_: Throwable) {}
        }
    }

    fun addListener(l: (String) -> Unit) = synchronized(lock) { listeners.add(l) }
    fun removeListener(l: (String) -> Unit) = synchronized(lock) { listeners.remove(l) }

    fun addClearListener(l: () -> Unit) = synchronized(lock) { clearListeners.add(l) }
    fun removeClearListener(l: () -> Unit) = synchronized(lock) { clearListeners.remove(l) }
}
