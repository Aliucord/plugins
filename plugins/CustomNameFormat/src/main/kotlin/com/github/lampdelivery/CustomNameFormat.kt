package com.github.lampdelivery

import android.content.Context
import android.text.TextUtils
import android.widget.TextView
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.after
import com.aliucord.Utils
import com.aliucord.wrappers.users.globalName
import com.discord.api.channel.Channel
import com.discord.databinding.*
import com.discord.models.member.GuildMember
import com.discord.models.user.User
import com.discord.stores.StoreStream
import com.discord.utilities.user.UserUtils
import com.discord.views.UsernameView
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListAdapter
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderMember
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemEmbed
import java.lang.reflect.Field

@AliucordPlugin
class CustomNameFormat : Plugin() {
    enum class Format {
        DEFAULT,
        USERNAME,
        DISPLAYNAME_USERNAME,
        USERNAME_DISPLAYNAME,
        NICKNAME_USERNAME,
        USERNAME_NICKNAME
    }

    enum class Separator {
        PARENTHESES,
        BRACKETS,
        PIPES,
        BULLETS,
        DASHES,
        CUSTOM
    }

    enum class CasingMode {
        NONE,
        PROPER_CASE,
        LOWER_CASE,
        UPPER_CASE
    }

    private val formattedTexts = mutableSetOf<String>()

