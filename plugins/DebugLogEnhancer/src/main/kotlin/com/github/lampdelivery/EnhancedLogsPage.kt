package com.github.lampdelivery

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.fragments.SettingsPage
import com.aliucord.views.TextInput
import com.aliucord.views.Divider
import com.aliucord.utils.DimenUtils
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EnhancedLogsPage(private val settings: com.aliucord.api.SettingsAPI) : SettingsPage() {
    private val logs = mutableListOf<Any>() 
    private var logsSubscription: Any? = null
    private lateinit var adapter: LogsAdapter
    private var selectedPriority: Int? = null 
    private var tagQuery: String = ""
    private var currentQuery = ""
    private var filtersVisible: Boolean = true
    private val settingsKeyFiltersVisible = "enhancedLogsFiltersVisible"

    override fun onViewBound(view: View) {
        setActionBarTitle("Debug Logs")
        setActionBarSubtitle(null)
        filtersVisible = settings.getBool(settingsKeyFiltersVisible, true)
        super.onViewBound(view)
        val safeContext = context ?: throw IllegalStateException("Context is null")

        val filterContainer = LinearLayout(safeContext).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (8 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
            val bg = android.graphics.drawable.GradientDrawable()
            bg.cornerRadius = 16f * resources.displayMetrics.density
            bg.setColor(com.discord.utilities.color.ColorCompat.getThemedColor(safeContext, com.lytefast.flexinput.R.b.colorBackgroundTertiary))
            background = bg
        }
        val filterRow = LinearLayout(safeContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val priorities = listOf(
            Triple("All", null, com.discord.utilities.color.ColorCompat.getThemedColor(safeContext, com.lytefast.flexinput.R.b.colorInteractiveNormal)),
            Triple("Error", 5, com.discord.utilities.color.ColorCompat.getThemedColor(safeContext, com.lytefast.flexinput.R.b.colorStatusDanger)),
            Triple("Warn", 4, com.discord.utilities.color.ColorCompat.getThemedColor(safeContext, com.lytefast.flexinput.R.b.colorStatusWarning)),
            Triple("Info", 3, com.discord.utilities.color.ColorCompat.getThemedColor(safeContext, com.lytefast.flexinput.R.b.colorInteractiveNormal)),
            Triple("Debug", 2, com.discord.utilities.color.ColorCompat.getThemedColor(safeContext, com.lytefast.flexinput.R.b.colorInteractiveMuted))
        )
        val chipParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { rightMargin = (8 * resources.displayMetrics.density).toInt() }
        priorities.forEach { (label, prio, color) ->
            val btn = TextView(safeContext).apply {
                text = label
                setPadding(0, 16, 0, 16)
                setTextColor(color)
                gravity = Gravity.CENTER
                val attrs = intArrayOf(android.R.attr.selectableItemBackground)
                val typedArray = safeContext.obtainStyledAttributes(attrs)
                val backgroundRes = typedArray.getResourceId(0, 0)
                if (backgroundRes != 0) setBackgroundResource(backgroundRes)
                typedArray.recycle()
                setOnClickListener {
                    selectedPriority = prio
                    for (i in 0 until filterRow.childCount) {
                        val v = filterRow.getChildAt(i)
                        v.isSelected = (v == this)
                        v.alpha = if (v == this) 1f else 0.6f
                    }
                    adapter.filter(currentQuery, selectedPriority, "")
                }
            }
            btn.isSelected = prio == null
            btn.alpha = if (prio == null) 1f else 0.6f
            filterRow.addView(btn, chipParams)
        }
        filterContainer.addView(filterRow)
        addView(filterContainer)

        val spacing = android.widget.Space(safeContext)
        spacing.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (12 * resources.displayMetrics.density).toInt())
        addView(spacing)

        val input = TextInput(safeContext)
        input.setHint("Search logs")
        val editText = input.getEditText()
        editText.setMaxLines(1)
        addView(input)

        val padding = DimenUtils.defaultPadding
        val recyclerView = RecyclerView(safeContext)
        val layoutManager = LinearLayoutManager(safeContext, RecyclerView.VERTICAL, false)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerView.setPadding(0, padding, 0, 0)
        adapter = LogsAdapter(logs, onLogAction = { logText, action ->
            when (action) {
                "copy" -> {
                    val clipboard = safeContext.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Log Entry", logText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(safeContext, "Copied log entry", Toast.LENGTH_SHORT).show()
                }
                "share" -> {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, logText)
                    }
                    safeContext.startActivity(android.content.Intent.createChooser(intent, "Share log entry"))
                }
                "details" -> {
                    val themedBg = com.discord.utilities.color.ColorCompat.getThemedColor(safeContext, com.lytefast.flexinput.R.b.colorBackgroundFloating)
                    val themedText = com.discord.utilities.color.ColorCompat.getThemedColor(safeContext, com.lytefast.flexinput.R.b.colorHeaderPrimary)
                    val font = try { androidx.core.content.res.ResourcesCompat.getFont(safeContext, com.aliucord.Constants.Fonts.whitney_medium) } catch (_: Throwable) { null }
                    val titleView = TextView(safeContext).apply {
                        text = "Log Details"
                        setTextColor(themedText)
                        if (font != null) typeface = font
                        textSize = 18f
                        setPadding((24 * resources.displayMetrics.density).toInt(), (18 * resources.displayMetrics.density).toInt(), (24 * resources.displayMetrics.density).toInt(), (8 * resources.displayMetrics.density).toInt())
                        setBackgroundColor(themedBg)
                    }
                    val builder = androidx.appcompat.app.AlertDialog.Builder(safeContext)
                        .setCustomTitle(titleView)
                        .setMessage(logText)
                        .setPositiveButton("OK", null)
                    val dialog = builder.create()
                    dialog.setOnShowListener {
                        try {
                            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(themedBg))
                            dialog.findViewById<TextView>(android.R.id.message)?.let { msgView ->
                                msgView.setTextColor(themedText)
                                if (font != null) msgView.typeface = font
                                msgView.textSize = 15f
                                msgView.setBackgroundColor(themedBg)
                            }
                        } catch (_: Throwable) {}
                    }
                    dialog.show()
                }
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        addView(recyclerView)

        addView(Divider(safeContext))

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString() ?: ""
                currentQuery = q
                adapter.filter(currentQuery, selectedPriority, tagQuery)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        try {
            val appLogClass = Class.forName("com.discord.app.AppLog")
            val dField = appLogClass.getDeclaredField("d")
            dField.isAccessible = true
            val logsSubject = dField.get(null)
            if (logsSubject != null) {
                val vMethod = logsSubject.javaClass.methods.find {
                    it.name == "V" && it.parameterTypes.size == 1 && it.parameterTypes[0].name.contains("Action1")
                }
                if (vMethod != null) {
                    val action1Class = Class.forName("rx.functions.Action1")
                    val onNext = java.lang.reflect.Proxy.newProxyInstance(
                        action1Class.classLoader,
                        arrayOf(action1Class)
                    ) { _, method, args ->
                        if (method.name == "call" && args != null && args.size == 1) {
                            val t = args[0]
                            if (t is List<*>) {
                                logs.clear()
                                logs.addAll(t.filterNotNull())
                                adapter.setLogs(logs)
                            } else if (t != null) {
                                logs.add(t)
                                adapter.setLogs(logs)
                            }
                        }
                        null
                    }
                    logsSubscription = vMethod.invoke(logsSubject, onNext)
                }
            }
        } catch (e: Throwable) {
            android.util.Log.e("DebugLogEnhancer", "Failed to subscribe to logs from AppLog.d: ${e.message}", e)
        }

        filterContainer.visibility = if (filtersVisible) View.VISIBLE else View.GONE
        getHeaderBar().getMenu().add("Filters").apply {
            isCheckable = true
            isChecked = filtersVisible
            setOnMenuItemClickListener {
                filtersVisible = !isChecked
                filterContainer.visibility = if (filtersVisible) View.VISIBLE else View.GONE
                isChecked = filtersVisible
                settings.setBool(settingsKeyFiltersVisible, filtersVisible)
                true
            }
        }
        getHeaderBar().getMenu().add("Clear logs").setOnMenuItemClickListener {
            adapter.clear()
            true
        }
        getHeaderBar().getMenu().add("Export logs").setOnMenuItemClickListener {
            try {
                val logText = buildString {
                    for (item in adapter.getFilteredLogs()) {
                        append(LogsAdapter.getMessage(item))
                        append("\n")
                        val throwable = LogsAdapter.getThrowable(item)
                        if (throwable != null) {
                            append(throwable)
                            append("\n")
                        }
                    }
                }
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, logText)
                }
                safeContext.startActivity(android.content.Intent.createChooser(intent, "Export logs"))
            } catch (e: Throwable) {
                Toast.makeText(safeContext, "Failed to export logs: ${e.message}", Toast.LENGTH_LONG).show()
            }
            true
        }
    }

    fun onViewUnbound(view: View) {
        try {
            logsSubscription?.let { sub ->
                val unsubscribeMethod = sub.javaClass.methods.find { it.name == "unsubscribe" || it.name == "dispose" }
                unsubscribeMethod?.invoke(sub)
            }
        } catch (_: Throwable) {}
        settings.setBool(settingsKeyFiltersVisible, filtersVisible)
    }
}

