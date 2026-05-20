import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.7"
description = "Type '@silent' at the start of your message"

aliucord {
    changelog = """
        # 1.0.1
        * Added an indicator for silent messages

        # 1.0.0
        * Initial version
    """.trimIndent()

    author(Authors.RazerTexz)
}