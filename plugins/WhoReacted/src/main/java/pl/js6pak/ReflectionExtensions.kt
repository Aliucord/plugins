/*
 * WhoReacted
 * Copyright (C) 2021 js6pak
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

import com.discord.databinding.WidgetChatListAdapterItemReactionsBinding
import com.discord.stores.StoreMessageReactions
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemReactions
import java.lang.reflect.Field

private val bindingField: Field =
    WidgetChatListAdapterItemReactions::class.java.getDeclaredField("binding")
        .apply { isAccessible = true }

val WidgetChatListAdapterItemReactions.binding: WidgetChatListAdapterItemReactionsBinding
    get() = bindingField[this] as WidgetChatListAdapterItemReactionsBinding

private val reactionsField: Field =
    StoreMessageReactions::class.java.getDeclaredField("reactions")
        .apply { isAccessible = true }

val StoreMessageReactions.reactions: Map<Long, Map<String, StoreMessageReactions.EmojiResults>>
    @Suppress("UNCHECKED_CAST")
    get() = reactionsField[this] as Map<Long, Map<String, StoreMessageReactions.EmojiResults>>
