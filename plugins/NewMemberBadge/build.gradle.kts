import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.1"
description = "Show the New Member badge on new members in chat"

aliucord {
    author(Authors.Ushie)

    changelog.set(
        """
        # 1.0.1
        * Fix badge randomly appearing in DMs
    """.trimIndent(),
    )
}
