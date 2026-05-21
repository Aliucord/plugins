/*
 * UserDetails integration for ModernProfiles
 * Based on UserDetails plugin by Juby210
 */

package com.github.lampdelivery

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.utils.ReflectUtils
import com.aliucord.utils.RxUtils
import com.aliucord.utils.RxUtils.onBackpressureBuffer
import com.aliucord.utils.RxUtils.subscribe
import com.aliucord.wrappers.ChannelWrapper.Companion.recipients
import com.discord.databinding.UserProfileHeaderViewBinding
import com.discord.models.message.Message
import com.discord.models.user.CoreUser
import com.discord.models.user.User
import com.discord.stores.StoreSearch
import com.discord.stores.StoreStream
import com.discord.utilities.SnowflakeUtils
import com.discord.utilities.search.network.SearchFetcher
import com.discord.utilities.search.network.SearchQuery
import com.discord.utilities.time.ClockFactory
import com.discord.utilities.time.TimeUtils
import com.discord.widgets.user.profile.UserProfileHeaderView
import com.lytefast.flexinput.R
import rx.Subscription
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

// Cache data structure for user details
class CachedData(var joinedAt: Long?, var lastMessage: Long?)

class UserDetailsHelper {
    companion object {
        private val viewId = View.generateViewId()
        private val customStatusViewId = Utils.getResId("user_profile_header_custom_status", "id")

        // Cache and subscription management
        private val cache = HashMap<Long, HashMap<Long, CachedData>>()
        private var lastRequestedSearch: Long = 0
        private var forceUpdate: Runnable? = null
        private val searchSubscription: AtomicReference<Subscription?> = AtomicReference()
        private var messageSubscription: Subscription? = null
        private var isInitialized = false

        private val profileHeaderBinding: Field = UserProfileHeaderView::class.java
            .getDeclaredField("binding")
            .apply { isAccessible = true }
        private val UserProfileHeaderView.binding
            get() = profileHeaderBinding[this] as UserProfileHeaderViewBinding?

        // Initialize message listener if needed
        private fun initializeMessageListener() {
            if (isInitialized) return
            isInitialized = true

            try {
                val gatewaySocket = StoreStream.getGatewaySocket()
                messageSubscription = gatewaySocket.messageCreate.onBackpressureBuffer().subscribe(
                    RxUtils.createActionSubscriber({
                        val msg = if (it == null) return@createActionSubscriber else Message(it)
                        cacheData(
                            guildId = msg.guildId ?: msg.channelId,
                            id = CoreUser(msg.author).id,
                            lastMessage = SnowflakeUtils.toTimestamp(msg.id)
                        )
                    })
                )
            } catch (_: Throwable) {
                // Fail silently if message listener setup fails
            }
        }

        private fun cacheData(guildId: Long, id: Long, joinedAt: Long? = null, lastMessage: Long? = null) {
            val guildCache = cache.getOrPut(guildId) { HashMap() }
            if (guildCache.containsKey(id)) {
                val cachedData = guildCache[id]!!
                if (joinedAt != null) cachedData.joinedAt = joinedAt
                if (lastMessage != null) cachedData.lastMessage = lastMessage
            } else {
                guildCache[id] = CachedData(joinedAt, lastMessage)
            }
        }

        private fun toReadable(context: android.content.Context, timestamp: Long, showDaysAgo: Boolean): String {
            val clock = ClockFactory.get()
            var readable = TimeUtils.toReadableTimeString(context, timestamp, clock).toString()
            if (showDaysAgo) {
                val days = TimeUnit.DAYS.convert(clock.currentTimeMillis() - timestamp, TimeUnit.MILLISECONDS)
                readable += when (days) {
                    0L -> " (Today)"
                    1L -> " (Yesterday)"
                    else -> " ($days days ago)"
                }
            }
            return readable
        }

        private fun appendLastMessage(text: StringBuilder, lastMessage: CharSequence) {
            if (text.isNotEmpty()) text.append("\n")
            text.append("Last message: ").append(lastMessage)
        }

        private var searchFetcher: SearchFetcher? = null
        private fun search(authorId: Long, id: Long, dm: Boolean) {
            try {
                val fetcher = searchFetcher ?: ReflectUtils.getField(
                    StoreStream.getSearch().storeSearchQuery,
                    "searchFetcher"
                ).let {
                    (it as SearchFetcher).apply { searchFetcher = this }
                }

                searchSubscription.getAndSet(
                    fetcher.makeQuery(
                        StoreSearch.SearchTarget(
                            if (dm) StoreSearch.SearchTarget.Type.CHANNEL else StoreSearch.SearchTarget.Type.GUILD,
                            id
                        ),
                        null,
                        SearchQuery(mapOf("author_id" to listOf(authorId.toString())), true)
                    ).subscribe(RxUtils.createActionSubscriber({
                        searchSubscription.getAndSet(null)?.unsubscribe()
                        if (it == null || it.errorCode != null) return@createActionSubscriber
                        if (it.totalResults == 0) cacheData(guildId = id, id = authorId, lastMessage = -1)
                        else it.hits?.run {
                            if (size > 0) cacheData(
                                guildId = id,
                                id = authorId,
                                lastMessage = SnowflakeUtils.toTimestamp(Message(get(0)).id)
                            )
                        } ?: return@createActionSubscriber
                        if (lastRequestedSearch == authorId && forceUpdate != null) {
                            lastRequestedSearch = 0
                            Utils.mainThread.post(forceUpdate!!)
                        }
                    }))
                )?.unsubscribe()
            } catch (_: Throwable) {
                // Search failed, cache as unavailable
                cacheData(guildId = id, id = authorId, lastMessage = -1)
            }
        }

        private fun findUserProfileHeaderView(root: View): UserProfileHeaderView? {
            fun searchForProfileHeaderView(v: View): UserProfileHeaderView? {
                if (v is UserProfileHeaderView) {
                    return v
                }
                if (v is ViewGroup) {
                    for (i in 0 until v.childCount) {
                        val found = searchForProfileHeaderView(v.getChildAt(i))
                        if (found != null) return found
                    }
                }
                return null
            }

            return searchForProfileHeaderView(root)
        }

        fun addMemberDetails(
            root: View,
            user: User,
            showCreatedAt: Boolean,
            showJoinedAt: Boolean,
            showDaysAgo: Boolean,
            showLastMessage: Boolean = false
        ) {
            try {
                // Initialize message listener if last message is enabled
                if (showLastMessage) {
                    initializeMessageListener()
                }

                val view = findUserProfileHeaderView(root) ?: return
                val binding = view.binding ?: return

                val customStatus = binding.a.findViewById<View>(customStatusViewId) ?: return
                val layout = customStatus.parent as LinearLayout
                val context = layout.context

                val detailsView = layout.findViewById(viewId) ?: TextView(
                    context,
                    null,
                    0,
                    R.i.UiKit_TextView_Semibold
                ).apply {
                    id = viewId
                    typeface = ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold)

                    try {
                        fun findAboutText(v: View): TextView? {
                            if (v is ViewGroup) {
                                for (i in 0 until v.childCount) {
                                    val c = v.getChildAt(i)
                                    try {
                                        if (c.id != View.NO_ID) {
                                            val name = c.context.resources.getResourceEntryName(c.id)
                                            if (name.contains("about_me_text") ||
                                                name.contains("aboutMeText")
                                            ) {
                                                return c as? TextView
                                            }
                                        }
                                    } catch (_: Throwable) {
                                    }

                                    if (c is TextView) {
                                        val txt = c.text?.toString()?.trim() ?: ""
                                        if (txt.isNotEmpty() && txt.length > 16) return c
                                    }

                                    val found = findAboutText(c)
                                    if (found != null) return found
                                }
                            }
                            return null
                        }

                        val aboutTextView = findAboutText(root)
                        if (aboutTextView != null) {
                            setTextColor(aboutTextView.currentTextColor)
                        } else {
                            setTextColor(0xFF72767D.toInt())
                        }
                    } catch (_: Throwable) {
                        setTextColor(0xFF72767D.toInt())
                    }

                    layout.addView(this)
                }

                val userId = user.id
                val guildId = StoreStream.getGuildSelected().selectedGuildId
                val isDM = guildId == 0L
                val text = StringBuilder()

                if (showCreatedAt) {
                    text.append("Created at: ").append(
                        toReadable(
                            context,
                            SnowflakeUtils.toTimestamp(userId),
                            showDaysAgo
                        )
                    )
                }

                if (showJoinedAt && !isDM) {
                    try {
                        val guildMember = StoreStream.getGuilds().getMember(guildId, userId)
                        if (guildMember != null) {
                            try {
                                val joinedAtField = guildMember.javaClass.getDeclaredField("joinedAt")
                                joinedAtField.isAccessible = true
                                val joinedAtObj = joinedAtField.get(guildMember)

                                if (joinedAtObj != null) {
                                    var joinedTimestamp: Long? = null

                                    try {
                                        val timestampMethod = joinedAtObj.javaClass.getMethod("g")
                                        joinedTimestamp = (timestampMethod.invoke(joinedAtObj) as? Number)?.toLong()
                                    } catch (_: Throwable) {
                                        try {
                                            joinedTimestamp = (joinedAtObj as? Number)?.toLong()
                                        } catch (_: Throwable) {
                                        }
                                    }

                                    if (joinedTimestamp != null && joinedTimestamp > 0) {
                                        if (text.isNotEmpty()) text.append("\n")
                                        text.append("Joined at: ").append(
                                            toReadable(
                                                context,
                                                joinedTimestamp,
                                                showDaysAgo
                                            )
                                        )
                                    } else {
                                        if (text.isNotEmpty()) text.append("\n")
                                        text.append("Joined at: -")
                                    }
                                } else {
                                    if (text.isNotEmpty()) text.append("\n")
                                    text.append("Joined at: -")
                                }
                            } catch (_: Throwable) {
                                if (text.isNotEmpty()) text.append("\n")
                                text.append("Joined at: -")
                            }
                        } else {
                            if (text.isNotEmpty()) text.append("\n")
                            text.append("Joined at: -")
                        }
                    } catch (_: Throwable) {
                        if (text.isNotEmpty()) text.append("\n")
                        text.append("Joined at: -")
                    }
                }

                // Last message functionality
                if (showLastMessage) {
                    val channelId = StoreStream.getChannelsSelected().id
                    val dontFetchLast = isDM && (channelId < 1 ||
                        StoreStream.getUsers().me.id != userId &&
                        StoreStream.getChannels().getChannel(channelId)?.recipients?.any { CoreUser(it).id == userId }?.let { !it } ?: true)

                    if (dontFetchLast) {
                        appendLastMessage(text, "-")
                    } else {
                        val cIdOrGId = if (isDM) channelId else guildId
                        cache[cIdOrGId]?.get(userId)?.lastMessage?.let {
                            appendLastMessage(text, if (it == -1L) "-" else toReadable(context, it, showDaysAgo))
                        } ?: run {
                            if (lastRequestedSearch != userId) {
                                lastRequestedSearch = userId
                                forceUpdate = Runnable { addMemberDetails(root, user, showCreatedAt, showJoinedAt, showDaysAgo, showLastMessage) }
                                search(userId, cIdOrGId, isDM)
                            }
                        }
                    }
                }

                detailsView.text = text
            } catch (_: Throwable) {
            }
        }

        // Clean up subscriptions
        fun cleanup() {
            messageSubscription?.unsubscribe()
            searchSubscription.getAndSet(null)?.unsubscribe()
        }
    }
}
