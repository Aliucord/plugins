import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.3"
description = "A quick way to search for Aliucord themes"

aliucord {
    changelog = """
        # 1.0.2
        * Fixed a resource leak when fetching themes

        # 1.0.1
        * Automatically installs Themer if missing

        # 1.0.0
        * Initial version
    """.trimIndent()

    author(Authors.RazerTexz)
}