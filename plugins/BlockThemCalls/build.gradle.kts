import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Allows you to block calls from other users"

aliucord {
    changelog = """
        # 1.0.0
        * Initial version
    """.trimIndent()

    author(Authors.RazerTexz)
}