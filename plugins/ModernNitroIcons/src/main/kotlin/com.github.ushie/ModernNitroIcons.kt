package com.github.ushie

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.rn.user.RNUserProfile
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.patcher.component1
import com.aliucord.patcher.component2
import com.aliucord.utils.lazyField
import com.discord.api.user.UserProfile
import com.discord.databinding.UserProfileHeaderBadgeBinding
import com.discord.models.user.User
import com.discord.widgets.user.Badge
import com.discord.widgets.user.profile.UserProfileHeaderView

// https://github.com/Aliucord/aliucord/blob/d1f7b7cfdce7b4d7531e62543652cf6a65d1bc9c/Aliucord/src/main/java/com/aliucord/coreplugins/badges/DiscordBadges.kt
@AliucordPlugin
class ModernNitroIcons : Plugin() {
    private val f_badgeViewHolderBinding by lazyField<UserProfileHeaderView.BadgeViewHolder>("binding")

    @Suppress("UNCHECKED_CAST")
    override fun start(context: Context) {
        patcher.after<Badge.Companion>(
            "getBadgesForUser",
            User::class.java,
            UserProfile::class.java,
            java.lang.Boolean.TYPE,
            java.lang.Boolean.TYPE,
            Context::class.java
        ) { params ->
            val badges = params.result as? MutableList<Badge> ?: return@after
            val profile = params.args[1] as? RNUserProfile ?: return@after

            val nitroBadge = profile.badges
                ?.firstOrNull { it.id.startsWith("premium_tenure_") }
                ?: return@after

            val iconUrl = "https://cdn.discordapp.com/badge-icons/${nitroBadge.icon}.png"

            val replacement = Badge(
                0,
                null,
                nitroBadge.description,
                false,
                iconUrl
            )

            val oldIndex = badges.indexOfFirst { it.icon == 2131232057 }
            if (oldIndex >= 0) {
                badges[oldIndex] = replacement
            }
        }

        patcher.after<UserProfileHeaderView.BadgeViewHolder>(
            "bind", Badge::
            class.java
        ) { (_, badge: Badge) ->
            val url = badge.objectType

            // Check that badge is ours (has icon = 0 and url set)
            if (badge.icon != 0 || url == null) return@after

            val binding = f_badgeViewHolderBinding[this] as UserProfileHeaderBadgeBinding
            binding.b.setCacheableImage(url)
        }
    }


    override fun stop(context: Context) = patcher.unpatchAll()
}