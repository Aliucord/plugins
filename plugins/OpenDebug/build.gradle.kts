import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.2"
description = "Opens the debug log as soon as opening the app & replaces the gift button"

aliucord {
    author(Authors.rushii)

    changelog.set(
        """
			# 1.0.2
			* Fix changelog

			# 1.0.1
			* Update patches

			# 1.0.0
			* Released
		""".trimIndent(),
    )
}
