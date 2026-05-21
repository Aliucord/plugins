import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.3"
description = "Saves your scroll position when searching"

aliucord {
    changelog = """
        # 1.0.1
        * Hopefully no timing issues

        # 1.0.0
        * Initial version
    """.trimIndent()

    author(Authors.RazerTexz)
}