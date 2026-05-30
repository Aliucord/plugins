import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.3"
description = "Show reply author, (optionally) show reply content, and fix clicking on them."

aliucord {
    author(Authors.rushii)

    changelog.set(
        """
		# 1.0.3
		* Fix changelog

		# 1.0.2
		* Update to new Aliucord

		# 1.0.1
		* Update patches

		# 1.0.0
		* Released
		""".trimIndent(),
    )
}
