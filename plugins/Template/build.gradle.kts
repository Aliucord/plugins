import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Template plugin"

aliucord {
    author(Authors.Aliucord)

    changelog.set(
        """
        # 1.0.0
        * Initial plugin release!
        """.trimIndent(),
    )

    deploy.set(false)
}
