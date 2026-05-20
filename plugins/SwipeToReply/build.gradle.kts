import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.2"
description = "Swipe to quickly reply!"

aliucord {
    changelog = """
        # 1.0.0
        * Initial version
    """.trimIndent()

    author(Authors.RazerTexz)
}