/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

import com.anatawa12.jarInJar.gradle.TargetPreset
import com.anatawa12.modPatching.source.internal.readTextOr
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.5.31"
    id("net.minecraftforge.gradle")
    id("com.anatawa12.mod-patching.binary") version "2.0.1"
    id("com.anatawa12.mod-patching.source") version "2.0.1"
    id("com.anatawa12.mod-patching.resources-dev") version "2.0.1"
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("com.anatawa12.jarInJar") version "1.0.3"
}

version = property("modVersion")!!
group = property("modGroup")!!
base { archivesBaseName = property("modBaseName")!!.toString() }

sourceSets {
    main {
        resources {
            srcDirs("src/main/rtmResources", "src/main/ngtlibResources")
        }
    }
}

val mcpChannel: String by extra
val mcpVersion: String by extra

minecraft.mappings(mcpChannel, mcpVersion)
minecraft.accessTransformer(file("src/main/resources/META-INF/fix-rtm_at.cfg"))

sourceSets.main.get().resources.srcDir("src/generated/resources")

val shade by configurations.creating
configurations.implementation.get().extendsFrom(shade)

repositories {
    mavenCentral()
}

dependencies {
    "minecraft"("net.minecraftforge:forge:1.12.2-14.23.5.2855")

    shade(kotlin("stdlib-jdk7"))
    shade("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    shade("io.sigpipe:jbsdiff:1.0")
    shade("com.anatawa12.sai:sai:0.0.2")
    shade("org.jetbrains:annotations:22.0.0")
    shade("org.sejda.imageio:webp-imageio:0.1.6")

    compileOnly(files(file("run/fixrtm-cache/script-compiled-class")))
//    compileOnly(files(projectDir.resolve("mods/rtm.deobf.jar"),
//        projectDir.resolve("mods/ngtlib.deobf.jar")))

    // https://mvnrepository.com/artifact/org.twitter4j/twitter4j-core
    compileOnly("org.twitter4j:twitter4j-core:4.0.7")
    // https://mvnrepository.com/artifact/com.github.sarxos/webcam-capture
    compileOnly("com.github.sarxos:webcam-capture:0.3.12")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.0")
}

val processResources by tasks.getting(Copy::class) {
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", project.version)

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    // replace stuff in mcmod.info, nothing else
    from(sourceSets.main.get().resources.srcDirs) {
        include("mcmod.info")

        // replace version and mcversion
        expand(mapOf(
            "version" to project.version,
            "mcversion" to "1.12.2"
        ))
    }

    // copy everything else, thats not the mcmod.info
    from(sourceSets.main.get().resources.srcDirs) {
        exclude("mcmod.info")
    }
}

// workaround for userdev bug
val copyResourceToClasses by tasks.creating(Copy::class) {
    tasks.classes.get().dependsOn(this)
    dependsOn(tasks.processResources)
    onlyIf { gradle.taskGraph.hasTask(tasks.getByName("prepareRuns")) }

    //into("$buildDir/classes/java/main")
    // if you write @Mod class in kotlin, please use code below
    into("$buildDir/classes/kotlin/main")
    from(tasks.processResources.get().destinationDir)
}

val coremods = mutableListOf(
    "com.anatawa12.fixRtm.asm.FixRtmCorePlugin",
    "com.anatawa12.fixRtm.asm.patching.PatchingFixRtmCorePlugin",
    "com.anatawa12.fixRtm.asm.preprocessing.PreprocessingFixRtmCorePlugin",
    "com.anatawa12.fixRtm.asm.hooking.HookingFixRtmCorePlugin"
)

val debugCoreMods = coremods + listOf(
    resourcesDev.forgeFmlCoreModClassName,
    "com.anatawa12.fixRtm.asm.FixRtmDevEnvironmentOnlyCorePlugin"
)

fun net.minecraftforge.gradle.common.util.RunConfig.commonConfigure() {
    workingDirectory(project.file("run"))
    args("--noCoreSearch")

    property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
    property("forge.logging.console.level", "debug")
    property("fml.coreMods.load", debugCoreMods.joinToString(","))
    property("legacy.debugClassLoading", "true")
    //property("legacy.debugClassLoadingSave", "true")
}

val runClient = minecraft.runs.create("client") {
    commonConfigure()
}

val runServer = minecraft.runs.create("server") {
    commonConfigure()
    //*
    if (!project.hasProperty("noLogin") && project.hasProperty("minecraft.login.username") && project.hasProperty("minecraft.login.password")) {
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
        args(
            "-username", project.property("minecraft.login.username").toString(),
            "-password", project.property("minecraft.login.password").toString()
        )
    }
    // */
}

val jar by tasks.getting(Jar::class) {
    shade.forEach { dep ->
        from(project.zipTree(dep)) {
            exclude("META-INF", "META-INF/**")
            exclude("LICENSE.txt")
        }
        from(project.zipTree(dep)) {
            include("META-INF/services/**")
        }
    }

    manifest {
        attributes(mapOf(
            "FMLCorePlugin" to coremods.joinToString(";"),
            "FMLCorePluginContainsFMLMod" to "*",
            "FMLAT" to "fix-rtm_at.cfg"
        ))
    }
}

tasks.jar.get().finalizedBy("reobfJar")

val shadowModJar by tasks.creating(ShadowJar::class) {
    dependsOn("reobfJar")

    val basePkg = "com.anatawa12.fixRtm.libs"
    // add also in FixRtmDevEnvironmentOnlyCorePlugin
    relocate("kotlin.", "$basePkg.kotlin.")
    relocate("kotlinx.", "$basePkg.kotlinx.")
    relocate("io.sigpipe.jbsdiff.", "$basePkg.jbsdiff.")
    relocate("org.intellij.lang.annotations.", "$basePkg.ij_annotations.")
    relocate("org.jetbrains.annotations.", "$basePkg.jb_annotations.")
    relocate("org.apache.commons.compress.", "$basePkg.commons_compress.")
    relocate("org.tukaani.xz.", "$basePkg.xz.")

    from(provider { zipTree(tasks.jar.get().archiveFile) })
    destinationDirectory.set(buildDir.resolve("shadowing"))
    archiveVersion.set("")
    manifest.from(provider {
        zipTree(tasks.jar.get().archiveFile)
            .matching { include("META-INF/MANIFEST.MF") }
            .files.first()
    })
}

val copyShadowedJar by tasks.creating {
    dependsOn(shadowModJar)
    doLast {
        shadowModJar.archiveFile.get().asFile.inputStream().use { src ->
            tasks.jar.get().archiveFile.get().asFile.apply { parentFile.mkdirs() }
                .outputStream()
                .use { dst -> src.copyTo(dst) }
        }
    }
}

tasks.listModifiedClasses.get().dependsOn(copyShadowedJar)

tasks.assemble.get().dependsOn(copyShadowedJar)

tasks.embedJarInJar {
    dependsOn(tasks.copyJar.get())
    target = TargetPreset.FMLInForge
    basePackage = "com.anatawa12.fixRtm.jarInJar"
}

tasks.assemble.get().dependsOn(tasks.embedJarInJar.get())

repositories {
    mavenLocal()
}

tasks.compileKotlin {
    kotlinOptions {
        //freeCompilerArgs = ["-XXLanguage:+InlineClasses"]
    }
}

tasks.compileTestKotlin {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

val makeSourceDir by tasks.creating {
    doLast {
        buildDir.resolve("sources/main/java").mkdirs()
    }
}
tasks.compileJava.get().dependsOn(makeSourceDir)

tasks.test {
    useJUnitPlatform()
}

@Suppress("SpellCheckingInspection")
val rtm = mods.curse(id = "realtrainmod", version = property("rtmVersion").toString()) {
    name = "rtm"
    targetVersions("1.12.2")
}

@Suppress("SpellCheckingInspection")
val ngtlib = mods.curse(id = "ngtlib", version = property("ngtVersion").toString()) {
    name = "ngtlib"
    targetVersions("1.12.2")
}

binPatching {
    patch(rtm)
    patch(ngtlib)
    bsdiffPrefix = "com/anatawa12/fixRtm/asm/patches"
    sourceNameSuffix = "(modified by fixrtm)"
}

sourcePatching {
    mappingName = "${mcpChannel}_${mcpVersion.substringBefore('-')}"
    mcVersion = mcpVersion.substringAfter('-')
    forgeFlowerVersion = "1.5.498.12"
    autoInstallCli = true
    patch(rtm)
    patch(ngtlib)
}

resourcesDev {
    ofMod(rtm)
    ofMod(ngtlib)
}

// workaround for anatawa12/mod-patching#78
enum class ModifiedType {
    SAME,
    MODIFIED,
    UNMODIFIED,
}
tasks.copyModifiedClasses {
    from(project.provider { zipTree(tasks.jar.get().archiveFile) }) {
        include {
            tasks.listModifiedClasses.get().run {
                modifiedInfoDir.get().file(it.path).asFile.readTextOr("UNMODIFIED")
                    .let(ModifiedType::valueOf) == ModifiedType.MODIFIED
            }
        }
    }
}

apply(from = "./processMods.gradle")
