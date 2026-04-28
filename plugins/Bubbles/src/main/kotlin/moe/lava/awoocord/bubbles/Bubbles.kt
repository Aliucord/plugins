package moe.lava.awoocord.bubbles

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import com.aliucord.PluginManager
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.utils.ViewUtils.findViewById
import com.aliucord.utils.accessField
import com.discord.databinding.WidgetChatListAdapterItemBotComponentRowBinding
import com.discord.databinding.WidgetChatListAdapterItemEmbedBinding
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.display.DisplayUtils
import com.discord.utilities.embed.EmbedResourceUtils
import com.discord.widgets.chat.list.adapter.*
import com.discord.widgets.chat.list.entries.*
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.lytefast.flexinput.R
import de.robv.android.xposed.XC_MethodHook
import java.util.WeakHashMap
import kotlin.math.min

private val padding get() = 12.dp
//private val topPad get() = 14.dp
private val topPad get() = 6.dp
private val bigCorner get() = 24.dp.toFloat()
private val smallCorner get() = 4.dp.toFloat()
private val ChatListEntry.connectBefore get() = this.type in arrayOf(
    ChatListEntry.MESSAGE_MINIMAL,
    ChatListEntry.MESSAGE_EMBED,
    ChatListEntry.MESSAGE_ATTACHMENT,
    ChatListEntry.STICKER,
    ChatListEntry.BOT_UI_COMPONENT,
    101,
)
private val ChatListEntry.excepted get() = this.type in arrayOf(
    ChatListEntry.REACTIONS,
)

private val WidgetChatListAdapterItemBotComponentRow.binding by accessField<WidgetChatListAdapterItemBotComponentRowBinding>()
private val WidgetChatListAdapterItemAttachment.binding get() = WidgetChatListAdapterItemAttachment.`access$getBinding$p`(this)
private val WidgetChatListAdapterItemEmbed.binding by accessField<WidgetChatListAdapterItemEmbedBinding>()
private val WidgetChatListAdapterItemSticker.binding get() = WidgetChatListAdapterItemSticker.`access$getBinding$p`(this)

private var MessageEntry.keyField by accessField<String>()

private val fullId = Utils.getResId("widget_chat_list_adapter_item_text", "layout")
private val minimalId = Utils.getResId("widget_chat_list_adapter_item_minimal", "layout")
private val bubbleId = View.generateViewId()

private const val messageLayoutTag = R.f.message // Just some random id

@Suppress("UNUSED")
@AliucordPlugin
class Bubbles : Plugin() {
    private fun createBubble(context: Context, parentHandler: View? = null): MaterialCardView {
        return MaterialCardView(context).apply {
            id = bubbleId
            setCardBackgroundColor(
                ColorCompat.getThemedColor(
                    this,
                    R.b.colorBackgroundSecondary
                )
            )
            parentHandler?.let { parent ->
                setOnClickListener { parent.performClick() }
                setOnLongClickListener { parent.performLongClick() }
            }
            elevation = 0f
        }
    }

    private fun WidgetChatListItem.configBubble(entry: ChatListEntry) {
        itemView.findViewById<MaterialCardView>(bubbleId)?.let {
            configBubble(it, entry)
        }
    }

    private fun WidgetChatListItem.configBubble(view: MaterialCardView, entry: ChatListEntry) {
        val idx = adapter.data.list.indexOf(entry)
        val previousEntry = adapter.data.list.getOrNull(idx + 1)
        val nextEntry = if (idx < 1) null else adapter.data.list[idx - 1]
        view.shapeAppearanceModel = view.shapeAppearanceModel.toBuilder().run {
            setAllCorners(CornerFamily.ROUNDED, bigCorner)
            if (entry.connectBefore && previousEntry?.excepted != true) {
                setTopLeftCornerSize(smallCorner)
                setTopRightCornerSize(smallCorner)
            }
            if (nextEntry?.connectBefore == true) {
                setBottomLeftCornerSize(smallCorner)
                setBottomRightCornerSize(smallCorner)
            }
            build()
        }
        view.clipToOutline = true
    }

