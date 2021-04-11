buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/") {
            name = "ossrh-snapshot"
        }
        maven(url = "https://files.minecraftforge.net/maven") {
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
        classpath("com.anatawa12.mod-patching:mod-patching-gradle-plugin:1.0.0-SNAPSHOT") {
            isChanging = true
        }
    }
    configurations.classpath.get().resolutionStrategy.cacheDynamicVersionsFor(10, "minutes")
    configurations.classpath.get().resolutionStrategy.cacheChangingModulesFor(10, "minutes")
}
