package com.github.ushie

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.utils.DimenUtils
import com.aliucord.utils.ReflectUtils
import com.aliucord.utils.RxUtils.subscribe
import com.discord.models.message.Message
import com.discord.stores.StoreSearch
import com.discord.stores.StoreStream
import com.discord.utilities.SnowflakeUtils
import com.discord.utilities.icon.IconUtils
import com.discord.utilities.images.MGImages
import com.discord.utilities.search.network.SearchFetcher
import com.discord.utilities.search.network.SearchQuery
import com.discord.utilities.time.ClockFactory
import com.discord.utilities.time.TimeUtils
import com.discord.widgets.user.usersheet.WidgetUserSheet
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel
import com.facebook.drawee.view.SimpleDraweeView
import com.lytefast.flexinput.R
import java.text.DateFormat
import java.util.concurrent.TimeUnit

@AliucordPlugin
class BetterUserDetails : Plugin() {
    private val userDetailsViewId = View.generateViewId()
    private var searchFetcher: SearchFetcher? = null

    private val timeViews = mutableListOf<Pair<TextView, Long>>()
    private var showReadableTime = false

    private val iconSize = DimenUtils.dpToPx(14)
    private val iconMarginEnd = DimenUtils.dpToPx(8)
    private val entryMarginEnd = DimenUtils.dpToPx(12)
    private val drawablePadding = DimenUtils.dpToPx(6)

    override fun start(context: Context) {
        patcher.after<WidgetUserSheet>(
            "configureNote",
            WidgetUserSheetViewModel.ViewState.Loaded::class.java
        ) { param ->
            val loaded = param.args[0] as? WidgetUserSheetViewModel.ViewState.Loaded ?: return@after
            val user = loaded.user ?: return@after

            val layout = WidgetUserSheet.`access$getBinding$p`(
                param.thisObject as WidgetUserSheet
            ).a.findViewById<LinearLayout>(
                Utils.getResId("user_sheet_content", "id")
            ) ?: return@after

            val ctx = layout.context

            if (layout.findViewById<View>(userDetailsViewId) != null) return@after

            val aboutMeCard = layout.findViewById<View>(Utils.getResId("about_me_card", "id"))
                ?: return@after

            val guildId = StoreStream.getGuildSelected().selectedGuildId
            val isGuild = guildId != 0L
            val dp = DimenUtils.defaultPadding

            layout.addView(LinearLayout(ctx).apply {
                timeViews.clear()
                showReadableTime = false
                id = userDetailsViewId
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp, dp, dp, dp)
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

                addTime(ctx, SnowflakeUtils.toTimestamp(user.id), icon = R.e.ic_tab_home)

                if (isGuild) {
                    StoreStream.getGuilds()
                        .getMember(guildId, user.id)
                        ?.joinedAt?.g()
                        ?.let {
                            addTime(
                                ctx,
                                it,
                                customIcon = {
                                    addView(SimpleDraweeView(ctx).apply {
                                        scaleType = ImageView.ScaleType.CENTER_CROP
                                        setImageURI(IconUtils.getForGuild(guildId, loaded.guildIcon, "", true, 2048))
                                        MGImages.setRoundingParams(this, 20f, false, null, null, 0f)

                                        layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
                                            marginEnd = iconMarginEnd
                                        }
                                    })
                                }
                            )
                        }
                }

                val targetId = if (isGuild) guildId else StoreStream.getChannelsSelected().id

                getLastMessageTimestamp(user.id, targetId, isGuild) {
                    it?.let { addTime(ctx, it, icon = R.e.ic_guild_list_dms_24dp) }
                }
            }, layout.indexOfChild(aboutMeCard) + 1)
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    private fun getLastMessageTimestamp(userId: Long, targetId: Long, isGuild: Boolean, onResult: (Long?) -> Unit) {
        val fetcher = searchFetcher ?: (ReflectUtils.getField(
            StoreStream.getSearch().storeSearchQuery, "searchFetcher"
        ) as SearchFetcher).also { searchFetcher = it }

        val targetType = if (isGuild) StoreSearch.SearchTarget.Type.GUILD else StoreSearch.SearchTarget.Type.CHANNEL

        fetcher.makeQuery(
            StoreSearch.SearchTarget(targetType, targetId),
            null,
            SearchQuery(mapOf("author_id" to listOf(userId.toString())), true)
        ).subscribe {
            val lastMessageDate = takeIf { it?.errorCode == null && (it?.totalResults ?: 0) > 0 }
                ?.hits?.firstOrNull()
                ?.let { Message(it).id }

            Utils.mainThread.post {
                onResult(lastMessageDate?.let(SnowflakeUtils::toTimestamp))
            }
        }
    }

    private fun LinearLayout.addTime(
        ctx: Context,
        timestamp: Long,
        icon: Int? = null,
        customIcon: (LinearLayout.() -> Unit)? = null
    ): TextView {
        customIcon?.invoke(this)

        return TextView(ctx, null, 0, R.i.UserProfile_Section_Header).apply {
            text = renderDate(ctx, timestamp)
            setPadding(0, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                marginEnd = entryMarginEnd
            }

            setOnClickListener {
                showReadableTime = !showReadableTime
                timeViews.forEach { (view, timestamp) ->
                    view.text = if (showReadableTime) {
                        toReadable(timestamp)
                    } else {
                        renderDate(ctx, timestamp)
                    }
                }
            }

            icon?.let { iconRes ->
                ContextCompat.getDrawable(ctx, iconRes)?.mutate()?.also {
                    it.setBounds(0, 0, iconSize, iconSize)
                    setCompoundDrawablesRelative(it, null, null, null)
                    TextViewCompat.setCompoundDrawableTintList(this, textColors)
                    compoundDrawablePadding = drawablePadding
                }
            }

            timeViews += this to timestamp
            addView(this)
        }
    }
}

private fun toReadable(timestamp: Long): String {
    val days = TimeUnit.DAYS.convert(
        ClockFactory.get().currentTimeMillis() - timestamp,
        TimeUnit.MILLISECONDS
    )

    return when (days) {
        0L -> "Today"
        1L -> "Yesterday"
        else -> "$days days ago"
    }
}

private fun renderDate(ctx: Context, timestamp: Long) =
    TimeUtils.INSTANCE.renderUtcDate(timestamp, ctx, DateFormat.MEDIUM)
