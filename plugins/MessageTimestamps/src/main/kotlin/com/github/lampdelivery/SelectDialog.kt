package com.github.lampdelivery

import android.app.Dialog
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.discord.utilities.color.ColorCompat
import com.lytefast.flexinput.R

class SelectDialog(
    private var items: List<String> = listOf(),
    private var title: String = "",
    private var onResult: ((Int) -> Unit)? = null
) : DialogFragment() {
    fun setItems(items: List<String>) { this.items = items }
    fun setTitle(title: String) { this.title = title }
    fun setOnResultListener(listener: (Int) -> Unit) { this.onResult = listener }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val ctx = requireContext()
        val color = ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal)
        val titleView = TextView(ctx).apply {
            text = title
            setTextColor(color)
            textSize = 20f
            setPadding(32, 32, 32, 16)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        val builder = AlertDialog.Builder(ctx)
            .setCustomTitle(titleView)
            .setItems(items.toTypedArray()) { _, which ->
                onResult?.invoke(which)
            }
        val dialog = builder.create()
        dialog.setOnShowListener {
            try {
                val listView = dialog.listView
                for (i in 0 until listView.childCount) {
                    val v = listView.getChildAt(i)
                    if (v is TextView) v.setTextColor(color)
                }
                listView.setOnHierarchyChangeListener(object : android.view.ViewGroup.OnHierarchyChangeListener {
                    override fun onChildViewAdded(parent: android.view.View?, child: android.view.View?) {
                        if (child is TextView) child.setTextColor(color)
                    }
                    override fun onChildViewRemoved(parent: android.view.View?, child: android.view.View?) {}
                })
            } catch (_: Throwable) {}
        }
        return dialog
    }
}