    override fun start(context: Context) {
        ReflectionExtensions.init()

        try {
            patcher.after<TextView>("setText", CharSequence::class.java, TextView.BufferType::class.java) { param ->
                val textView = param.thisObject as? TextView ?: return@after
                val text = param.args[0]?.toString() ?: return@after

                if (!settings.getBool("enableMarquee", false)) return@after

                if (text.length <= settings.getInt("maxLength", 20)) return@after

                val isTrackedText = synchronized(formattedTexts) { formattedTexts.contains(text) }
                if (!(isTrackedText || isUsernameFormat(text))) return@after

                setMarquee(textView)
            }
        } catch (e: Exception) {
        }

        try {
            patcher.patch(
                GuildMember.Companion::class.java.getDeclaredMethod(
                    "getNickOrUsername",
                    com.discord.models.user.User::class.java,
                    GuildMember::class.java,
                    Channel::class.java,
                    List::class.java
                ),
                Hook { param ->
                    val format = Format.valueOf(settings.getString("format", Format.DEFAULT.name))
                    if (format == Format.DEFAULT) return@Hook

                    if (!settings.getBool("displayInChat", true)) return@Hook

                    val user = param.args[0] as? com.discord.models.user.User ?: return@Hook
                    val username = user.username
                    val result = param.result as? String ?: username

                    val formatted = getFormatted(username, result, user)
                    param.result = formatted

                    synchronized(formattedTexts) {
                        formattedTexts.add(formatted)
                        if (formattedTexts.size > 1000) {
                            formattedTexts.clear()
                        }
                    }
                }
            )
        } catch (e: Exception) {
        }

        try {
            patcher.patch(
                Class.forName("com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemEmbed\$Companion\$getModel\$1")
                    .getDeclaredMethod("call", Object::class.java, Object::class.java),
                Hook { param ->
                    val format = Format.valueOf(settings.getString("format", Format.DEFAULT.name))
                    if (format == Format.DEFAULT) return@Hook

                    if (!settings.getBool("displayInChat", true)) return@Hook

                    @Suppress("UNCHECKED_CAST")
                    val map = param.result as? MutableMap<Long, String> ?: return@Hook
                    if (map.isEmpty()) return@Hook
                    val users = StoreStream.getUsers().users
                    for ((id, value) in map.entries) {
                        val user = users[id] as? com.discord.models.user.User ?: continue
                        val formatted = getFormatted(user.username, value, user)
                        map[id] = formatted

                        synchronized(formattedTexts) {
                            formattedTexts.add(formatted)
                            if (formattedTexts.size > 1000) {
                                formattedTexts.clear()
                            }
                        }
                    }
                }
            )
        } catch (e: Exception) {
            try {
                patcher.patch(
                    WidgetChatListAdapterItemEmbed::class.java.getDeclaredMethod(
                        "getModel",
                        Any::class.java,
                        Any::class.java
                    ),
                    Hook { param ->
                        val format = Format.valueOf(settings.getString("format", Format.DEFAULT.name))
                        if (format == Format.DEFAULT) return@Hook

                        if (!settings.getBool("displayInChat", true)) return@Hook

                        val mapResult = param.result as? MutableMap<*, *> ?: return@Hook
                        if (mapResult.isEmpty()) return@Hook
                        val users = StoreStream.getUsers().users
                        for ((idAny, valueAny) in mapResult) {
                            val id = idAny as? Long ?: continue
                            val value = valueAny as? String ?: continue
                            val user = users[id] as? User ?: continue
                            val formatted = getFormatted(user.username, value, user)
                            @Suppress("UNCHECKED_CAST")
                            (mapResult as MutableMap<Any?, Any?>)[id] = formatted

                            synchronized(formattedTexts) {
                                formattedTexts.add(formatted)
                                if (formattedTexts.size > 1000) {
                                    formattedTexts.clear()
                                }
                            }
                        }
                    }
                )
            } catch (e2: Exception) {
            }
        }

        try {
            patcher.after<ChannelMembersListViewHolderMember>(
                "bind",
                ChannelMembersListAdapter.Item.Member::class.java,
                Function0::class.java
            ) { param ->
                val format = Format.valueOf(settings.getString("format", Format.DEFAULT.name))
                if (format == Format.DEFAULT) return@after

                if (!settings.getBool("displayInMemberList", true)) return@after

                val memberItem = param.args[0] as? ChannelMembersListAdapter.Item.Member ?: return@after
                val memberHolder = param.thisObject as? ChannelMembersListViewHolderMember ?: return@after

                try {
                    val binding = ReflectionExtensions.getBinding(memberHolder)
                    val usernameView = binding.f
                    val usernameTextView = usernameView.j.c

                    val memberUser = try {
                        val userField = memberItem.javaClass.getDeclaredField("user")
                        userField.isAccessible = true
                        userField.get(memberItem) as? User
                    } catch (e: Exception) {
                        try {
                            val userIdField = memberItem.javaClass.getDeclaredField("userId")
                            userIdField.isAccessible = true
                            val userId = userIdField.get(memberItem) as? Long
                            userId?.let { StoreStream.getUsers().users[it] as? User }
                        } catch (e2: Exception) {
                            null
                        }
                    } ?: return@after

                    val originalText = usernameTextView.text?.toString() ?: return@after
                    val formatted = getFormatted(memberUser.username, originalText, memberUser)
                    usernameTextView.text = formatted

                    synchronized(formattedTexts) {
                        formattedTexts.add(formatted)
                        if (formattedTexts.size > 1000) {
                            formattedTexts.clear()
                        }
                    }

                    if (settings.getBool("enableMarquee", false) && formatted.length > settings.getInt(
                            "maxLength",
                            20
                        )
                    ) {
                        setMarquee(usernameTextView)
                    }
                } catch (_: Throwable) {
                }
            }
        } catch (_: Throwable) {
        }
    }

    private fun isUsernameFormat(text: String): Boolean {
        val patterns = listOf(
            ".*\\s+\\(.*\\)$".toRegex(),
            ".*\\s+\\[.*\\]$".toRegex(),
            ".*\\s+\\|\\s+.*$".toRegex(),
            ".*\\s+•\\s+.*$".toRegex(),
            ".*\\s+-\\s+.*$".toRegex()
        )

        val customSep = settings.getString("customSeparator", " - ")
        if (customSep.isNotEmpty()) {
            val customPattern = ".*\\Q${customSep}\\E.*".toRegex()
            if (customPattern.matches(text)) return true
        }

        return patterns.any { it.matches(text) }
    }

