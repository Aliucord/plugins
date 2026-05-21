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

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.Editable
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.aliucord.views.TextInput
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.view.text.TextWatcher
import com.lytefast.flexinput.R

private object MaxUsers {
    const val ID: String = "maxUsersShown"
    const val DEFAULT: Int = 5
}

var SettingsAPI.maxUsers: Int
    get() = this.getInt(MaxUsers.ID, MaxUsers.DEFAULT)
    set(value) = this.setInt(MaxUsers.ID, value)

class WhoReactedSettings(private val plugin: WhoReacted) : SettingsPage() {
    @SuppressLint("SetTextI18n")
    override fun onViewBound(view: View) {
        super.onViewBound(view)

        setActionBarTitle(plugin.getName())

        val ctx = view.context

        TextInput(ctx, "Max users shown", plugin.settings.maxUsers.toString()).layout.run {
            helperText = "The maximum number of users shown per reaction between 0 and 20"

            editText?.run {
                maxLines = 1
                inputType = InputType.TYPE_CLASS_NUMBER

                setHelperTextTextAppearance(R.i.UiKit_TextAppearance_MaterialEditText_Label)
                setHelperTextColor(
                    ColorStateList.valueOf(
                        ColorCompat.getThemedColor(
                            context,
                            R.b.colorHeaderSecondary
                        )
                    )
                )

                val editText = this
                addTextChangedListener(object : TextWatcher() {
                    override fun afterTextChanged(s: Editable) {
                        val value = try {
                            s.toString().toInt()
                        } catch (th: NumberFormatException) {
                            MaxUsers.DEFAULT
                        }

                        if (value < 0 || value > 20) {
                            editText.error = "Out of range"
                            return
                        }

                        plugin.settings.maxUsers = value
                    }
                })
            }

            (this.parent as ViewGroup).removeView(this) // huh?
            linearLayout.addView(this)
        }
    }
}