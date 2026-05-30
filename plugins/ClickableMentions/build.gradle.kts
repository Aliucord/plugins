import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.3"
description = "Enable clicking user/channel mentions everywhere. (ex. embeds/channel topics)"

aliucord {
    author(Authors.rushii)

	changelog.set(
		"""
		# 1.0.3
		* Fix changelog

		# 1.0.2
		* Remove a dev log

		# 1.0.1
		* Fix random crash(?)

		# 1.0.0
		* Released
		""".trimIndent()
	)
}
