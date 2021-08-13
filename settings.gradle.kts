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
        // required by ForgeGradle. see anatawa12/ForgeGradle-2.3#22
        classpath("com.anatawa12.forge:ForgeGradle:2.3-1.0.+") {
            isChanging = true
        }
        classpath("org.ow2.asm:asm-util:6.0")
        classpath("com.anatawa12.java-stab-gen:gradle-library:1.0.0")
    }
    configurations.classpath.get().resolutionStrategy.cacheDynamicVersionsFor(10, "minutes")
    configurations.classpath.get().resolutionStrategy.cacheChangingModulesFor(10, "minutes")
}
