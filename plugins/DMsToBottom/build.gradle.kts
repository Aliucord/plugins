import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.0"
description = "Moves DMs to the bottom of the server list"

aliucord {
    author(Authors.Ushie)
    changelog.set(
        """
            # 1.1.0
            * Added the ability to stack the server list to the bottom (toggleable in settings) 
        """.trimIndent(),
    )
}
