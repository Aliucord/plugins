/*
 * WhoReacted
 * Copyright (C) 2022 js6pak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package pl.js6pak.whoreacted

import androidx.viewbinding.ViewBinding
import com.discord.api.message.reaction.MessageReaction
import com.discord.api.message.reaction.MessageReactionEmoji
import com.discord.databinding.WidgetChatListAdapterItemReactionsBinding
import com.discord.views.ReactionView
import com.facebook.drawee.generic.GenericDraweeHierarchy
import com.google.android.flexbox.FlexboxLayout
import java.lang.reflect.Field

inline val MessageReaction.count: Int
    get() = this.a()

inline val MessageReaction.emoji: MessageReactionEmoji
    get() = this.b()

inline val MessageReactionEmoji.id: String
    get() = this.c()

inline val WidgetChatListAdapterItemReactionsBinding.chatListItemReactions: FlexboxLayout
    get() = this.d

private val bindingField: Field =
    ReactionView::class.java.getDeclaredField("o").apply { isAccessible = true }

val ReactionView.binding: ViewBinding
    get() = bindingField[this] as ViewBinding

typealias RoundingParams = b.f.g.f.c

var RoundingParams.mRoundAsCircle: Boolean
    get() = this.b
    set(value) {
        this.b = value
    }

fun GenericDraweeHierarchy.setRoundingParams(roundingParams: RoundingParams?) {
    this.s(roundingParams)
}
