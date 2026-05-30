import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.3"
description = "Remove Discord's built-in slash commands."

aliucord {
    author(Authors.rushii)

    changelog.set(
        """
		# 1.0.3
		* Fix changelog

		# 1.0.2
		* Update patches

		# 1.0.1
		* Fix NPE

		# 1.0.0
		* Released
		""".trimIndent(),
    )
}
