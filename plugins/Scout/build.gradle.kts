version = "1.4.0"
description = "Backported and improved search functionality"

android {
    namespace = "moe.lava.awoocord.scout"
}

aliucord {
    // Changelog of your plugin
    changelog.set("""
        !!! Minimum Aliucord version requirement {fixed}
        ======================
        * Scout now requires Aliucord 2.4.0, please update before reporting issues.

        Changelog {added marginTop}
        ======================
        # 1.4.0 - Scout is searching for clues about the elusive MvM update
        * Added the authorType filter option to search by user, bot, or webhook
        * Moved sort filter to the top of the new ones
        * Fixes a Discord bug where typing "mentions" would also suggest "has"
        * Some people said the options were getting bloated, so they're all hidden behind a "Show all" button now. They'll still show up in auto suggestions.

        # 1.3.0
        * Removes empty discriminator when searching with users

        # 1.2.2
        * Fix possible rare crash related to thread searching

        # 1.2.1
        * Fixes off-looking thread icon
        Only Discord will name an icon "thread_white_24dp", and it's neither white nor 24dp. Seriously, what were they thinking?

        # 1.2.0 - Scout is in:to knitting
        * Adds support for searching threads; simply use in:

        # 1.1.3
        * Patch to fix the biggggg top padding in results

        # 1.1.2
        * Fix month being one month behind after using the date picker

        # 1.1.1
        * Use proper icons for search filter suggestions

        # 1.1.0 - Look out, Scout has:updates
        * Add "has:forward" and "has:poll" filters
        * Add "exclude:" filter. It is the opposite of "has:" and filters out matching elements

        # 1.0.1
        * Fix not being able to search more than one page with sort:old

        # 1.0.0
        * Initial release >w<
    """.trimIndent())

    deploy.set(true)
}
