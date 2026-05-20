import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.1"
description = "Disable server-specific avatars, banners, and nicknames"

aliucord {
    author(Authors.Ushie)

    changelog.set(
        """
            # 1.0.1
            * Fix nickname setting not doing anything
        """.trimIndent()
    )
}
