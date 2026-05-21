package ing.frolick.nativefriendnicknames

import android.view.View

import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.fragments.InputDialog
import com.aliucord.wrappers.users.globalName
import com.discord.models.user.User
import com.discord.stores.StoreStream
import com.discord.stores.updates.ObservationDeck

data class UpdateRelationshipRequest(
    val nickname: String?
)

class EditNicknameDialog(private val user: User) : InputDialog() {
    override fun onViewBound(view: View) {
        setTitle("Edit Friend Nickname")
        setDescription("Enter a new nickname for your friend:")
        setPlaceholderText("Nickname")

        setOnDialogShownListener {
            // only replace input if a friend nickname already exists
            if (Stores.friendNicknames.getNickname(user.id) != null)
                inputLayout.editText?.setText(user.globalName)
        }

        setOnOkListener {
            val inputName = input.trim()

            if (inputName != user.globalName) {
                Utils.threadPool.execute {
                    val req = UpdateRelationshipRequest(inputName)
                    val res = Http.Request
                        .newDiscordRNRequest("/users/@me/relationships/${user.id}", "PATCH")
                        .executeWithJson(req)
                    // why does this not throw the usual 200 success code dieee discord
                    if (res.statusCode == 204) {
                        // this is supposed to instantly update everything that uses User.globalName
                        // but it doesn't :(
                        /*StoreStream.`access$getDispatcher$p`(StoreStream.getPresences().stream).schedule {
                            val storeUsers = StoreStream.Companion!!.users
                            val getUsersUpdate =
                                storeUsers::class.java.getDeclaredMethod("access\$getUsersUpdate\$cp").apply {
                                    isAccessible = true
                                }
                            val usersUpdate = getUsersUpdate.invoke(null)
                            storeUsers.markChanged(usersUpdate as ObservationDeck.UpdateSource?)
                        }*/
                    } else {
                        Utils.mainThread.post {
                            Utils.showToast("Error ${res.statusCode} while changing ${user.username}'s nickname!", true)
                        }
                    }
                }
            }

            dismiss()
        }

        super.onViewBound(view)
    }
}
