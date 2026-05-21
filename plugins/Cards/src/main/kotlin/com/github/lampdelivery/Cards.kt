package com.github.lampdelivery

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.google.android.material.card.MaterialCardView
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R

@AliucordPlugin
class ContextMenuCard : Plugin() {
    override fun start(context: Context) {
        try {
            val actionsClass = Class.forName("com.discord.widgets.channels.list.WidgetChannelsListItemChannelActions")
            val configureUIMethod = actionsClass.declaredMethods.firstOrNull {
                it.name == "configureUI" && it.parameterTypes.size == 1
            } ?: throw NoSuchMethodException("configureUI with 1 param not found")
            patcher.patch(configureUIMethod, { param ->
                val getBindingMethod = actionsClass.getDeclaredMethod("getBinding").apply { isAccessible = true }
                val binding = getBindingMethod.invoke(param.thisObject)
                val doWrap = {
                    binding.javaClass.declaredFields.forEach { field ->
                        field.isAccessible = true
                        val v = field.get(binding)
                    }
                    val container = binding.javaClass.declaredFields
                        .mapNotNull { field ->
                            field.isAccessible = true
                            val v = field.get(binding)
                            if (v is ViewGroup) v else null
                        }
                        .firstOrNull()
                    val allTextViews = if (container != null) CardUtils.findVisibleTextViews(container) else emptyList()
                    val actionViews = if (allTextViews.isNotEmpty()) allTextViews.drop(1) else emptyList()
                    CardUtils.wrapActionViewsInCards(actionViews, logger)
                }
                val containerField = binding.javaClass.declaredFields
                    .mapNotNull { field ->
                        field.isAccessible = true
                        val v = field.get(binding)
                        if (v is ViewGroup) v else null
                    }
                    .firstOrNull()
                if (containerField != null) {
                    containerField.post({ doWrap() })
                } else {
                    doWrap()
                }
            })
        } catch (e: Throwable) {
            logger.error("ContextMenuCard: Failed to patch channel context menu", e)
        }

        try {
            val actionsClass = Class.forName("com.discord.widgets.chat.list.actions.WidgetChatListActions")
            val configureUIMethod = actionsClass.declaredMethods.firstOrNull {
                try {
                    it.name == "configureUI" && it.parameterTypes.size == 1 && it.parameterTypes[0].name == Class.forName("com.discord.widgets.chat.list.actions.WidgetChatListActions\$Model").name
                } catch (_: Throwable) { false }
            } ?: throw NoSuchMethodException("configureUI with 1 param not found")
            patcher.patch(configureUIMethod, { param ->
                val actions = param.thisObject
                val getViewMethod = actionsClass.getMethod("getView")
                val rootView = getViewMethod.invoke(actions) as? ViewGroup ?: run {
                    logger.warn("[ContextMenuCard] Message: getView() is not a ViewGroup!")
                    return@patch
                }
                rootView.post { CardUtils.wrapActionItemsInCards(rootView, logger) }
            })
        } catch (e: Throwable) {
            logger.error("ContextMenuCard: Failed to patch message context menu", e)
        }

        try {
            val settingsClass = Class.forName("com.discord.widgets.settings.WidgetSettings")
            val onViewBoundMethod = settingsClass.getDeclaredMethod("onViewBound", View::class.java)
            patcher.patch(onViewBoundMethod, { param ->
                val root = param.args[0] as? ViewGroup ?: return@patch
                root.post({
                    val headerViews = mutableListOf<View>()
                    fun findHeaders(view: View) {
                        if (view is android.widget.TextView && view.text?.toString()?.contains("Settings", true) == true) {
                            headerViews.add(view)
                        } else if (view is ViewGroup) {
                            for (i in 0 until view.childCount) {
                                findHeaders(view.getChildAt(i))
                            }
                        }
                    }
                    findHeaders(root)
                    val segmentHeadersRaw = listOf(
                        "User Settings", "App Settings", "Aliucord", "Developer Options"
                    )
                    val allViews = mutableListOf<View>()
                    val rootParent = (headerViews.firstOrNull()?.parent as? ViewGroup)
                    if (rootParent != null) {
                        for (i in 0 until rootParent.childCount) {
                            allViews.add(rootParent.getChildAt(i))
                        }
                    }
                    val nitroHeaderIdx = 10
                    fun norm(text: CharSequence?): String = text?.toString()?.trim()?.lowercase() ?: ""
                    val normalizedHeaders = segmentHeadersRaw.map { it.trim().lowercase() }
                    val seenHeaders = mutableSetOf<String>()
                    var seenDevOptions = false
                    val headerIndices = allViews.mapIndexedNotNull { idx, v ->
                        if (idx == nitroHeaderIdx) return@mapIndexedNotNull idx  
                        if (v is android.widget.TextView) {
                            val t = norm(v.text)
                            if (t == "settings") return@mapIndexedNotNull null
                            val headerIdx = normalizedHeaders.indexOf(t)
                            if (headerIdx != -1) {
                                if (t == "developer options") {
                                    if (!seenDevOptions) {
                                        seenDevOptions = true
                                        seenHeaders.add(t)
                                        return@mapIndexedNotNull idx
                                    } else {
                                        return@mapIndexedNotNull null
                                    }
                                } else if (!seenHeaders.contains(t)) {
                                    seenHeaders.add(t)
                                    return@mapIndexedNotNull idx
                                }
                            }
                            return@mapIndexedNotNull null
                        } else null
                    }
                    fun findNitroHeaderGroup(vg: ViewGroup?): List<View>? {
                        if (vg == null) return null
                        for (i in 0 until vg.childCount) {
                            val child = vg.getChildAt(i)
                            val idName = try { child.resources.getResourceEntryName(child.id) } catch (_: Throwable) { null }
                            if (idName == "nitro_header") {
                                val group = mutableListOf<View>()
                                for (j in i+1 until vg.childCount) {
                                    group.add(vg.getChildAt(j))
                                }
                                return group
                            }
                            if (child is ViewGroup) {
                                val found = findNitroHeaderGroup(child)
                                if (found != null) return found
                            }
                        }
                        return null
                    }
                    val nitroGroup = findNitroHeaderGroup(rootParent)
                    if (nitroGroup != null && nitroGroup.isNotEmpty()) {
                        CardUtils.wrapSettingsCategoryCards(nitroGroup, logger)
                    }
                    for (i in headerIndices.indices) {
                        val start = headerIndices[i] + 1
                        val end = if (i + 1 < headerIndices.size) headerIndices[i + 1] else allViews.size
                        val group = allViews.subList(start, end).filter { v ->
                            if (v is android.widget.TextView) {
                                val t = norm(v.text)
                                if (t in normalizedHeaders && t != "developer options") return@filter false
                                if (t == "developer options" && v != allViews[headerIndices.find { hi ->
                                    allViews[hi] is android.widget.TextView && norm((allViews[hi] as android.widget.TextView).text) == "developer options"
                                } ?: -1]) return@filter true
                                if (t == "developer options") return@filter false
                            }
                            true
                        }
                        if (group.isNotEmpty()) {
                            CardUtils.wrapSettingsCategoryCards(group, logger)
                        }
                    }

                    val pluginSettingsHeaders = mutableListOf<android.widget.TextView>()
                    fun findPluginSettingsHeader(view: View) {
                        if (view is android.widget.TextView && view.text?.toString() == "Plugin Settings") {
                            pluginSettingsHeaders.add(view)
                        } else if (view is ViewGroup) {
                            for (i in 0 until view.childCount) {
                                findPluginSettingsHeader(view.getChildAt(i))
                            }
                        }
                    }
                    findPluginSettingsHeader(root)
                    for (header in pluginSettingsHeaders) {
                        val parent = header.parent as? ViewGroup ?: continue
                        val group = mutableListOf<View>()
                        val headerIdx = parent.indexOfChild(header)
                        for (i in 1..3) {
                            val childIdx = headerIdx + i
                            if (childIdx < parent.childCount) {
                                val child = parent.getChildAt(childIdx)
                                group.add(child)
                            }
                        }
                        if (group.isNotEmpty()) CardUtils.wrapSettingsCategoryCards(group, logger)
                    }
                })
            })
        } catch (e: Throwable) {
            logger.error("ContextMenuCard: Failed to patch settings page", e)
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}

object CardUtils {
    fun findSettingsCategoryHeaders(root: ViewGroup): List<View> {
        val result = mutableListOf<View>()
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            if (child is android.widget.TextView && child.visibility == View.VISIBLE && child.isShown) {
                val isHeader = child.textSize >= 40f || child.typeface?.isBold == true
                if (isHeader) result.add(child)
            } else if (child is ViewGroup) {
                result.addAll(findSettingsCategoryHeaders(child))
            }
        }
        return result
    }

    fun wrapSettingsCategoryCards(categoryViews: List<View>, logger: com.aliucord.Logger) {
        try {
            if (categoryViews.isEmpty()) return
            val density = categoryViews[0].context.resources.displayMetrics.density
            val smallCorner = 0f
            val bigCorner = 16f * density
            val crocosmiaPad = (10 * density).toInt()
            val verticalSpacing = (1 * density).toInt()
            val groupSpacing = (10 * density).toInt()
            val sideGroupPadding = (16 * density).toInt()
            val group = categoryViews
            for ((idx, v) in group.withIndex()) {
                val parent = v.parent as? ViewGroup ?: continue
                if (parent is MaterialCardView) continue
                val card = MaterialCardView(v.context)
                card.layoutParams = v.layoutParams
                val bgResId = v.context.resources.getIdentifier("drawable_overlay_channels_selected_dark", "color", v.context.packageName)
                val bgColor = if (bgResId != 0) ColorCompat.getThemedColor(v.context, bgResId) else ColorCompat.getThemedColor(v.context, R.b.colorBackgroundTertiary)
                card.setCardBackgroundColor(bgColor)
                val verticalPad = (6 * density).toInt()
                card.setContentPadding(crocosmiaPad, verticalPad, crocosmiaPad, verticalPad)
                card.cardElevation = 0f
                card.maxCardElevation = 0f
                card.preventCornerOverlap = false
                card.useCompatPadding = false
                card.minimumHeight = 0
                val shape = card.shapeAppearanceModel.toBuilder()
                shape.setAllCorners(com.google.android.material.shape.CornerFamily.ROUNDED, smallCorner)
                if (idx == 0) {
                    shape.setTopLeftCornerSize(bigCorner)
                    shape.setTopRightCornerSize(bigCorner)
                }
                if (idx == group.lastIndex) {
                    shape.setBottomLeftCornerSize(bigCorner)
                    shape.setBottomRightCornerSize(bigCorner)
                }
                card.shapeAppearanceModel = shape.build()
                card.clipToOutline = true
                val params = card.layoutParams
                if (params is ViewGroup.MarginLayoutParams) {
                    params.topMargin = if (idx == 0) 0 else verticalSpacing
                    params.bottomMargin = 0
                    params.leftMargin = sideGroupPadding
                    params.rightMargin = sideGroupPadding
                    card.layoutParams = params
                }
                val index = parent.indexOfChild(v)
                parent.removeView(v)
                card.addView(v)
                parent.addView(card, index)
            }
        } catch (e: Throwable) {
            logger.error("[ContextMenuCard] Failed to wrap settings category items in cards (context menu style)", e)
        }
    }

    fun wrapActionItemsInCards(root: ViewGroup, logger: com.aliucord.Logger) {
        try {
            val actionViews = mutableListOf<View>()
            for (i in 0 until root.childCount) {
                val child = root.getChildAt(i)
                if (child is android.widget.ImageView) continue
                if (child.javaClass.name.contains("RecyclerView")) continue
                if (child is android.widget.TextView && child.visibility == View.VISIBLE && child.isShown) {
                    actionViews.add(child)
                } else if (child is ViewGroup) {
                    actionViews.addAll(findVisibleTextViews(child))
                }
            }
            val density = root.context.resources.displayMetrics.density
            val smallCorner = 0f
            val crocosmiaPad = (10 * density).toInt() 
            val verticalSpacing = (1 * density).toInt() 
            val groupSpacing = (10 * density).toInt() 
            val sideGroupPadding = (16 * density).toInt()
            val bigCorner = 16f * density 
            val groupSize = 4
            val groups = actionViews.chunked(groupSize)
            for ((groupIdx, group) in groups.withIndex()) {
                for ((idx, v) in group.withIndex()) {
                    val parent = v.parent as? ViewGroup ?: continue
                    if (parent is MaterialCardView) continue
                    if (v is android.widget.ImageView) continue 
                    if (isInsideRecyclerView(v)) continue 
                    val card = MaterialCardView(v.context)
                    card.layoutParams = v.layoutParams
                    val bgResId = v.context.resources.getIdentifier("drawable_overlay_channels_selected_dark", "color", v.context.packageName)
                    val bgColor = if (bgResId != 0) ColorCompat.getThemedColor(v.context, bgResId) else ColorCompat.getThemedColor(v.context, R.b.colorBackgroundTertiary)
                    card.setCardBackgroundColor(bgColor)
                    val verticalPad = (6 * density).toInt()
                    card.setContentPadding(crocosmiaPad, verticalPad, crocosmiaPad, verticalPad)
                    card.cardElevation = 0f
                    card.maxCardElevation = 0f
                    card.preventCornerOverlap = false
                    card.useCompatPadding = false
                    card.minimumHeight = 0
                    val shape = card.shapeAppearanceModel.toBuilder()
                    shape.setAllCorners(com.google.android.material.shape.CornerFamily.ROUNDED, smallCorner)
                    if (idx == 0) {
                        shape.setTopLeftCornerSize(bigCorner)
                        shape.setTopRightCornerSize(bigCorner)
                    }
                    if (idx == group.lastIndex) {
                        shape.setBottomLeftCornerSize(bigCorner)
                        shape.setBottomRightCornerSize(bigCorner)
                    }
                    card.shapeAppearanceModel = shape.build()
                    card.clipToOutline = true
                    val params = card.layoutParams
                    if (params is ViewGroup.MarginLayoutParams) {
                        params.topMargin = if (idx == 0 && groupIdx != 0) groupSpacing else if (idx == 0) 0 else verticalSpacing
                        params.bottomMargin = 0
                        params.leftMargin = sideGroupPadding
                        params.rightMargin = sideGroupPadding
                        card.layoutParams = params
                    }
                    val index = parent.indexOfChild(v)
                    parent.removeView(v)
                    card.addView(v)
                    parent.addView(card, index)
                }
            }
        } catch (e: Throwable) {
            logger.error("[ContextMenuCard] Failed to wrap action items in cards", e)
        }
    }

    fun findVisibleTextViews(root: ViewGroup): List<View> {
        val result = mutableListOf<View>()
        if (root.javaClass.name.contains("RecyclerView")) return result 
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            if (child is android.widget.TextView && child.visibility == View.VISIBLE && child.isShown && !isInsideRecyclerView(child)) {
                result.add(child)
            } else if (child is ViewGroup) {
                result.addAll(findVisibleTextViews(child))
            }
        }
        return result
    }

