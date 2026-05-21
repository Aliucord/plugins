import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.1"
description = "Adds a copy button on multi-line codeblocks!"

aliucord {
    changelog = """
        # 1.0.0
        * Initial version
    """.trimIndent()

    author(Authors.RazerTexz)
}