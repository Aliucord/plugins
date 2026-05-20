import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.1"
description = "Displays configurable user, group, and server avatars in the header"

aliucord {
    author(Authors.Ushie)

    changelog.set(
        """
            # 1.0.1
            * Fix positioning of the avatar
        """.trimIndent(),
    )
}