class LogsAdapter(
    logs: List<Any>,
    private val onLogAction: ((logText: String, action: String) -> Unit)? = null
) : RecyclerView.Adapter<LogsAdapter.VH>() {
    private var useCardBg: Boolean = true
    fun setUseCardBg(enabled: Boolean) {
        useCardBg = enabled
        notifyDataSetChanged()
    }
    private var full: List<Any> = logs
    private var filtered: List<Any> = logs

    fun setLogs(newLogs: List<Any>) {
        full = newLogs
        filter("")
    }

    fun filter(q: String, prio: Int? = null, tag: String = "") {
        val low = q.trim().lowercase()
        val tagLow = tag.trim().lowercase()
        filtered = full.filter {
            (low.isEmpty() || getMessage(it).lowercase().contains(low)) &&
            (prio == null || getPriority(it) == prio) &&
            (tagLow.isEmpty() || getTags(it).any { t -> t.lowercase().contains(tagLow) })
        }
        notifyDataSetChanged()
    }

    fun clear() {
        full = emptyList()
        filtered = emptyList()
        notifyDataSetChanged()
    }

    fun getFilteredLogs(): List<Any> = filtered

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
        val ctx = parent.context
        val tv = TextView(ctx).apply {
            setPadding(8, 8, 8, 8)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            try {
                val font = androidx.core.content.res.ResourcesCompat.getFont(ctx, com.aliucord.Constants.Fonts.whitney_medium)
                if (font != null) typeface = font
            } catch (_: Throwable) {}
            textSize = 15f
        }
        val card = CardUtils.wrapInMaterialCard(ctx, tv)
        return VH(tv, card)
    }

    override fun getItemCount(): Int = filtered.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = filtered[position]
        val msg = getMessage(item)
        val prio = getPriority(item)
        val throwable = getThrowable(item)
        val sb = StringBuilder()
        sb.append("[").append(prio).append("] ").append(msg)
        if (throwable != null) sb.append("\n").append(throwable)
        val logText = sb.toString()
        holder.tv.text = logText

        val ctx = holder.tv.context
        val color = try {
            when (prio) {
                5 -> com.discord.utilities.color.ColorCompat.getThemedColor(ctx, com.lytefast.flexinput.R.b.colorStatusDanger)
                4 -> com.discord.utilities.color.ColorCompat.getThemedColor(ctx, com.lytefast.flexinput.R.b.colorStatusWarning)
                3 -> com.discord.utilities.color.ColorCompat.getThemedColor(ctx, com.lytefast.flexinput.R.b.colorInteractiveNormal)
                2 -> Color.WHITE 
                else -> com.discord.utilities.color.ColorCompat.getThemedColor(ctx, com.lytefast.flexinput.R.b.colorInteractiveNormal)
            }
        } catch (_: Throwable) {
            Color.WHITE
        }
        holder.tv.setTextColor(color)
        if (useCardBg && holder.card is com.google.android.material.card.MaterialCardView) {
            val card = holder.card as com.google.android.material.card.MaterialCardView
            val density = ctx.resources.displayMetrics.density
            val padding = (12 * density).toInt()
            val bigCorner = 24f * density
            val smallCorner = 0f 
            val outerMargin = (4 * density).toInt() 
            val groupMargin = 0 
            val crocosmiaPad = (12 * density).toInt()
            card.setContentPadding(crocosmiaPad, crocosmiaPad, crocosmiaPad, crocosmiaPad)
            card.cardElevation = 0f
            card.maxCardElevation = 0f
            card.preventCornerOverlap = false
            card.useCompatPadding = false
            card.setContentPadding(crocosmiaPad, crocosmiaPad, crocosmiaPad, crocosmiaPad)
            card.minimumHeight = 0
            val isFirst = position == 0
            val isLast = position == filtered.lastIndex
            val shape = card.shapeAppearanceModel.toBuilder()
            shape.setAllCorners(com.google.android.material.shape.CornerFamily.ROUNDED, smallCorner)
            if (isFirst) {
                shape.setBottomLeftCornerSize(bigCorner)
                shape.setBottomRightCornerSize(bigCorner)
            }
            if (isLast) {
                shape.setTopLeftCornerSize(bigCorner)
                shape.setTopRightCornerSize(bigCorner)
            }
            card.shapeAppearanceModel = shape.build()
            card.clipToOutline = true
            val params = card.layoutParams
            if (params is ViewGroup.MarginLayoutParams) {
                val gap = (1 * density).toInt()
                params.topMargin = if (isFirst) 0 else gap
                params.bottomMargin = 0
                params.leftMargin = 0
                params.rightMargin = 0
                card.layoutParams = params
            }
        }

        holder.tv.setOnLongClickListener {
            val actions = arrayOf("Copy", "Share", "Details")
            val icons = arrayOf(
                try {
                    val copyIcon = androidx.core.content.ContextCompat.getDrawable(ctx, com.lytefast.flexinput.R.e.ic_copy_24dp)
                    copyIcon?.setTint(com.discord.utilities.color.ColorCompat.getThemedColor(ctx, com.lytefast.flexinput.R.b.colorInteractiveNormal))
                    copyIcon
                } catch (_: Throwable) {
                    null
                },
                try {
                    val shareIcon = androidx.core.content.ContextCompat.getDrawable(ctx, com.lytefast.flexinput.R.e.ic_share_24dp)
                    shareIcon?.setTint(com.discord.utilities.color.ColorCompat.getThemedColor(ctx, com.lytefast.flexinput.R.b.colorInteractiveNormal))
                    shareIcon
                } catch (_: Throwable) {
                    null
                },
                try {
                    val infoIcon = androidx.core.content.ContextCompat.getDrawable(ctx, com.lytefast.flexinput.R.e.ic_info_24dp)
                    infoIcon?.setTint(com.discord.utilities.color.ColorCompat.getThemedColor(ctx, com.lytefast.flexinput.R.b.colorInteractiveNormal))
                    infoIcon
                } catch (_: Throwable) {
                    null
                }
            )
            val themedBg = com.discord.utilities.color.ColorCompat.getThemedColor(ctx, com.lytefast.flexinput.R.b.colorBackgroundFloating)
            val themedText = com.discord.utilities.color.ColorCompat.getThemedColor(ctx, com.lytefast.flexinput.R.b.colorHeaderPrimary)
            val font = try { androidx.core.content.res.ResourcesCompat.getFont(ctx, com.aliucord.Constants.Fonts.whitney_medium) } catch (_: Throwable) { null }
            val titleView = TextView(ctx).apply {
                text = "Log Entry Actions"
                setTextColor(themedText)
                if (font != null) typeface = font
                textSize = 18f
                setPadding((24 * ctx.resources.displayMetrics.density).toInt(), (18 * ctx.resources.displayMetrics.density).toInt(), (24 * ctx.resources.displayMetrics.density).toInt(), (8 * ctx.resources.displayMetrics.density).toInt())
                setBackgroundColor(themedBg)
            }
            val builder = androidx.appcompat.app.AlertDialog.Builder(ctx)
            builder.setCustomTitle(titleView)
            builder.setItems(actions) { _, which ->
                when (which) {
                    0 -> onLogAction?.invoke(logText, "copy")
                    1 -> onLogAction?.invoke(logText, "share")
                    2 -> onLogAction?.invoke(logText, "details")
                }
            }
            val dialog = builder.create()
            dialog.setOnShowListener {
                try {
                    dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(themedBg))
                    val listView = dialog.listView
                    listView.setBackgroundColor(themedBg)
                    for (i in actions.indices) {
                        val icon = icons[i]
                        listView.getChildAt(i)?.let { v ->
                            if (v is TextView) {
                                if (icon != null) {
                                    v.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
                                    v.compoundDrawablePadding = (12 * ctx.resources.displayMetrics.density).toInt()
                                }
                                v.setTextColor(themedText)
                                if (font != null) v.typeface = font
                                v.textSize = 16f
                            }
                        }
                    }
                } catch (_: Throwable) {}
            }
            dialog.show()
            true
        }
    }

    class VH(val tv: TextView, val card: View) : RecyclerView.ViewHolder(card)

    companion object {
        fun getMessage(item: Any): String {
            return try {
                val field = item.javaClass.getDeclaredField("l")
                field.isAccessible = true
                field.get(item) as? String ?: ""
            } catch (_: Throwable) { "" }
        }
        fun getPriority(item: Any): Int {
            return try {
                val field = item.javaClass.getDeclaredField("k")
                field.isAccessible = true
                field.getInt(item)
            } catch (_: Throwable) { 0 }
        }
        fun getThrowable(item: Any): String? {
            return try {
                val field = item.javaClass.getDeclaredField("m")
                field.isAccessible = true
                val t = field.get(item)
                t?.toString()
            } catch (_: Throwable) { null }
        }
        fun getTags(item: Any): List<String> {
            return try {
                val field = item.javaClass.getDeclaredField("tags")
                field.isAccessible = true
                (field.get(item) as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            } catch (_: Throwable) {
                try {
                    val field = item.javaClass.getDeclaredField("tag")
                    field.isAccessible = true
                    listOfNotNull(field.get(item) as? String)
                } catch (_: Throwable) { emptyList() }
            }
        }
    }
}