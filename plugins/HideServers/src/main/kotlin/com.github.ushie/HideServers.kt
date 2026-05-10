package com.github.ushie

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.aliucord.patcher.before
import com.aliucord.utils.lazyMethod
import com.discord.databinding.WidgetFolderContextMenuBinding
import com.discord.databinding.WidgetGuildContextMenuBinding
import com.discord.utilities.color.ColorCompat
import com.discord.widgets.guilds.contextmenu.FolderContextMenuViewModel
import com.discord.widgets.guilds.contextmenu.GuildContextMenuViewModel
import com.discord.widgets.guilds.contextmenu.WidgetFolderContextMenu
import com.discord.widgets.guilds.contextmenu.WidgetGuildContextMenu
import com.discord.widgets.guilds.list.GuildListItem
import com.discord.widgets.guilds.list.GuildListViewHolder
import com.discord.widgets.guilds.list.WidgetGuildListAdapter
import com.discord.widgets.guilds.list.WidgetGuildsListViewModel
import com.google.gson.reflect.TypeToken
import com.lytefast.flexinput.R

@AliucordPlugin
class HideServers : Plugin() {
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    private enum class VisibilityMode { HIDE, SHOW, EDIT }

    private var visibilityMode = VisibilityMode.HIDE
    private val hiddenServers = mutableSetOf<Long>()
    private val hiddenFolders = mutableSetOf<Long>()
    private val hideServerViewId = View.generateViewId()
    private val hideFolderViewId = View.generateViewId()
    private val visibilityBadgeViewId = View.generateViewId()
    private val hiddenEntryType = object : TypeToken<MutableSet<Long>>() {}.type
    private val getServerBindingMethod by lazyMethod<WidgetGuildContextMenu>("getBinding")
    private val getFolderBindingMethod by lazyMethod<WidgetFolderContextMenu>("getBinding")
    private var adapter: WidgetGuildListAdapter? = null
    private var currentItems: List<GuildListItem> = emptyList()
    private var originalItems: List<GuildListItem> = emptyList()
    private var shouldHideAfterDrop = false

    override fun start(context: Context) {
        hiddenServers += settings.getObject("hiddenServers", mutableSetOf(), hiddenEntryType)
        hiddenFolders += settings.getObject("hiddenFolders", mutableSetOf(), hiddenEntryType)

        patchSetItems()
        patchGuildBinding()
        patchVisibilityToggle()
        patchDragAndDrop()
        patchContextMenus()
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        adapter = null
        visibilityMode = VisibilityMode.HIDE
        hiddenServers.clear()
        hiddenFolders.clear()
        currentItems = emptyList()
        originalItems = emptyList()
    }

    private fun patchSetItems() {
        patcher.before<WidgetGuildListAdapter>(
            "setItems",
            List::class.java,
            Boolean::class.java
        ) { param ->
            adapter = param.thisObject as? WidgetGuildListAdapter

            @Suppress("UNCHECKED_CAST")
            val incomingItems = param.args[0] as? List<GuildListItem> ?: return@before

            if (GuildListItem.HelpItem.INSTANCE !in incomingItems) {
                originalItems = incomingItems
            }

            val shouldShowVisibilityToggle = settings.getBool("showVisibilityToggle", true)

            var items = originalItems

            if (shouldShowVisibilityToggle && GuildListItem.HelpItem.INSTANCE !in items) {
                items = items.toMutableList().apply {
                    add(lastIndex, GuildListItem.HelpItem.INSTANCE)
                }
            }

            if (visibilityMode == VisibilityMode.HIDE) {
                items = items.mapNotNull { item ->
                    when (item) {
                        is GuildListItem.GuildItem ->
                            item.takeUnless { it.guild.id in hiddenServers }

                        is GuildListItem.FolderItem ->
                            item.takeUnless { it.folderId in hiddenFolders }
                                ?.filterHiddenServersOrNull(hiddenServers)

                        else -> item
                    }
                }
            }

            currentItems = items
            param.args[0] = items
        }
    }

