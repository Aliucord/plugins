/*
 * Omar (omardotdev)'s Aliucord Plugins
 * Copyright (C) 2025 Omar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
*/

package com.github.omardotdev

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.*
import com.discord.models.guild.Guild

@AliucordPlugin
class HideServerBanner : Plugin() {
    override fun start(context: Context) {
        // Removes banner off guild profile and channels list
        patcher.instead<Guild>("getBanner") { null }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
