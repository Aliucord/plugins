package com.github.ushie

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ReplacementSpan
import androidx.core.graphics.ColorUtils
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.models.user.User
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.textprocessing.node.UserMentionNode
import com.github.ushie.util.AvatarUtils
import com.lytefast.flexinput.R
import java.util.concurrent.ConcurrentHashMap

@Suppress("UseKtx")
@AliucordPlugin
class ModernMentions : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    private val avatars = ConcurrentHashMap<Long, Bitmap>()
    private val fetching = ConcurrentHashMap<Long, Boolean>()

    override fun start(context: Context) {
        val users = StoreStream.getUsers()
        val guilds = StoreStream.getGuilds()
        val namePrefix = settings.getString("prefix", "@")
        val padding = settings.getInt("padding", 12)
        val avatarGap = settings.getInt("avatar_gap", 8)
        val radius = settings.getInt("radius", 12)
        val showAvatar = settings.getBool("show_avatar", true)
        val useRoleColor = settings.getBool("use_role_color", true)

        patcher.after<UserMentionNode<UserMentionNode.RenderContext>>(
            "renderUserMention", SpannableStringBuilder::class.java, UserMentionNode.RenderContext::class.java
        ) { param ->
            val builder = param.args[0] as? SpannableStringBuilder ?: return@after
            val renderContext = param.args[1] as? UserMentionNode.RenderContext ?: return@after
            val node = this as? UserMentionNode<*> ?: return@after

            try {
                val userId = node.userId
                val username = renderContext.userNames?.get(userId) ?: "invalid-user"

                val end = builder.length
                val start = end - "@$username".length
                if (start < 0) return@after

                val memberColor = guilds.getGuild(StoreStream.getGuildSelected().selectedGuildId)
                    ?.let { guilds.getMember(it.id, userId) }
                    ?.color

                val avatarBitmap = avatars[userId]
                val user = users.users[userId]
                if (showAvatar && user != null) fetchAvatar(context, user)

                val mentionText = "$namePrefix$username"

                builder.applyMentionSpan(
                    context,
                    start,
                    end,
                    mentionText,
                    memberColor,
                    avatarBitmap,
                    padding.toFloat(),
                    avatarGap.toFloat(),
                    radius.toFloat(),
                    useRoleColor
                )
            } catch (t: Throwable) {
                logger.error("Failed to render mention", t)
            }
        }
    }

    private fun fetchAvatar(context: Context, user: User) {
        if (fetching.putIfAbsent(user.id, true) != null) return

        Utils.threadPool.execute {
            try {
                val avatar = AvatarUtils(context, user).toBitmap() ?: return@execute

                avatars[user.id] = AvatarUtils.makeCircle(avatar)
            } catch (t: Throwable) {
                logger.error("Failed to fetch avatar for ${user.username}", t)
            } finally {
                fetching.remove(user.id)
            }
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        avatars.clear()
        fetching.clear()
    }
}

@SuppressLint("UseKtx")
private class MentionSpan(
    private val mentionText: String,
    private val avatar: Drawable? = null,
    private val avatarSize: Int = 0,
    private val padding: Float,
    private val avatarGap: Float,
    private val radius: Float,
    private val backgroundColor: Int? = null,
    private val textColor: Int? = null
) : ReplacementSpan() {

    private val avatarWidth
        get() =
            if (avatar != null) avatarSize + avatarGap else 0f

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val textWidth = paint.measureText(mentionText)
        return (padding + avatarWidth + textWidth + padding).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val oldColor = paint.color
        val width = padding + avatarWidth + paint.measureText(mentionText) + padding

        backgroundColor?.let {
            paint.color = it
            canvas.drawRoundRect(
                x, top.toFloat(),
                x + width, bottom.toFloat(),
                radius, radius,
                paint
            )
        }

        avatar?.let {
            val centerY = y + (paint.fontMetrics.ascent + paint.fontMetrics.descent) / 2f
            canvas.save()
            canvas.translate(x + padding, centerY - avatarSize / 2f)
            it.draw(canvas)
            canvas.restore()
        }

        textColor?.let { paint.color = it }

        canvas.drawText(
            mentionText,
            x + padding + avatarWidth,
            y.toFloat(),
            paint
        )

        paint.color = oldColor
    }
}

@Suppress("UseKtx")
private fun SpannableStringBuilder.applyMentionSpan(
    context: Context,
    start: Int,
    end: Int,
    mentionText: String,
    memberColor: Int?,
    avatar: Bitmap?,
    padding: Float,
    avatarGap: Float,
    radius: Float,
    useRoleColor: Boolean
) {
    val textColor = if (useRoleColor) {
        memberColor?.takeUnless { it == Color.BLACK } ?: Color.WHITE
    } else {
        ColorCompat.getThemedColor(
            context,
            R.b.theme_chat_mention_foreground
        )
    }

    val backgroundColor = if (useRoleColor) {
        ColorUtils.setAlphaComponent(
            ColorUtils.blendARGB(textColor, Color.BLACK, 0.65f),
            70
        )
    } else {
        ColorCompat.getThemedColor(
            context,
            R.b.theme_chat_mention_background
        )
    }

    val size = (context.resources.displayMetrics.density * 16).toInt()

    val drawable = avatar?.let {
        BitmapDrawable(
            context.resources, Bitmap.createScaledBitmap(it, size, size, true)
        ).apply {
            setBounds(0, 0, size, size)
        }
    }

    val span = MentionSpan(
        mentionText = mentionText,
        avatar = drawable,
        avatarSize = size,
        padding = padding,
        avatarGap = avatarGap,
        radius = radius,
        backgroundColor = backgroundColor,
        textColor = textColor
    )

    setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
}
