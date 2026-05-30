import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.0"
description = "Shortcut to close DMs in the DM context menu."

aliucord {
    author(Authors.rushii)

    changelog.set(
        """
		# 1.0.0
		* Released
		""".trimIndent(),
    )
}
