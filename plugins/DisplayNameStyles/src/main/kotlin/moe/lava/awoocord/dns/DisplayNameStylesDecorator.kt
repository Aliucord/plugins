@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package moe.lava.awoocord.dns

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View.OnLayoutChangeListener
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.aliucord.Logger
import com.aliucord.api.PatcherAPI
import com.aliucord.patcher.before
import com.aliucord.patcher.component1
import com.aliucord.patcher.component2
import com.aliucord.utils.ViewUtils.findViewById
import com.aliucord.utils.accessField
import com.aliucord.wrappers.users.displayNameStyles
import com.discord.api.user.DisplayNameStyle
import com.discord.databinding.WidgetChannelMembersListItemUserBinding
import com.discord.models.user.User
import com.discord.stores.StoreStream
import com.discord.utilities.spans.TypefaceSpanCompat
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListAdapter
import com.discord.widgets.channels.memberlist.adapter.ChannelMembersListViewHolderMember
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.user.UserNameFormatterKt
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.discord.widgets.user.profile.UserProfileHeaderViewModel
import java.util.WeakHashMap

internal val logger = Logger("DisplayNameStyles")

private val ChannelMembersListViewHolderMember.binding by accessField<WidgetChannelMembersListItemUserBinding>()

internal class DisplayNameStylesDecorator() : Decorator() {
    private val defaultTypeface = WeakHashMap<TextView, Typeface>()
    private val listeners = WeakHashMap<TextView, OnLayoutChangeListener>()

    private fun configureOn(view: TextView, styleData: DisplayNameStyle?, applyEffect: Boolean) {
        defaultTypeface[view]?.let { view.typeface = it }
        listeners.remove(view)?.let { view.removeOnLayoutChangeListener(it) }
        view.paint.shader = null

        if (styleData == null)
            return;

        val font = FontStyle.from(styleData.fontId)
        val effect = EffectStyle.from(styleData.effectId)

        font.url?.let {
            FontHandler.fetch(font) {
                if (!defaultTypeface.contains(view))
                    defaultTypeface[view] = view.typeface
                view.typeface = it
            }
        }

        if (applyEffect) {
            when (effect) {
                EffectStyle.Solid -> {
                    ColorUtils.setAlphaComponent(styleData.colors[0], 255)
                        .let { view.setTextColor(it) }
                }
                EffectStyle.Toon,
                EffectStyle.Pop,
                EffectStyle.Neon -> {} // TODO
                EffectStyle.Glow,
                EffectStyle.Gradient -> {
                    val (from, to) = styleData.colors.map { (it + 0xFF000000).toInt() }
                    val list = OnLayoutChangeListener { v, left, top, right, bottom, _, _, _, _ ->
                        if (v !is TextView)
                            return@OnLayoutChangeListener
                        // For some reason the text opacity is 50% in profile header, so set it to a solid colour for 100%
                        v.setTextColor(Color.BLACK)
                        v.paint.shader = LinearGradient(
                            /* x0 */ 0f,
                            /* y0 */ 0f,
                            /* x1 */ right.toFloat() - left.toFloat(),
                            /* y1 */ bottom.toFloat() - top.toFloat(),
                            /* colorFrom */ from,
                            /* colorTo */ to,
                            Shader.TileMode.REPEAT
                        )
                    }
                    view.addOnLayoutChangeListener(list)
                    listeners[view] = list
                }
            }
        }
    }

    override fun onMembersListConfigure(
        holder: ChannelMembersListViewHolderMember,
        item: ChannelMembersListAdapter.Item.Member,
        adapter: ChannelMembersListAdapter
    ) {
        val usernameView = holder.binding.f
        val usernameTextView = usernameView.j.c
        val data = StoreStream.getUsers().users[item.userId]?.displayNameStyles
        configureOn(usernameTextView, data, false)
    }

    override fun onProfileHeaderConfigure(
        view: UserProfileHeaderView,
        state: UserProfileHeaderViewModel.ViewState.Loaded
    ) {
        val binding = UserProfileHeaderView.`access$getBinding$p`(view)
        val usernameView = binding.j
        val usernameTextView = usernameView.j.c
        state.user.displayNameStyles?.let {
            logger.info("${state.user.username}: $it")
        }
        configureOn(usernameTextView, state.user.displayNameStyles, true)
    }

    override fun onMessageConfigure(holder: WidgetChatListAdapterItemMessage, entry: MessageEntry) {
        val username = holder.itemView.findViewById<TextView?>("chat_list_adapter_item_text_name")
            ?: return
        configureOn(username, entry.message.author.displayNameStyles, false)
    }

    override fun patch(patcher: PatcherAPI) {
        logger.info("hi! patching now...")

        // Patches reply preview
        patcher.before<WidgetChatListAdapterItemMessage>(
            "configureReplyPreview",
            MessageEntry::class.java,
        ) { (_, entry: MessageEntry) ->
            val referencedAuthor = entry.message.referencedMessage?.e()

            val replyUsername = itemView.findViewById<TextView?>("chat_list_adapter_item_text_decorator_reply_name")
                ?: return@before
            configureOn(replyUsername, referencedAuthor?.displayNameStyles, false)
        }

        // Remove the custom typeface from profile if the user has display name styles, so it displays properly
        patcher.patch(
            UserNameFormatterKt::class.java.declaredMethods.first { it.name == "getSpannableForUserNameWithDiscrim" }
        ) { (param, user: User) ->
            val styles = user.displayNameStyles
                ?: return@patch

            val res = param.result as SpannableStringBuilder
            val colorSpan = res.getSpans(0, res.length, ForegroundColorSpan::class.java)
            val typefaceSpan = res.getSpans(0, res.length, TypefaceSpanCompat::class.java)
            if (styles.colors.isNotEmpty()) {
                colorSpan.getOrNull(0)?.let { res.removeSpan(it) }
            }
            if (styles.fontId != 11) {
                typefaceSpan.getOrNull(0)?.let { res.removeSpan(it) }
            }
        }
    }
}
