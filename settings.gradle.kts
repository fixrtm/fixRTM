buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://files.minecraftforge.net/maven") {
            name = "forge"
        }
    }
    dependencies {
        // required by ForgeGradle. see anatawa12/ForgeGradle-2.3#22
        classpath("com.anatawa12.forge:ForgeGradle:2.3-1.0.+") {
            isChanging = true
        }
        classpath("com.anatawa12.jasm:jasm-gradle-plugin:1.0.8") {
            exclude(group = "org.ow2.asm", module = "asm-debug-all")
        }
        classpath("com.anatawa12.java-stab-gen:gradle-library:1.0.0")
    }
}
