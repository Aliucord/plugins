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
 * // ...
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
    val Aliucord = Author(name = "Aliucord")
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
