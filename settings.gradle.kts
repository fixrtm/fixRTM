/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/") {
            name = "ossrh-snapshot"
        }
        maven(url = "https://maven.minecraftforge.net/") {
            name = "forge"
        }
    }
    dependencies {
        // use latest version by dependabot. dependabot supports dependencies in settings.gralde
        classpath("net.minecraftforge.gradle:ForgeGradle:5.1.24")
        classpath("org.ow2.asm:asm-util:6.0")
        classpath("com.anatawa12.java-stab-gen:gradle-library:1.0.0")
    }
}
