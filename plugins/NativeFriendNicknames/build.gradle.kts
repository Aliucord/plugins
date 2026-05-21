import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.6"
description = "a plugin that backports the friend nickname feature"

aliucord {
    author(Authors.miaaaa0a)

    changelog.set(
        """
        # 1.1.6
        * don't display friend nickname in servers
        """.trimIndent(),
    )
}
