import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.0.2"
description = "Open users' profiles in reaction lists on click."

aliucord {
    author(Authors.rushii)

	changelog.set(
		"""
		# 1.0.2
		* Fix changelog

		# 1.0.1
		* Uncached users sometimes don't show

		# 1.0.0
		* Released
		""".trimIndent()
	)
}