    private fun patchGuildBinding() {
        patcher.after<WidgetGuildListAdapter>(
            "onBindViewHolder",
            GuildListViewHolder::class.java,
            Int::class.javaPrimitiveType!!
        ) { param ->
            val holder = param.args[0] as? GuildListViewHolder ?: return@after
            val position = param.args[1] as Int
            val item = currentItems.getOrNull(position) ?: return@after

            when (item) {
                is GuildListItem.GuildItem ->
                    bindVisibilityItem(holder, item.guild.id, hiddenServers, "hiddenServers")

                is GuildListItem.FolderItem ->
                    bindVisibilityItem(holder, item.folderId, hiddenFolders, "hiddenFolders", longClick = true)

                GuildListItem.HelpItem.INSTANCE -> {
                    val icon = holder.itemView.findViewById<ImageView>(
                        Utils.getResId("guilds_item_profile_avatar", "id")
                    ) ?: return@after

                    icon.setImageResource(getVisibilityIcon(mode = visibilityMode))
                    icon.imageTintList = ColorStateList.valueOf(
                        ColorCompat.getThemedColor(icon.context, R.b.colorInteractiveNormal)
                    )
                }
            }
        }
    }

    private fun patchVisibilityToggle() {
        patcher.before<WidgetGuildsListViewModel>(
            "onItemClicked",
            GuildListItem::class.java,
            Context::class.java,
            FragmentManager::class.java
        ) { param ->
            if (param.args[0] != GuildListItem.HelpItem.INSTANCE) return@before
            param.result = null

            visibilityMode = when (visibilityMode) {
                VisibilityMode.HIDE -> VisibilityMode.SHOW
                VisibilityMode.SHOW -> VisibilityMode.EDIT
                VisibilityMode.EDIT -> VisibilityMode.HIDE
            }

            refreshList()
        }
    }

    private fun patchDragAndDrop() {
        patcher.before<WidgetGuildListAdapter>(
            "onDragStarted",
            RecyclerView.ViewHolder::class.java
        ) {
            if (visibilityMode != VisibilityMode.HIDE) return@before

            visibilityMode = VisibilityMode.SHOW
            shouldHideAfterDrop = true
            refreshList()
        }

        patcher.after<WidgetGuildListAdapter>("onDrop") {
            if (!shouldHideAfterDrop) return@after

            Utils.mainThread.postDelayed({
                if (visibilityMode == VisibilityMode.SHOW) {
                    visibilityMode = VisibilityMode.HIDE
                    refreshList()
                }

                shouldHideAfterDrop = false
            }, 2_000)
        }
    }

    private fun patchContextMenus() {
        patcher.after<WidgetGuildContextMenu>(
            "configureUI",
            GuildContextMenuViewModel.ViewState::class.java
        ) { param ->
            val state = param.args[0] as? GuildContextMenuViewModel.ViewState.Valid ?: return@after
            val binding = getServerBindingMethod.invoke(param.thisObject) as? WidgetGuildContextMenuBinding
                ?: return@after
            val layout = binding.e.parent as? LinearLayout ?: return@after
            val guild = state.guild

            if (layout.findViewById<TextView>(hideServerViewId) != null) return@after

            createHideOption(
                layout = layout,
                id = hideServerViewId,
                layoutParams = binding.e.layoutParams,
                targetId = guild.id,
                hiddenSet = hiddenServers,
                settingsKey = "hiddenServers",
                label = "Server"
            )
        }

        patcher.after<WidgetFolderContextMenu>(
            "configureUI",
            FolderContextMenuViewModel.ViewState::class.java
        ) { param ->
            val state = param.args[0] as? FolderContextMenuViewModel.ViewState.Valid ?: return@after
            val folderId = state.folder.id ?: return@after
            val binding = getFolderBindingMethod.invoke(param.thisObject) as? WidgetFolderContextMenuBinding
                ?: return@after
            val layout = binding.c.parent as? LinearLayout ?: return@after

            if (layout.findViewById<TextView>(hideFolderViewId) != null) return@after

            createHideOption(
                layout = layout,
                id = hideFolderViewId,
                layoutParams = binding.d.layoutParams,
                targetId = folderId,
                hiddenSet = hiddenFolders,
                settingsKey = "hiddenFolders",
                label = "Folder"
            )
        }
    }

    private fun bindVisibilityItem(
        holder: GuildListViewHolder,
        id: Long,
        hiddenSet: MutableSet<Long>,
        settingsKey: String,
        longClick: Boolean = false
    ) {
        if (visibilityMode == VisibilityMode.EDIT) {
            if (longClick) {
                holder.itemView.setOnLongClickListener {
                    toggleAndSave(id, hiddenSet, settingsKey)
                    true
                }
            } else {
                holder.itemView.setOnClickListener {
                    toggleAndSave(id, hiddenSet, settingsKey)
                }
            }
        }

        addHiddenBadge(
            holder.itemView,
            isHidden = id in hiddenSet,
            show = visibilityMode == VisibilityMode.EDIT && id in hiddenSet
        )
    }

