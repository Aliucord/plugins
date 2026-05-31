import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.3.0"
description = "Replaces the TM, R, and C emojis with ones that are more visible."

aliucord {
    author(Authors.rushii)

    changelog.set(
        """
            # 1.3.0
            * Restructure internals
            * Bundle all icons together with plugin

			# 1.2.2
			* Fix target resource IDs

			# 1.2.1
			* Fix changelog

			# 1.2.0
			* Add original emojis as an option

			# 1.1.0
			* Add Registered Trademark & Copyright emoji

			# 1.0.0
			* Released
		""".trimIndent(),
    )
}
