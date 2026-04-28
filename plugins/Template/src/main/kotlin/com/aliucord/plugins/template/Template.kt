package com.aliucord.plugins.template

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin

@AliucordPlugin(requiresRestart = false)
@Suppress("unused")
class Template : Plugin() {
    override fun start(context: Context) {}
    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
