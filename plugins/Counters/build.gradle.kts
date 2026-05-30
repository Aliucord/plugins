import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.2"
description = "Show an servers/online friend count in your server list."

aliucord {
    author(Authors.rushii)

    changelog.set(
        """
		# 1.1.2
		* Fix invisible status check

		# 1.1.1
		* Fix changelog

		# 1.1.0
		* Add padding between text and divider
		* Add setting for adjusting text size

		# 1.0.1
		* Remove a dev log

		# 1.0.0
		* Released
		""".trimIndent(),
    )
}
