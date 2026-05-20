import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Shows who sent the blocked messages in chat without opening them"


aliucord {
    aliucord {
        author(Authors.Ushie)
    }

    changelog.set(
        """
        # 1.0.0
        * Initial release
        * TODO: Support multiple authors
    """.trimIndent()
    )
}
