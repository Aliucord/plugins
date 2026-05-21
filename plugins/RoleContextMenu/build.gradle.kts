import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.2.1"
description = "RoleContextMenu"

aliucord {
    author(Authors.HalalKing)
    author(Authors.Wing)
}