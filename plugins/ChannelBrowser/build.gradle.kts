import com.aliucord.gradle.repo.Authors
import com.aliucord.gradle.repo.author

import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.proto

plugins {
    id("com.aliucord.plugins-repo")
    id("com.google.protobuf") version "0.9.5"
}

version = "0.1.0"
description = "Manage your channel list from a menu, show/hide channels and follow/unfollow categories"

aliucord {
    author(Authors.LampDelivery)
}

dependencies {
    implementation("com.google.protobuf:protobuf-javalite:4.28.3")
    api("com.google.code.gson:gson:2.10.1")
}

android {
    sourceSets {
        named("main") {
            proto { }
            java {
                srcDirs("${'$'}{protobuf.generatedFilesBaseDir}/main/javalite")
            }
        }
    }
}

protobuf {
    val version = "4.28.3"
    protoc {
        artifact = "com.google.protobuf:protoc:$version"
    }

    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("java") {
                    option("lite")
                }
            }
        }
    }
}
