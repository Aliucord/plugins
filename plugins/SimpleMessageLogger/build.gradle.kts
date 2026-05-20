import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.0"
description = "Logs deleted and edited messages. Simpler and lighter than MessageLogger"

aliucord {
    changelog = """
        # 1.0.8
        * Fixed compatibility with FreeNitroEmojis and NitroSpoof

        # 1.0.0
        * Initial version
    """.trimIndent()

    author(Authors.RazerTexz)
}