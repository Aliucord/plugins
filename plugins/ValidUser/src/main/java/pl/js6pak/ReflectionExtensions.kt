/*
 * ValidUser
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

package pl.js6pak.validuser

import com.discord.stores.Dispatcher
import com.discord.stores.StoreUser
import com.discord.stores.`StoreStream$users$1`

val StoreUser.notifyUserUpdated: `StoreStream$users$1`
    @Suppress("UNCHECKED_CAST")
    get() = StoreUser.`access$getNotifyUserUpdated$p`(this) as `StoreStream$users$1`

val StoreUser.dispatcher: Dispatcher
    @Suppress("UNCHECKED_CAST")
    get() = StoreUser.`access$getDispatcher$p`(this)
