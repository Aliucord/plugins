package com.github.ushie

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.before
import com.discord.api.utcdatetime.UtcDateTime
import com.discord.models.member.GuildMember

@Suppress("unused")
@AliucordPlugin
class GlobalProfilesOnly : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    override fun start(context: Context) {
        val disableNick = settings.getBool("disableNick", false)
        val disableAvatar = settings.getBool("disableAvatar", true)
        val disableBanner = settings.getBool("disableBanner", true)
        patcher.before<GuildMember>(
            Int::class.javaPrimitiveType!!,
            Long::class.javaPrimitiveType!!,
            List::class.java,
            String::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType!!,
            UtcDateTime::class.java,
            Long::class.javaPrimitiveType!!,
            Long::class.javaPrimitiveType!!,
            String::class.java,
            String::class.java,
            String::class.java,
            UtcDateTime::class.java
        ) {
            if (disableNick) it.args[3] = null // nick
            if (disableAvatar) it.args[9] = null // avatarHash
            if (disableBanner) it.args[10] = null // bannerHash
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
