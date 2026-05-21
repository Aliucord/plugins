import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.1.3"
description = "VCID"

aliucord {
    author(Authors.HalalKing)
}