    fun isInsideRecyclerView(view: View): Boolean {
        var parent = view.parent
        while (parent is ViewGroup) {
            if (parent.javaClass.name.contains("RecyclerView")) return true
            parent = parent.parent
        }
        return false
    }

    fun wrapActionViewsInCards(actionViews: List<View>, logger: com.aliucord.Logger) {
        try {
            if (actionViews.isEmpty()) return
            val density = actionViews[0].context.resources.displayMetrics.density
            val smallCorner = 0f
            val crocosmiaPad = (10 * density).toInt() 
            val verticalSpacing = (1 * density).toInt() 
            val groupSpacing = (10 * density).toInt() 
            val sideGroupPadding = (16 * density).toInt() 
            val bigCorner = 16f * density 
            val groupSize = 4
            val groups = actionViews.chunked(groupSize)
            for ((groupIdx, group) in groups.withIndex()) {
                for ((idx, v) in group.withIndex()) {
                    val parent = v.parent as? ViewGroup ?: continue
                    if (parent is MaterialCardView) continue
                    if (v is android.widget.ImageView) continue
                    if (isInsideRecyclerView(v)) continue 
                    val card = MaterialCardView(v.context)
                    card.layoutParams = v.layoutParams
                    val bgResId = v.context.resources.getIdentifier("drawable_overlay_channels_selected_dark", "color", v.context.packageName)
                    val bgColor = if (bgResId != 0) ColorCompat.getThemedColor(v.context, bgResId) else ColorCompat.getThemedColor(v.context, R.b.colorBackgroundTertiary)
                    card.setCardBackgroundColor(bgColor)
                    val verticalPad = (6 * density).toInt()
                    card.setContentPadding(crocosmiaPad, verticalPad, crocosmiaPad, verticalPad)
                    card.cardElevation = 0f
                    card.maxCardElevation = 0f
                    card.preventCornerOverlap = false
                    card.useCompatPadding = false
                    card.minimumHeight = 0
                    val shape = card.shapeAppearanceModel.toBuilder()
                    shape.setAllCorners(com.google.android.material.shape.CornerFamily.ROUNDED, smallCorner)
                    if (idx == 0) {
                        shape.setTopLeftCornerSize(bigCorner)
                        shape.setTopRightCornerSize(bigCorner)
                    }
                    if (idx == group.lastIndex) {
                        shape.setBottomLeftCornerSize(bigCorner)
                        shape.setBottomRightCornerSize(bigCorner)
                    }
                    card.shapeAppearanceModel = shape.build()
                    card.clipToOutline = true
                    val params = card.layoutParams
                    if (params is ViewGroup.MarginLayoutParams) {
                        params.topMargin = if (idx == 0 && groupIdx != 0) groupSpacing else if (idx == 0) 0 else verticalSpacing
                        params.bottomMargin = 0
                        params.leftMargin = sideGroupPadding
                        params.rightMargin = sideGroupPadding
                        card.layoutParams = params
                    }
                    val index = parent.indexOfChild(v)
                    parent.removeView(v)
                    card.addView(v)
                    parent.addView(card, index)
                }
            }
        } catch (e: Throwable) {
            logger.error("[ContextMenuCard] Failed to wrap action items in cards (reflection)", e)
        }
    }

    fun nestedChildAt(root: ViewGroup, vararg indices: Int): ViewGroup? {
        var current: ViewGroup? = root
        for (idx in indices) {
            current = current?.getChildAt(idx) as? ViewGroup ?: return null
        }
        return current
    }
}