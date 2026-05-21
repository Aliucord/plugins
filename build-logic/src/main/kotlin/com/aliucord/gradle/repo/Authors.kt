package com.aliucord.gradle.repo

import com.aliucord.gradle.AliucordExtension

/**
 * A list of all currently known authors, used in the plugin buildscripts
 * to specify plugin authors. Add yourself to this list rather than duplicating
 * your author information in each separate plugin.
 *
 * Entries are not required to use standard Kotlin naming conventions.
 *
 * Example usage from a plugin's `build.gradle.kts`:
 * ```kotlin
 * import com.aliucord.gradle.Authors
 * import com.aliucord.gradle.author
 *
 * plugins {
 *     id("com.aliucord.plugins-repo")
 * }
 *
 * version = "..."
 * description = "..."
 *
 * aliucord {
 *     author(Authors.xyz)
 *
 *     // ...
 * }
 * ```
 */
@Suppress("SpellCheckingInspection", "unused", "RedundantSuppression")
object Authors {
    val Aliucord = Author("Aliucord")
    val Ushie = Author("Ushie", 399862294143696897L)
    val reisxd = Author("reisxd", 1385511666266935318L)
    val Grzesiek11 = Author("Grzesiek11", 368475654662127616L)
    val Alyxia = Author("Alyxia", 465702500146610176L)
    val oSumAtrIX = Author("oSumAtrIX", 737323631117598811L)
    val js6pak = Author("6pak", 141580516380901376L)
    val RazerTexz = Author("RazerTexz", 633565155501801472L)
    val Xinto = Author("Xinto", 423915768191647755L)
    val zt = Author("zt", 289556910426816513L)
    val rushii = Author("rushii", 0L, false)
    val Nyako = Author("nyakowint", 118437263754395652L)
    val Juby210 = Author("Juby210", 324622488644616195L)
    val LampDelivery = Author("LampDelivery", 650805815623680030L)
    val miaaaa0a = Author("miaaaa0a", 435750383491481602L)
    val Vendicated = Author("Vendicated", 343383572805058560L)
    val mantikafasi = Author("mantikafasi", 287555395151593473L)
    val Ty = Author("Ty", 487443883127472129L)
    val Cloudburst = Author("Cloudburst", 295186738085756929L)
    val HalalKing = Author("HalalKing", 261634919980204033L)
    val Accelerator = Author("Accelerator", 150234173024501762L)
    val Derlan = Author("Derlan", 821545900807028757L)
    val Link = Author("Link", 725923756555501658L)
    val Wing = Author("Wing", 298295889720770563L)
    val ArjixWasTaken = Author("ArjixWasTaken", 674710789138939916L)
    val PikachuGabe = Author("pikachugabe", 701095070870274139L)
}

/**
 * Represents a plugin author.
 *
 * @param name      The user-facing name to display
 * @param id        The Discord ID of the author, optional.
 *                  This also will allow Aliucord to show a badge on your profile if the plugin is installed.
 * @param hyperlink Whether to hyperlink the Discord profile specified by [id].
 *                  Set this to false if you don't want to be spammed for support.
 */
data class Author(
    val name: String,
    val id: Long = 0L,
    val hyperlink: Boolean = true,
)

/**
 * Specifies an already-known author of this plugin.
 */
fun AliucordExtension.author(author: Author) {
    author(
        name = author.name,
        id = author.id,
        hyperlink = author.hyperlink,
    )
}