    override fun load(context: Context) {
        hasCompactMode = PluginManager.isPluginEnabled("CompactMode")
        hasHighlightMessages = PluginManager.isPluginEnabled("HighlightOwnMessages")
        if (hasCompactMode) {
            logger.info("Enabling compatibility with CompactMode")
            compactCompatOverride = SettingsAPI("CompactMode").getInt("contentMargin", 8)
        }
    }

    private fun compatHighlightMessages() {
        val cls = try {
            val cl = PluginManager.plugins["HighlightOwnMessages"]!!.javaClass
            val loader = cl.classLoader!!
            loader.loadClass(
                $$$"cloudburst.plugins.highlightownmessages.HighlightOwnMessages$$ExternalSyntheticLambda0"
            )
        } catch(e: Throwable) {
            logger.warn("Tried to enable compatibility with HighlightOwnMessages, but no lambda class found", e)
            return
        }
        logger.info("Enabling compatibility with HighlightOwnMessages")
        val method = cls.getDeclaredMethod("call", Object::class.java)
        patcher.patch(method) { mparam ->
            val param = mparam.args[0] as XC_MethodHook.MethodHookParam
            val self = param.thisObject as? WidgetChatListAdapterItemMessage
                ?: return@patch logger.warn("Failed to cast thisObject (found: ${param.thisObject.javaClass.name})")
            self.run {
                val isFull = itemView.getTag(messageLayoutTag) as? Boolean
                    ?: return@patch
                itemView.findViewById<View>("chat_list_adapter_item_text").apply {
                    layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                        if (isFull) {
                            setPadding(padding, 0, padding, padding)
                        } else {
                            setPadding(padding, padding + 2.dp, padding, padding)
                        }
                    }
                }
            }
        }
    }

    override fun stop(context: Context) { patcher.unpatchAll() }

    var hasCompactMode = false
    var compactCompatOverride: Int? = null
    var hasHighlightMessages = false

    override fun start(context: Context) {
        patcher.after<WidgetChatListAdapter>(
            "setData",
            WidgetChatListAdapter.Data::class.java,
        ) {
            notifyItemChanged(1, Unit.a)
        }

        patcher.after<WidgetChatListAdapterItemEmbed>(
            WidgetChatListAdapter::class.java,
        ) {
            binding.a.layoutParams = binding.a.layoutParams.apply {
                width = MATCH_PARENT
            }
            (binding.f.getChildAt(0) as? ConstraintLayout)?.run {
                layoutParams = (layoutParams as FrameLayout.LayoutParams).apply {
                    width = WRAP_CONTENT
                }
            }
            binding.f.setPadding(padding, padding, padding, padding)
            binding.f.layoutParams = (binding.f.layoutParams as ConstraintLayout.LayoutParams).apply {
                marginEnd = binding.f.resources.getDimension(R.d.chat_cell_horizontal_spacing_total).toInt()
            }
        }

        patcher.instead<EmbedResourceUtils>(
            "computeMaximumImageWidthPx",
            Context::class.java,
        ) { (_, context: Context) ->
            val res = context.resources
            val screenWidth = DisplayUtils.getScreenSize(context).width()
            val space = res.getDimensionPixelSize(R.d.uikit_guideline_chat) + res.getDimensionPixelSize(R.d.chat_cell_horizontal_spacing_total) + padding * 2
            return@instead min(1440, screenWidth - space);
        }

        patchEmbed()
        patchAttachmentInit()
        patchAttachmentConfig()
        patchComponentsConfig()
        patchMessageInit()
        patchMessageConfig()
        patchStickerInit()
        patchStickerConfig()
        patchPollConfig()

        if (hasHighlightMessages) {
            compatHighlightMessages()
        }
    }

    private fun patchAttachmentConfig() {
        patcher.after<WidgetChatListAdapterItemAttachment>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChatListEntry::class.java,
        ) { (_, _: Int, entry: AttachmentEntry) ->
            configBubble(entry)
        }
    }

    private fun patchAttachmentInit() {
        patcher.after<WidgetChatListAdapterItemAttachment>(
            WidgetChatListAdapter::class.java,
        ) {
            val mediaView = binding.h
            mediaView.layoutParams =
                (mediaView.layoutParams as ConstraintLayout.LayoutParams).apply {
                    topMargin = padding
                    bottomMargin = padding
                    marginStart = padding
                    marginEnd = padding
                }
            itemView.layoutParams = (itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                bottomMargin = 2.dp
            }

            binding.d.radius = 0f
            binding.d.elevation = 0f
            binding.d.strokeWidth = 0
            binding.d.setCardBackgroundColor(Color.TRANSPARENT)

            createBubble(itemView.context, binding.a).addTo(itemView as ConstraintLayout, 1) {
                layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                    startToStart = PARENT_ID
                    topToTop = PARENT_ID
                    bottomToBottom = PARENT_ID
                    endToEnd = PARENT_ID
                    marginStart = compactCompatOverride?.dp
                        ?: resources.getDimension(R.d.uikit_guideline_chat).toInt()
                    marginEnd = resources.getDimension(R.d.chat_cell_horizontal_spacing_total).toInt()
                }
            }
        }
    }

    private val marked = WeakHashMap<LinearLayout, Unit>()
    private fun patchComponentsConfig() {
        patcher.after<WidgetChatListAdapterItemBotComponentRow>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChatListEntry::class.java,
        ) { (_, _: Int, entry: BotUiComponentEntry) ->
            var i = 0
            val layout = binding.b
            layout.layoutParams = (layout.layoutParams as ConstraintLayout.LayoutParams).apply {
                marginEnd = layout.resources.getDimension(R.d.chat_cell_horizontal_spacing_total).toInt()
            }
            while (i < layout.childCount) {
                val child = layout.getChildAt(i)
                    ?: break
                val bubble: MaterialCardView
                if (child.javaClass.simpleName == "ContainerComponentView") {
                    bubble = (child as? ConstraintLayout)?.getChildAt(0) as? MaterialCardView
                        ?: continue
                    if (i == (layout.childCount - 1)) {
                        ((bubble.getChildAt(0) as? ConstraintLayout)?.getChildAt(1) as? LinearLayout)?.run {
                            if (!marked.contains(this)) {
                                marked[this] = Unit.a
                                setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + padding)
                            }
                        }
                    }
                } else {
                    layout.removeViewAt(i)
                    bubble = createBubble(itemView.context).addTo(layout, i) {
                        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                        child.addTo(this) {
                            layoutParams = (layoutParams as LinearLayout.LayoutParams).apply {
                                topMargin += padding
                                bottomMargin += padding
                                rightMargin += padding
                                leftMargin += padding
                            }
                        }
                    }
                    bubble.setOnClickListener {
                        adapter.eventHandler.onMessageClicked(entry.message, false)
                    }
                    bubble.setOnLongClickListener {
                        adapter.eventHandler.onMessageLongClicked(entry.message, "", false)
                        true
                    }
                }

                bubble.shapeAppearanceModel = bubble.shapeAppearanceModel.toBuilder().run {
                    setAllCorners(CornerFamily.ROUNDED, smallCorner)
                    if (i == (layout.childCount - 1)) {
                        setBottomLeftCornerSize(bigCorner)
                        setBottomRightCornerSize(bigCorner)
                    }
                    build()
                }
                bubble.clipToOutline = true
                i++
            }
        }
    }

    private fun patchEmbed() {
        patcher.after<WidgetChatListAdapterItemEmbed>(
            WidgetChatListAdapter::class.java,
        ) { (_) ->
            binding.t.layoutParams =
                (binding.t.layoutParams as ConstraintLayout.LayoutParams).apply {
                    topMargin = padding
                    bottomMargin = padding
                    marginStart = padding
                    marginEnd = padding
                }
            createBubble(itemView.context, binding.a).addTo(itemView as ConstraintLayout, 1) {
                visibility = GONE
                layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                    startToStart = PARENT_ID
                    topToTop = PARENT_ID
                    bottomToBottom = PARENT_ID
                    endToEnd = PARENT_ID
                    marginStart = compactCompatOverride?.dp
                        ?: resources.getDimension(R.d.uikit_guideline_chat).toInt()
                    marginEnd = resources.getDimension(R.d.chat_cell_horizontal_spacing_total).toInt()
                }
            }
        }
        patcher.after<WidgetChatListAdapterItemEmbed>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChatListEntry::class.java,
        ) { (_, _: Int, entry: EmbedEntry) ->
            if (EmbedResourceUtils.INSTANCE.isInlineEmbed(entry.embed)) {
                itemView.findViewById<View>(bubbleId).visibility = View.VISIBLE
                configBubble(entry)
            } else {
                itemView.findViewById<View>(bubbleId).visibility = GONE
                configBubble(binding.f, entry)
            }
        }
    }

    private fun patchMessageInit() {
        patcher.after<WidgetChatListAdapterItemMessage>(
            Int::class.javaPrimitiveType!!,
            WidgetChatListAdapter::class.java,
        ) { (_, layoutId: Int) ->
            val isFull = when (layoutId) {
                fullId -> !hasCompactMode
                minimalId -> false
                else -> return@after
            }

            itemView.layoutParams = (itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                bottomMargin = 2.dp
            }
            itemView.setTag(messageLayoutTag, isFull)
            if (isFull) {
                itemView.findViewById<View?>("chat_list_adapter_item_text_header")?.apply {
                    layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                        setPadding(
                            paddingLeft + padding,
                            paddingTop + topPad,
                            paddingRight + padding,
                            paddingBottom
                        )
                    }
                }
            }
            itemView.findViewById<View>("chat_list_adapter_item_text").apply {
                layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                    if (isFull) {
                        setPadding(padding, 0, padding, padding)
                    } else {
                        setPadding(padding, padding + 2.dp, padding, padding)
                    }
                }
            }
            createBubble(itemView.context, itemView).addTo(itemView as ConstraintLayout, 2) {
                layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                    if (isFull) {
                        startToStart = Utils.getResId("uikit_chat_guideline", "id")
                        topToTop = Utils.getResId("chat_list_adapter_item_text_header", "id")
                    } else {
                        startToStart = PARENT_ID
                        topToTop = Utils.getResId("chat_list_adapter_item_text", "id")
                        marginStart = compactCompatOverride?.dp
                            ?: resources.getDimension(R.d.uikit_guideline_chat).toInt()
                    }
                    bottomToBottom = PARENT_ID
                    endToEnd = PARENT_ID
                    marginEnd = resources.getDimension(R.d.chat_cell_horizontal_spacing_total).toInt()
                }
            }
        }
    }

    private fun patchMessageConfig() {
        patcher.after<WidgetChatListAdapterItemMessage>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChatListEntry::class.java,
        ) { (_, _: Int, entry: MessageEntry) ->
            if (entry.message.content.isNullOrEmpty()) {
                itemView.findViewById<View>("chat_list_adapter_item_text").visibility = GONE
            }
            configBubble(entry)
        }
    }

    private fun patchStickerInit() {
        patcher.after<WidgetChatListAdapterItemSticker>(
            WidgetChatListAdapter::class.java,
        ) {
            binding.b.layoutParams = (binding.b.layoutParams as FrameLayout.LayoutParams).apply {
                topMargin = padding
                bottomMargin = padding
                marginStart = padding
                marginEnd = padding
            }
            binding.a.layoutParams = binding.a.layoutParams.apply {
                width = MATCH_PARENT
            }
            binding.a.removeView(binding.b)
            createBubble(itemView.context, binding.b).addTo(binding.a, 0) {
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                binding.b.addTo(this)
            }
        }
    }
    private fun patchStickerConfig() {
        patcher.after<WidgetChatListAdapterItemSticker>(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChatListEntry::class.java,
        ) { (_, _: Int, entry: StickerEntry) ->
            configBubble(entry)
        }
    }

    private val pollClass = try {
        Class.forName("com.aliucord.coreplugins.polls.chatview.WidgetChatListAdapterItemPoll")
    } catch(_: Throwable) {
        null
    }
    private val pollField = pollClass?.getDeclaredField("pollView")?.apply { isAccessible = true }
    private fun patchPollConfig() {
        if (pollClass == null) return
        patcher.patch(pollClass.getDeclaredMethod(
            "onConfigure",
            Int::class.javaPrimitiveType!!,
            ChatListEntry::class.java,
        )) { (param, _: Int, entry: ChatListEntry) ->
            val view = pollField?.get(param.thisObject) as? MaterialCardView
            view?.let {
                (param.thisObject as WidgetChatListItem).configBubble(it, entry)
            }
        }
    }
}
