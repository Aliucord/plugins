import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.2.0"
description = "add an option to copy images to your clipboard"

aliucord {
    author(Authors.miaaaa0a)
    
    changelog.set(
        """
        # 1.2.0
        * completely rewrite mime type detection
        """.trimIndent(),
    )
}
