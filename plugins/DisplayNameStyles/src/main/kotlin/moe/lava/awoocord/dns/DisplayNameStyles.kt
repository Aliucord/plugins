@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package moe.lava.awoocord.dns

import android.content.Context
import com.aliucord.PluginManager
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.utils.ReflectUtils

internal typealias Decorations = com.aliucord.coreplugins.Decorations
internal typealias Decorator = com.aliucord.coreplugins.decorations.Decorator

@AliucordPlugin(requiresRestart = true)
@Suppress("unused")
internal class DisplayNameStyles : Plugin() {
    override fun load(context: Context) {
        val decoPlug = PluginManager.plugins["Decorations"] as? Decorations
            ?: return logger.warn("Decorations not loaded, possibly outdated client")

        @Suppress("UNCHECKED_CAST")
        val decorators = ReflectUtils.getField(decoPlug, "decorators") as List<Decorator>
        ReflectUtils.setFinalField(decoPlug, "decorators", decorators + DisplayNameStylesDecorator())
    }
}
