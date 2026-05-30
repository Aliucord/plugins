package dev.rushii.plugins.typingusers

import android.annotation.SuppressLint
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.fragments.SettingsPage
import com.aliucord.wrappers.ChannelWrapper.Companion.name
import com.discord.stores.StoreStream
import com.lytefast.flexinput.R

@SuppressLint("SetTextI18n")
class TypingUsersPage(private val plugin: TypingUsers) : SettingsPage() {
    override fun onViewBound(view: View?) {
        super.onViewBound(view)

        setActionBarTitle('#' + StoreStream.getChannelsSelected().selectedChannel.name)
        setActionBarSubtitle(null)

        plugin.typingUsersModelClearTask?.let(Utils.mainThread::removeCallbacks)
        val modelProvider = ViewModelProvider(plugin.typingUsersModelStore, ViewModelProvider.NewInstanceFactory())
        val model = modelProvider.get(TypingUsersPageModel::class.java)

        val ctx = this.requireContext()
        val container = this.linearLayout.apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
        }

        TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).apply {
            text = "Currently typing"
            isAllCaps = true
            container.addView(this)
        }

        RecyclerView(ctx).apply {
            adapter = TypingUsersAdapter(model.currentlyTyping, model.refreshUser, parentFragmentManager)
            layoutManager = LinearLayoutManager(ctx)
            container.addView(this)
        }

        TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Header).apply {
            text = "Previously typing"
            isAllCaps = true
            container.addView(this)
        }

        RecyclerView(ctx).apply {
            adapter = TypingUsersAdapter(model.previouslyTyping, model.refreshUser, parentFragmentManager)
            layoutManager = LinearLayoutManager(ctx)
            container.addView(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Clear the ViewModel after 2min
        plugin.typingUsersModelClearTask?.let(Utils.mainThread::removeCallbacks)
        plugin.typingUsersModelClearTask = Runnable {
            plugin.typingUsersModelStore.clear()
            plugin.typingUsersModelClearTask = null
        }
        Utils.mainThread.postDelayed(plugin.typingUsersModelClearTask!!, /* 2min */ 1000 * 60 * 2)
    }
}
