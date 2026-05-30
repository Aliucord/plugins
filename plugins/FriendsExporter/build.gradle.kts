import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.2"
description = "Export your friends list with /friendsexport"

aliucord {
    author(Authors.rushii)

    changelog.set(
        """
		# 1.0.2
		* Fix not handling leading 0s in discriminators

		# 1.0.1
		* Escape markdown when printing in chat

		# 1.0.0
		* Released
		""".trimIndent(),
    )
}
