import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.3"
description = "Port of vc-betterplusreacts"

aliucord {
    changelog = """
        # 1.0.2
        * Support for custom emojis

        # 1.0.1
        * Support for '+++⭐' format

        # 1.0.0
        * Initial version
    """.trimIndent()

    author(Authors.RazerTexz)
}