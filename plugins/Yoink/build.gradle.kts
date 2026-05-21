import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Exports loaded channel messages (up to 200) to a text file via the top-right (⋮) menu"

aliucord {
    changelog = """
        # 1.0.0
        * Initial version
    """.trimIndent()

    author(Authors.RazerTexz)
}