    private fun setMarquee(textView: TextView?) {
        textView?.apply {
            ellipsize = TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1
            isSelected = true
            isSingleLine = true
            isHorizontalFadingEdgeEnabled = true
            setHorizontallyScrolling(true)
        }
    }

    object ReflectionExtensions {
        private lateinit var memberBinding: Field

        fun init() {
            try {
                memberBinding = ChannelMembersListViewHolderMember::class.java.getDeclaredField("binding").apply {
                    isAccessible = true
                }
            } catch (_: Throwable) {
            }
        }

        fun getBinding(member: ChannelMembersListViewHolderMember): WidgetChannelMembersListItemUserBinding {
            return memberBinding.get(member) as WidgetChannelMembersListItemUserBinding
        }
    }

    init {
        settingsTab = SettingsTab(Settings::class.java, SettingsTab.Type.PAGE).withArgs(settings)
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        formattedTexts.clear()
    }

    private fun getFormatted(username: String, res: String, user: User): String {
        if (settings.getBool("formatOthersOnly", false)) {
            val currentUser = StoreStream.getUsers().me
            if (currentUser != null && currentUser.id == user.id) {
                return res
            }
        }

        val displayName = user.globalName ?: username
        val format = Format.valueOf(settings.getString("format", Format.DEFAULT.name))

        if (format == Format.DEFAULT) {
            return res
        }

        val separator = try {
            Separator.valueOf(settings.getString("separator", Separator.PARENTHESES.name))
        } catch (e: IllegalArgumentException) {
            settings.setString("separator", Separator.PARENTHESES.name)
            Separator.PARENTHESES
        }
        val casing = CasingMode.valueOf(settings.getString("casing", CasingMode.NONE.name))
        val maxLength = settings.getInt("maxLength", 20)
        val smartConditional = settings.getBool("smartConditional", true)

        val (primary, secondary) = when (format) {
            Format.DEFAULT -> return res
            Format.USERNAME -> return truncateText(applyCasing(username, casing), maxLength)
            Format.DISPLAYNAME_USERNAME -> displayName to username
            Format.USERNAME_DISPLAYNAME -> username to displayName
            Format.NICKNAME_USERNAME -> res to username
            Format.USERNAME_NICKNAME -> username to res
        }

        if (smartConditional && primary.equals(secondary, ignoreCase = true)) {
            return truncateText(applyCasing(primary, casing), maxLength)
        }

        val primaryFormatted = applyCasing(primary, casing)
        val secondaryFormatted = applyCasing(secondary, casing)

        val combined = formatWithSeparator(primaryFormatted, secondaryFormatted, separator)

        return truncateText(combined, maxLength)
    }

    private fun formatWithSeparator(primary: String, secondary: String, separator: Separator): String {
        return when (separator) {
            Separator.PARENTHESES -> "$primary ($secondary)"
            Separator.BRACKETS -> "$primary [$secondary]"
            Separator.PIPES -> "$primary | $secondary"
            Separator.BULLETS -> "$primary • $secondary"
            Separator.DASHES -> "$primary - $secondary"
            Separator.CUSTOM -> {
                val customSep = settings.getString("customSeparator", " - ")
                "$primary$customSep$secondary"
            }
        }
    }

    private fun applyCasing(text: String, mode: CasingMode): String {
        return when (mode) {
            CasingMode.NONE -> text
            CasingMode.PROPER_CASE -> text.split(" ").joinToString(" ") { word ->
                if (word.isNotEmpty()) {
                    word.first().uppercase() + word.drop(1).lowercase()
                } else {
                    word
                }
            }
            CasingMode.LOWER_CASE -> text.lowercase()
            CasingMode.UPPER_CASE -> text.uppercase()
        }
    }

    private fun truncateText(text: String, maxLength: Int): String {
        if (settings.getBool("enableMarquee", false)) {
            return text
        }

        return if (text.length > maxLength && maxLength > 3) {
            text.take(maxLength - 3) + "..."
        } else {
            text
        }
    }
}