    private fun addHiddenBadge(root: View, isHidden: Boolean, show: Boolean) {
        val container = root.findViewById<FrameLayout>(
            Utils.getResId("guilds_item_avatar_wrap", "id")
        ) ?: root.findViewById(
            Utils.getResId("guilds_item_folder_container", "id")
        ) ?: return

        container.findViewById<FrameLayout>(visibilityBadgeViewId)?.let { overlay ->
            overlay.fadeVisibility(show)
            val icon = overlay.getChildAt(0) as? ImageView
            icon?.setImageResource(getVisibilityIcon(isHidden))

            return
        }

        container.addView(FrameLayout(container.context).apply {
            id = visibilityBadgeViewId
            visibility = if (show) View.VISIBLE else View.GONE
            setBackgroundColor(0x99000000.toInt())

            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            addView(ImageView(context).apply {
                setImageResource(getVisibilityIcon(isHidden))
                imageTintList = ColorStateList.valueOf(Color.WHITE)

                layoutParams = FrameLayout.LayoutParams(
                    20.dp(container),
                    20.dp(container),
                    Gravity.CENTER
                )
            })

            bringToFront()
        })
    }

    @SuppressLint("SetTextI18n")
    private fun createHideOption(
        layout: LinearLayout,
        id: Int,
        layoutParams: ViewGroup.LayoutParams,
        targetId: Long,
        hiddenSet: MutableSet<Long>,
        settingsKey: String,
        label: String
    ) {
        val isHidden = targetId in hiddenSet

        layout.addView(TextView(layout.context, null, 0, R.i.ContextMenuTextOption).apply {
            this.id = id
            this.layoutParams = layoutParams
            text = if (isHidden) "Unhide $label" else "Hide $label"
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                AppCompatResources.getDrawable(
                    layout.context,
                    getVisibilityIcon(isHidden)
                )?.tinted(layout.context),
                null, null, null
            )
            setOnClickListener {
                toggleAndSave(targetId, hiddenSet, settingsKey)
                layout.visibility = View.GONE
            }
        })
    }

    private fun getVisibilityIcon(
        isHidden: Boolean? = null,
        mode: VisibilityMode? = null
    ): Int {
        mode?.let {
            return when (it) {
                VisibilityMode.HIDE -> R.e.design_ic_visibility_off
                VisibilityMode.SHOW -> R.e.design_ic_visibility
                VisibilityMode.EDIT -> R.e.ic_edit_24dp
            }
        }

        return if (isHidden == true) {
            R.e.design_ic_visibility_off
        } else {
            R.e.design_ic_visibility
        }
    }

    private fun toggleAndSave(id: Long, set: MutableSet<Long>, key: String) {
        if (!set.add(id)) set.remove(id)
        settings.setObject(key, set)
        refreshList()
    }

    private fun refreshList() {
        Utils.mainThread.post {
            adapter?.setItems(originalItems, false)
        }
    }
}

private fun View.fadeVisibility(show: Boolean) {
    if ((visibility == View.VISIBLE) == show) {
        animate().cancel()
        alpha = if (show) 1f else 0f
        return
    }

    animate().cancel()

    if (show) {
        alpha = 0f
        visibility = View.VISIBLE
    }

    animate()
        .alpha(if (show) 1f else 0f)
        .setDuration(150)
        .withEndAction {
            visibility = if (show) View.VISIBLE else View.GONE
        }
        .start()
}

private fun GuildListItem.FolderItem.filterHiddenServersOrNull(
    hiddenServers: Set<Long>
): GuildListItem.FolderItem? {
    val visibleGuilds = guilds.filterNot { it.id in hiddenServers }
    if (visibleGuilds.isEmpty()) return null
    return copy(
        folderId, color, name, isOpen, visibleGuilds, isAnyGuildSelected,
        isAnyGuildConnectedToVoice, isAnyGuildConnectedToStageChannel,
        mentionCount, isUnread, isTargetedForFolderAddition
    )
}

private fun Drawable.tinted(context: Context): Drawable = mutate().apply {
    setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal))
}

private fun Int.dp(view: View): Int =
    (this * view.resources.displayMetrics.density).toInt()
