package com.github.lampdelivery

import android.content.Context
import android.view.View
import android.widget.TextView
import android.view.ViewGroup
import com.aliucord.patcher.after
import com.aliucord.*
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.fragments.SettingsPage
import com.aliucord.patcher.Hook
import com.discord.stores.StoreChannels

@AliucordPlugin(requiresRestart = false)

class CompactLinks : Plugin() {
    override fun start(context: Context) {
        settingsTab = SettingsTab(Settings::class.java).withArgs(settings)
        with(com.discord.api.message.Message::class.java) {
            patcher.patch(getDeclaredMethod("i"), Hook { callFrame ->
                try {
                    if (callFrame.result == null) return@Hook
                    var content = callFrame.result as String
                    if (content == "") return@Hook

                    if (settings.getBool("compactMessageLinks", true)) {
                        val messageRegex = Regex("""(?<!\]\()https://discord\.com/channels/(\d+|@me)/(\d+)/(\d+)""")
                        content = messageRegex.replace(content) { match ->
                            val isDm = match.groupValues[1] == "@me"
                            val channelId = match.groupValues[2]
                            val channelName = getChannelName(channelId, isDm) ?: "unknown"
                            "[#$channelName > 💬](${match.value})"
                        }
                    }
                    if (settings.getBool("compactChannelLinks", true)) {
                        val channelRegex = Regex("""(?<!\]\()https://discord\.com/channels/(\d+)/(\d+)(?!/(\d+))""")
                        content = channelRegex.replace(content) { match ->
                            val guildId = match.groupValues[1]
                            val channelId = match.groupValues[2]
                            val channelName = getChannelName(channelId) ?: "unknown"
                            val serverName = getGuildName(guildId) ?: "unknown"
                            "[$serverName > #$channelName](${match.value})"
                        }
                    }
                        if (settings.getBool("compactRawLinks", true)) {
                            val rawRegex = Regex("https?://\\S+/([\\w\\-.]+\\.(?i:json|txt))(\\?\\S*)?")
                            content = rawRegex.replace(content) { match: MatchResult ->
                                val url = match.value
                                val filename = match.groupValues[1]
                                // Only compact if not already in markdown format
                                val markdownRegex = Regex("\\[$filename]\\($url\\) 🔗")
                                if (markdownRegex.containsMatchIn(content)) url else "[$filename]($url) 🔗"
                            }
                        }
                    callFrame.result = content
                } catch (_: Throwable) {}
            })
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    private fun getChannelName(channelId: String, isDm: Boolean = false): String? {
        return try {
            val storeStreamClass = Class.forName("com.discord.stores.StoreStream")
            val getChannelsMethod = storeStreamClass.getDeclaredMethod("getChannels")
            val channelsStore = getChannelsMethod.invoke(null)
            val getChannelMethod = channelsStore.javaClass.getDeclaredMethod("getChannel", Long::class.java)
            val channel = getChannelMethod.invoke(channelsStore, channelId.toLong()) ?: return null

            if (isDm) {
                try {
                    val nameField = channel.javaClass.getDeclaredField("name").apply { isAccessible = true }
                    val name = nameField.get(channel) as? String
                    if (!name.isNullOrBlank()) return name
                } catch (_: Throwable) {}
                try {
                    val recipientsField = channel.javaClass.getDeclaredField("recipients").apply { isAccessible = true }
                    val recipients = recipientsField.get(channel) as? List<*>
                    val user = recipients?.firstOrNull()
                    val usernameField = user?.javaClass?.getDeclaredField("username")?.apply { isAccessible = true }
                    return usernameField?.get(user) as? String
                } catch (_: Throwable) {}
                return "DM"
            } else {
                // Guild channel
                val nameField = channel.javaClass.getDeclaredField("name").apply { isAccessible = true }
                return nameField.get(channel) as? String
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun getGuildName(guildId: String): String? {
        return try {
            val storeStreamClass = Class.forName("com.discord.stores.StoreStream")
            val getGuildsMethod = storeStreamClass.getDeclaredMethod("getGuilds")
            val guildsStore = getGuildsMethod.invoke(null)
            val getGuildMethod = guildsStore.javaClass.getDeclaredMethod("getGuild", Long::class.java)
            val guild = getGuildMethod.invoke(guildsStore, guildId.toLong()) ?: return null
            val nameField = guild.javaClass.getDeclaredField("name").apply { isAccessible = true }
            nameField.get(guild) as? String
        } catch (_: Throwable) {
            null
        }
    }
}
