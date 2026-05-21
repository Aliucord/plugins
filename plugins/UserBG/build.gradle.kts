import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.2.3"
description = "UserBG"

aliucord {
    changelog.set(
        """
        * Update to new URL for USRBG
    """.trimIndent()
    )

    author(Authors.HalalKing)
}