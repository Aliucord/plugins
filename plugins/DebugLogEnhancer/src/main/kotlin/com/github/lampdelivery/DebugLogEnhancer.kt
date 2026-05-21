package com.github.lampdelivery

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook

@AliucordPlugin

class DebugLogEnhancer : Plugin() {
    private val patchedAdapters = mutableSetOf<Int>()
    private var logSub: Any? = null

    override fun start(context: Context) {
        try {
            val cls = Class.forName("com.discord.widgets.debugging.WidgetDebugging")
            val onViewBound = cls.getDeclaredMethod("onViewBound", View::class.java)
            patcher.patch(onViewBound, Hook { cf ->
                val fragment = this
                try {
                    val activity = cf.thisObject as? androidx.fragment.app.Fragment ?: return@Hook
                    val fm = activity.parentFragmentManager
                    val page = com.github.lampdelivery.EnhancedLogsPage(settings)
                    fm.beginTransaction().replace(activity.id, page).commitAllowingStateLoss()
                } catch (e: Throwable) {
                    logger.info("DebugLogEnhancer: Could not show EnhancedLogsPage")
                    logger.error(e)
                }
            })
        } catch (_: Throwable) {}
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        logSub = null
    }
}
