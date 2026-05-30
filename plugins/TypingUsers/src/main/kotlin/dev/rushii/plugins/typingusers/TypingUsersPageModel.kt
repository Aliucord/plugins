package dev.rushii.plugins.typingusers

import androidx.lifecycle.ViewModel
import com.aliucord.Utils
import com.aliucord.utils.RxUtils.subscribe
import com.discord.stores.StoreStream
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class TypingUsersPageModel : ViewModel() {
    private var currentChannelSubscription: Subscription? = null
    private var presenceUpdateSubscription: Subscription? = null
    private var typingSubscription: Subscription? = null

    val currentlyTyping: BehaviorSubject<Set<Long>> = BehaviorSubject.l0(emptySet())
    val previouslyTyping: BehaviorSubject<Set<Long>> = BehaviorSubject.l0(emptySet())
    val refreshUser: PublishSubject<Long> = PublishSubject.k0()

    init {
        currentChannelSubscription = StoreStream.getChannelsSelected().observeId().subscribe {
            typingSubscription?.unsubscribe()
            typingSubscription = StoreStream.getUsersTyping().observeTypingUsers(this).subscribe {
                val allPreviousUsers = currentlyTyping.n0() + previouslyTyping.n0()
                val newUsers = this - allPreviousUsers
                if (newUsers.isNotEmpty()) requestMembers(newUsers)

                Utils.mainThread.post {
                    currentlyTyping.onNext(this)
                    previouslyTyping.onNext(allPreviousUsers - this)
                }

                presenceUpdateSubscription?.unsubscribe()
                presenceUpdateSubscription = StoreStream.getPresences()
                    .observePresencesForUsers(allPreviousUsers + this)
                    .subscribe {
                        Utils.mainThread.post {
                            for ((userId) in this)
                                refreshUser.onNext(userId)
                        }
                    }
            }
        }
    }

    private fun requestMembers(userIds: Set<Long>) {
        val guildId = StoreStream.getGuildSelected().selectedGuildId
            .takeIf { it > 0 } ?: return

        StoreStream.getGatewaySocket().requestGuildMembers( // @formatter:off
			/* guildId = */ guildId,
			/* query = */ null,
			/* userIds = */ userIds.take(100),
			/* limit = */ null,
		)  // @formatter:on
    }

    override fun onCleared() {
        currentChannelSubscription?.unsubscribe()
        typingSubscription?.unsubscribe()
    }
}
