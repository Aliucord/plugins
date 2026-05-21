package com.github.lampdelivery

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.SettingsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.after
import com.aliucord.settings.delegate
import com.aliucord.utils.ReflectUtils
import com.aliucord.widgets.BottomSheet
import com.discord.databinding.WidgetGuildProfileSheetBinding
import com.discord.views.CheckedSetting
import com.discord.widgets.guilds.profile.WidgetGuildProfileSheet
import com.discord.widgets.guilds.profile.WidgetGuildProfileSheetViewModel
import java.text.Normalizer
import java.util.WeakHashMap

@AliucordPlugin
class CleanChannels : Plugin() {

    private val guardedViews = WeakHashMap<TextView, Boolean>()
    
    private var hideSymbols by settings.delegate(true)
    private var capitalizeCategories by settings.delegate(true)
    private var removeEmojis by settings.delegate(true)
    private var normalizeLetters by settings.delegate(true)
    private var showWhitelistToggle by settings.delegate(true)
    private val whitelist by settings.delegate(mutableSetOf<Long>())

    private var categoryId = 0
    private var inputId = 0
    private val targetIds = mutableSetOf<Int>()

    init {
        settingsTab = SettingsTab(PluginSettings::class.java, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings)
    }

    override fun start(context: Context) {
        categoryId = getResId("channels_item_category_name")
        inputId = getResId("text_input")

        // Only target channel-specific elements, not general usernames or profiles
        val names = listOf(
            "channels_item_channel_name",
            "channels_item_voice_channel_name",
            "stage_channel_item_voice_channel_name",
            "channels_item_thread_name",
            "channels_item_category_name",
            "toolbar_title",
            "channel_topic_name",
            "channel_topic_title",
            "text_input"
        )
        names.forEach { name ->
            val id = getResId(name)
            if (id != 0) targetIds.add(id)
        }

        // Only hook Fragment views that are NOT profiles
        try {
            val appFragmentClass = Class.forName("com.discord.app.AppFragment")
            patcher.patch(appFragmentClass.getDeclaredMethod("onViewCreated", View::class.java, Bundle::class.java), Hook { cf ->
                val root = cf.args?.getOrNull(0) as? View ?: return@Hook
                val fragment = cf.thisObject

                // Skip if this is a profile-related fragment
                if (isProfileFragment(fragment)) return@Hook

                root.post { walkAndClean(root) }
            })
        } catch (_: Throwable) {}

        // Skip AppBottomSheet entirely since profiles use bottom sheets
        // Removed AppBottomSheet hook to avoid cleaning profiles

        try {
            val setTextHook = Hook { param ->
                val tv = param.thisObject as? TextView ?: return@Hook

                // Only apply to specific channel-related IDs, not general usernames
                if (targetIds.contains(tv.id)) {
                    val text = param.args[0]?.toString() ?: ""
                    if (isUsernameFormat(text)) return@Hook
                    if (isInProfileContext(tv)) return@Hook

                    applyCleaningSafely(tv)
                    attachGuard(tv)
                }
            }
            TextView::class.java.declaredMethods.filter { it.name == "setText" }.forEach { method ->
                if (method.parameterTypes.isNotEmpty() && CharSequence::class.java.isAssignableFrom(method.parameterTypes[0])) {
                    patcher.patch(method, setTextHook)
                }
            }
        } catch (_: Throwable) {}

        try {
            val draweeTextViewClass = Class.forName("com.discord.utilities.view.text.SimpleDraweeSpanTextView")
            val setDraweeMethod = draweeTextViewClass.declaredMethods.find { it.name == "setDraweeSpanStringBuilder" }
            if (setDraweeMethod != null) {
                patcher.patch(setDraweeMethod, Hook { param ->
                    val tv = param.thisObject as? TextView ?: return@Hook

                    // Only apply to channel-specific IDs, skip if in profile context
                    if (targetIds.contains(tv.id) && !isInProfileContext(tv)) {
                        val builder = param.args[0] as? CharSequence ?: return@Hook
                        val original = builder.toString()
                        val cleaned = cleanName(original, tv)
                        if (cleaned != original) {
                            try {
                                val draweeBuilderClass = Class.forName("com.facebook.drawee.span.DraweeSpanStringBuilder")
                                val newnewBuilder = draweeBuilderClass.getConstructor(CharSequence::class.java).newInstance(cleaned)
                                param.args[0] = newnewBuilder
                            } catch (_: Throwable) {
                                param.args[0] = SpannableStringBuilder(cleaned)
                            }
                        }
                    }
                })
            }
        } catch (_: Throwable) {}

        // Only hook specific channel-related widgets, not user/profile widgets
        val channelOnlyClasses = listOf(
            "com.discord.widgets.channels.WidgetChannelTopic",
            "com.discord.widgets.channels.list.WidgetChannelsListItemChannelActions"
        )
        channelOnlyClasses.forEach { className ->
            try {
                val clazz = Class.forName(className)
                clazz.declaredMethods.forEach { method ->
                    if (method.name == "setActionBarTitle" && method.parameterTypes.size == 1 && method.parameterTypes[0] == CharSequence::class.java) {
                        patcher.patch(method, Hook { cf ->
                            val title = cf.args[0] as? CharSequence ?: return@Hook
                            cf.args[0] = cleanName(title, isCategory = false, isHint = false, isHeader = true, isThread = false, isVoice = false)
                        })
                    } else if (method.name == "configure" || method.name == "configureUI" || method.name == "updateUI") {
                        patcher.patch(method, Hook { cf ->
                            val obj = cf.thisObject ?: return@Hook
                            if (obj is androidx.fragment.app.Fragment) {
                                obj.view?.let { root -> root.post { walkAndClean(root) } }
                            } else {
                                findAndCleanView(obj)
                            }
                        })
                    }
                }
            } catch (_: Throwable) {}
        }

        try {
            val widgetChatInputClass = Class.forName("com.discord.widgets.chat.input.WidgetChatInput")
            val getHintMethods = widgetChatInputClass.declaredMethods.filter { it.name == "getHint" }
            getHintMethods.forEach { method ->
                patcher.patch(method, Hook { param ->
                    val res = param.result as? CharSequence ?: return@Hook
                    param.result = cleanName(res, isCategory = false, isHint = true, isHeader = false, isThread = false, isVoice = false)
                })
            }
        } catch (_: Throwable) {}

        patcher.after<WidgetGuildProfileSheet>(
            "configureTabItems",
            Long::class.java,
            WidgetGuildProfileSheetViewModel.TabItems::class.java,
            Boolean::class.java
        ) {
            if (!showWhitelistToggle) return@after
            val guildId = it.args[0] as Long
            val fragment = it.thisObject as WidgetGuildProfileSheet

            val binding = ReflectUtils.getMethodByArgs(WidgetGuildProfileSheet::class.java, "getBinding").invoke(fragment) as WidgetGuildProfileSheetBinding
            val layout = binding.f.getRootView() as ViewGroup

            val secondaryActionsId = Utils.getResId("guild_profile_sheet_secondary_actions", "id")
            val container = layout.findViewById<View>(secondaryActionsId) as? ViewGroup ?: return@after
            val actionsLayout = container.getChildAt(0) as? LinearLayout ?: return@after

            if (actionsLayout.findViewWithTag<View>("clean_channels_toggle") != null) return@after

            val context = actionsLayout.context
            val setting = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Clean Channels", null).apply {
                tag = "clean_channels_toggle"
                isChecked = !whitelist.contains(guildId)
                setOnCheckedListener { checked ->
                    if (checked) {
                        whitelist.remove(guildId)
                    } else {
                        whitelist.add(guildId)
                    }
                    settings.setObject("whitelist", whitelist)
                }
            }

            val changeNicknameId = Utils.getResId("guild_profile_sheet_change_nickname", "id")
            val changeNicknameView = actionsLayout.findViewById<View?>(changeNicknameId)
            val index = if (changeNicknameView != null) actionsLayout.indexOfChild(changeNicknameView) else 0

            actionsLayout.addView(setting, index)
        }
    }

    private fun getResId(name: String): Int {
        val id = Utils.getResId(name, "id")
        if (id != 0) return id
        val ctx = Utils.appContext ?: return 0
        var fallbackId = ctx.resources.getIdentifier(name, "id", "com.discord")
        if (fallbackId == 0) fallbackId = ctx.resources.getIdentifier(name, "id", "com.discord.app")
        return fallbackId
    }

    // Detect if fragment is profile-related to avoid cleaning usernames/profiles
    private fun isProfileFragment(fragment: Any): Boolean {
        val className = fragment.javaClass.name
        return className.contains("Profile") ||
               className.contains("User") && !className.contains("Channel") ||
               className.contains("Member") && !className.contains("List")
    }

    // Detect if TextView is in a profile context to avoid cleaning
    private fun isInProfileContext(tv: TextView): Boolean {
        var parent = tv.parent
        while (parent != null && parent is View) {
            val id = parent.id
            if (id != 0 && id != View.NO_ID) {
                try {
                    val idName = parent.resources.getResourceEntryName(id)
                    if (idName.contains("profile") ||
                        idName.contains("user") && !idName.contains("channel") ||
                        idName.contains("member") && !idName.contains("list")) {
                        return true
                    }
                } catch (_: Throwable) {}
            }

            // Check class name of parent
            val className = parent.javaClass.name
            if (className.contains("Profile") ||
                className.contains("User") && !className.contains("Channel")) {
                return true
            }

            parent = parent.parent
        }
        return false
    }

    // IMPORTANT: Detect CustomNameFormat patterns to avoid interference
    private fun isUsernameFormat(text: String): Boolean {
        // Only match specific CustomNameFormat patterns to avoid being too broad
        val usernamePatterns = listOf(
            ".*\\s+\\(.*\\)$",      // Name (username)
            ".*\\s+\\[.*\\]$",      // Name [username]
            ".*\\s+\\|\\s+.*$",     // Name | username
            ".*\\s+•\\s+.*$",       // Name • username
            ".*\\s+-\\s+.*$"        // Name - username
        )

        return try {
            usernamePatterns.any { pattern ->
                text.matches(Regex(pattern))
            } && text.length in 10..100 && text.count { it == ' ' } <= 3
        } catch (e: Exception) {
            false // If regex fails, don't treat as username format
        }
    }

    private fun findAndCleanView(obj: Any) {
        try {
            obj.javaClass.declaredFields.forEach { f ->
                if (View::class.java.isAssignableFrom(f.type)) {
                    f.isAccessible = true
                    (f.get(obj) as? View)?.let { root -> root.post { walkAndClean(root) } }
                } else if (f.name == "binding") {
                    f.isAccessible = true
                    val binding = f.get(obj)
                    binding?.javaClass?.getDeclaredMethod("getRoot")?.let { getRoot ->
                        (getRoot.invoke(binding) as? View)?.let { root -> root.post { walkAndClean(root) } }
                    }
                }
            }
        } catch (_: Throwable) {}
    }

    private fun walkAndClean(root: View) {
        if (root is TextView) {
            // Only clean specific channel-related IDs, not general usernames or profiles
            if (targetIds.contains(root.id) && !isInProfileContext(root)) {
                val text = root.text?.toString() ?: ""
                if (!isUsernameFormat(text)) {
                    applyCleaningSafely(root)
                    attachGuard(root)
                }
            }
        }
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) walkAndClean(root.getChildAt(i))
        }
    }

    private fun isHeader(tv: TextView): Boolean {
        val id = tv.id
        if (id == 0 || id == View.NO_ID) return isInsideDrawer(tv)
        return try {
            val res = tv.context?.resources ?: tv.resources ?: return false
            val idName = res.getResourceEntryName(id) ?: ""
            idName.contains("toolbar") || idName.contains("header") || idName.contains("topic") || 
            idName.contains("actions_title") || idName == "title"
        } catch (_: Throwable) { isInsideDrawer(tv) }
    }

    private fun isInsideDrawer(v: View): Boolean {
        var parent = v.parent
        while (parent != null && parent is View) {
            val id = parent.id
            if (id != 0 && id != View.NO_ID) {
                val idName = try { parent.resources.getResourceEntryName(id) } catch (_: Throwable) { "" }
                if (idName.contains("drawer") || idName.contains("panel") || idName.contains("side_bar")) return true
            }
            parent = parent.parent
        }
        return false
    }

    private fun isPoll(tv: TextView): Boolean {
        val id = tv.id
        if (id == 0 || id == View.NO_ID) return false
        return try {
            tv.resources.getResourceEntryName(id).contains("poll")
        } catch (_: Throwable) { false }
    }

    private fun isThread(tv: TextView): Boolean {
        val id = tv.id
        if (id == 0 || id == View.NO_ID) return false
        return try {
            tv.resources.getResourceEntryName(id).contains("thread")
        } catch (_: Throwable) { false }
    }

    private fun isVoice(tv: TextView): Boolean {
        val id = tv.id
        if (id == 0 || id == View.NO_ID) return false
        return try {
            tv.resources.getResourceEntryName(id).contains("voice")
        } catch (_: Throwable) { false }
    }

    private fun applyCleaningSafely(tv: TextView) {
        if (isPoll(tv)) return
        val guildId = com.discord.stores.StoreStream.getGuildSelected().selectedGuildId
        if (whitelist.contains(guildId)) return

        val id = tv.id
        val isHint = id == inputId
        val original = (if (isHint) tv.hint else tv.text) ?: return
        if (original.isEmpty()) return

        val text = original.toString()
        if (text.matches(Regex("^\\d+/\\d+$"))) return

        // IMPORTANT: Don't interfere with CustomNameFormat username patterns
        if (isUsernameFormat(text)) return

        val cleaned = cleanName(original, tv)
        if (cleaned != original.toString()) {
            if (isHint) tv.hint = cleaned else tv.text = cleaned
        }
    }

    private fun attachGuard(tv: TextView) {
        if (guardedViews.containsKey(tv)) return
        guardedViews[tv] = true

        tv.addTextChangedListener(object : TextWatcher {
            private var isRunning = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isRunning) return
                isRunning = true
                applyCleaningSafely(tv)
                isRunning = false
            }
        })
    }

    private fun isEmoji(cp: Int): Boolean {
        return (cp in 0x1F300..0x1F9FF) || (cp in 0x2600..0x27BF) || (cp in 0x1F000..0x1F0FF) || (cp in 0x1F600..0x1F64F) || (cp in 0x1F680..0x1F6FF) || (cp in 0xFE00..0xFE0F)
    }

    private fun isDash(cp: Int): Boolean {
        if (cp == '-'.code || cp == 0x2D || cp == 45) return true
        val type = Character.getType(cp).toByte()
        return type == Character.DASH_PUNCTUATION || cp == '\u2212'.code ||
               cp in 0x2010..0x2015
    }

    private fun cleanName(name: CharSequence, tv: TextView): String {
        return cleanName(
            name, 
            isCategory = tv.id == categoryId, 
            isHint = tv.id == inputId, 
            isHeader = isHeader(tv),
            isThread = isThread(tv),
            isVoice = isVoice(tv)
        )
    }

    private fun cleanName(name: CharSequence, isCategory: Boolean, isHint: Boolean, isHeader: Boolean, isThread: Boolean, isVoice: Boolean): String {
        var str = name.toString()
        if (normalizeLetters) {
            str = Normalizer.normalize(str, Normalizer.Form.NFKD).replace(Regex("\\p{M}"), "")
        }
        val cleaned = StringBuilder()
        
        var i = 0
        while (i < str.length) {
            val cp = str.codePointAt(i)
            val charCount = Character.charCount(cp)
            val c = str[i]
            
            val dash = isDash(cp)
            val emoji = isEmoji(cp)
            
            val isStrictAN = (cp in 'a'.code..'z'.code) || (cp in 'A'.code..'Z'.code) || (cp in '0'.code..'9'.code)
            
            val keep = when {
                dash -> true
                emoji -> !removeEmojis
                hideSymbols -> {
                    if (isHint || isHeader) {
                        isStrictAN || c == ' ' || c == '#' || c == '@' || dash
                    } else if (isCategory || isThread || isVoice) {
                        isStrictAN || c == ' ' || dash
                    } else {
                        isStrictAN || dash
                    }
                }
                else -> {
                    Character.isLetterOrDigit(cp) || c == ' ' || c == '_' || 
                    c == '#' || c == '.' || c == '@' || dash
                }
            }
            
            if (keep) cleaned.append(str.substring(i, i + charCount))
            i += charCount
        }
        
        var result = cleaned.toString().replace(Regex("\\s+"), " ")
        result = result.trim { it == ' ' }

        if (isCategory && capitalizeCategories && result.isNotEmpty()) {
            result = result.lowercase().replaceFirstChar { it.uppercase() }
        }
        
        return result
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    class PluginSettings(private val settings: SettingsAPI) : BottomSheet() {
        override fun onViewCreated(view: View, bundle: Bundle?) {
            super.onViewCreated(view, bundle)
            val context = view.context

            addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Remove emojis", "Toggle removal of emojis from channel names.").apply {
                isChecked = settings.getBool("removeEmojis", true)
                setOnCheckedListener { settings.setBool("removeEmojis", it) }
            })

            addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Hide symbols", "Restricts channel names to letters, digits and - only.").apply {
                isChecked = settings.getBool("hideSymbols", true)
                setOnCheckedListener { settings.setBool("hideSymbols", it) }
            })

            addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Normalize letters", "Converts special characters (like fancy script) to standard letters.").apply {
                isChecked = settings.getBool("normalizeLetters", true)
                setOnCheckedListener { settings.setBool("normalizeLetters", it) }
            })

            addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Capitalize categories", "Uniformly capitalize categories (e.g. Category).").apply {
                isChecked = settings.getBool("capitalizeCategories", true)
                setOnCheckedListener { settings.setBool("capitalizeCategories", it) }
            })

            addView(Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, "Show menu toggle", "Show the Clean Channels toggle in the server's three-dot menu.").apply {
                isChecked = settings.getBool("showWhitelistToggle", true)
                setOnCheckedListener { settings.setBool("showWhitelistToggle", it) }
            })
        }
    }
}
