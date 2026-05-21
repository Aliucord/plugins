import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.4"
description = "Removes emojis and symbols from channel names, and more"

aliucord.changelog.set("""
    # 1.0.4
    * Fixed unintentionally applying to usernames 

    # 1.0.3
    * Fixed conflicts with CharCounter

    # 1.0.1
    * now works with threads sucessfully (doesnt remove spaces or dashes)
    * you can now whitelist a server from the menu, and show/hide this toggle in the settings
    * there is now a normalization setting which turns fancy characters into standard ones
""".trimIndent())

aliucord {
    author(Authors.Lamp)
    deploy.set(true)
}
