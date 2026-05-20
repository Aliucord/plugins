/*
 * Based on Vencord's ClearURLs plugin:
 * Copyright (c) 2022 Vendicated and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.github.ushie

import android.content.Context
import com.aliucord.Http
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.PreHook
import com.aliucord.utils.lazyField
import com.discord.widgets.chat.MessageContent
import com.discord.widgets.chat.MessageManager
import com.discord.widgets.chat.input.ChatInputViewModel
import org.json.JSONObject
import java.net.URI
import java.util.regex.Pattern

private const val RULES_URL =
    "https://raw.githubusercontent.com/ClearURLs/Rules/master/data.min.json"

private val URL_REGEX = Regex("""https?://[^\s<]+[^<.,:;"')\]\s]""")

@AliucordPlugin
class ClearURLs : Plugin() {
    private val rules = mutableListOf<RuleSet>()
    private val textContentField by lazyField<MessageContent>("textContent")

    private data class RuleSet(
        val url: Pattern,
        val params: List<Pattern>,
        val raw: List<Pattern>,
        val exceptions: List<Pattern>
    )

    override fun start(context: Context) {
        loadRules()

        patcher.patch(
            ChatInputViewModel::class.java.getDeclaredMethod(
                "sendMessage",
                Context::class.java,
                MessageManager::class.java,
                MessageContent::class.java,
                List::class.java,
                Boolean::class.javaPrimitiveType,
                Function1::class.java
            ),
            PreHook {
                val msg = it.args[2] as? MessageContent ?: return@PreHook
                val content = textContentField.get(msg) as? String ?: return@PreHook
                clean(content).takeIf { cleaned -> cleaned != content }?.let { cleaned ->
                    textContentField.set(msg, cleaned)
                }
            }
        )
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    private fun loadRules() = Thread {
        val providers = JSONObject(Http.simpleGet(RULES_URL)).getJSONObject("providers")

        rules.clear()
        providers.keys().forEach { key ->
            val provider = providers.getJSONObject(key)

            rules += RuleSet(
                url = provider.pattern("urlPattern"),
                params = provider.patterns("rules"),
                raw = provider.patterns("rawRules"),
                exceptions = provider.patterns("exceptions")
            )
        }
    }.start()

    private fun clean(content: String) =
        if ("http" !in content) content else URL_REGEX.replace(content) { cleanUrl(it.value) }

    private fun cleanUrl(match: String): String {
        var uri = runCatching { URI(match) }.getOrNull() ?: return match

        for (rule in rules) {
            val url = uri.toString()
            val query = uri.rawQuery ?: continue

            if (!rule.url.matcher(url).find()) continue
            if (rule.exceptions.any { it.matcher(url).find() }) continue

            var cleaned = URI(
                uri.scheme,
                uri.authority,
                uri.path,
                query.split("&")
                    .filterNot { param ->
                        rule.params.any { it.matcher(param.substringBefore("=")).find() }
                    }
                    .joinToString("&")
                    .takeIf(String::isNotEmpty),
                uri.fragment
            ).toString()

            rule.raw.forEach {
                cleaned = it.matcher(cleaned).replaceAll("")
            }

            uri = runCatching { URI(cleaned) }.getOrNull() ?: return cleaned
        }

        return uri.toString()
    }

    private fun JSONObject.pattern(key: String) =
        Pattern.compile(getString(key), Pattern.CASE_INSENSITIVE)

    private fun JSONObject.patterns(key: String) =
        optJSONArray(key)?.let {
            List(it.length()) { i -> Pattern.compile(it.getString(i), Pattern.CASE_INSENSITIVE) }
        }.orEmpty()
}
