import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

plugins {
    id("com.aliucord.plugins-repo")
}

version = "1.2.4"
description = "Show \"WEBHOOK\" \"OWNER\" \"MOD\" and \"STAFF\" tags appropriately."

aliucord {
    author(Authors.Cloudburst)
}