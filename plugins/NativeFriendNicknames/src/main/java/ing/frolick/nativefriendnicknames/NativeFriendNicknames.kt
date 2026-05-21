package ing.frolick.nativefriendnicknames

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.lytefast.flexinput.R

import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.GatewayAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.aliucord.wrappers.users.globalName
import com.discord.api.channel.`ChannelUtils$getDisplayName$1`
import com.discord.models.user.CoreUser
import com.discord.api.user.User
import com.discord.utilities.color.ColorCompat
import b.a.a.d.a as UserActionsDialog
import com.discord.stores.StoreStream
import com.aliucord.Utils
import com.aliucord.wrappers.ChannelWrapper.Companion.isDM

internal class Relationship(val id: Long, val nickname: String?)
internal class Ready(val relationships: List<Relationship>)
object Stores {
    val friendNicknames = StoreRelationshipNick()
}

@SuppressWarnings("unused")
@AliucordPlugin(requiresRestart = true)
class NativeFriendNicknames : Plugin() {
    override fun start(context: Context) {
        Stores.friendNicknames.setup()

        patcher.before<CoreUser>("getGlobalName") {
            if (!StoreStream.getChannelsSelected().selectedChannel.isDM()) return@before
            val nickname = Stores.friendNicknames.getNickname(this.id) ?: this.globalName
            it.result = nickname
        }

        // this gets globalName from a different api, so I have to patch it out manually
        patcher.before<`ChannelUtils$getDisplayName$1`>("invoke", Any::class.java) {
            val nickname = Stores.friendNicknames.getNickname((it.args[0] as User).id) ?: (it.args[0] as User).globalName ?: (it.args[0] as User).username
            it.result = nickname
        }
        //Logger().info("METHODS: ${`ChannelUtils$getDisplayName$1`::class.java.declaredMethods.joinToString(",")}")

        patcher.patch(
            UserActionsDialog::class.java,
            "onViewBound",
            arrayOf(View::class.java),
            PreHook { param ->
                val dialog = param.thisObject as UserActionsDialog
                val root = param.args[0] as LinearLayout

                val userId = dialog.argumentsOrDefault
                    .getLong("com.discord.intent.extra.EXTRA_USER_ID", 0L)
                val user = StoreStream.Companion!!.users.getUsers(listOf(userId), false)[userId] ?: return@PreHook
                // no friend = no option
                if (!Stores.friendNicknames.data.containsKey(userId)) return@PreHook

                val ctx = root.context
                val newItem = TextView(ctx, null, 0, R.i.UiKit_ListItem_Icon).apply {
                    text = "Edit Friend Nickname"
                    setOnClickListener {
                        dialog.dismiss()
                        EditNicknameDialog(user).show(dialog.parentFragmentManager, "EditNickname")
                    }
                    setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat
                            .getDrawable(ctx, R.e.ic_edit_24dp)!!
                            .mutate()
                            .apply {
                                setTint(ColorCompat.getThemedColor(ctx, R.b.colorChannelIcon))
                            },
                        null,
                        null,
                        null
                    )
                }

                root.addView(newItem)
            }
        )
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}

class StoreRelationshipNick(var data: MutableMap<Long, String?> = mutableMapOf<Long, String?>()) {
    fun setup() {
        GatewayAPI.onEvent<Ready>("READY") { event ->
            data = event.relationships.associate { relationship ->
                relationship.id to relationship.nickname
            } as MutableMap<Long, String?>
        }
        GatewayAPI.onEvent<Relationship>("RELATIONSHIP_ADD") { event ->
            data[event.id] = event.nickname
        }
        GatewayAPI.onEvent<Relationship>("RELATIONSHIP_UPDATE") { event ->
            data[event.id] = event.nickname
        }
        GatewayAPI.onEvent<Relationship>("RELATIONSHIP_REMOVE") { event ->
            data.remove(event.id)
        }
    }
    fun getNickname(id: Long?): String? = data[id]
}
