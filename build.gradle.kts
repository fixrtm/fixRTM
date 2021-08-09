/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

import com.anatawa12.jarInJar.gradle.TargetPreset
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.5.20"
    id("net.minecraftforge.gradle.forge")
    id("com.anatawa12.mod-patching.binary")
    id("com.anatawa12.mod-patching.source")
    id("com.anatawa12.mod-patching.resources-dev")
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.anatawa12.jarInJar") version "1.0.0"
}

version = property("modVersion")!!
group = property("modGroup")!!
base { archivesBaseName = property("modBaseName")!!.toString() }

sourceSets {
    api {
        java {
            srcDirs("src/api/rtm", "src/api/ngtlib")
        }
    }
    main {
        resources {
            srcDirs("src/main/rtmResources", "src/main/ngtlibResources")
        }
    }
}

minecraft {
    version = project.property("forgeVersion").toString()
    runDir = "run"

    // the mappings can be changed at any time, and must be in the following format.
    // snapshot_YYYYMMDD   snapshot are built nightly.
    // stable_#            stables are built at the discretion of the MCP team.
    // Use non-default mappings at your own risk. they may not always work.
    // simply re-run your setup task after changing the mappings to update your workspace.
    mappings = project.property("mcpVersion").toString()
    // makeObfSourceJar = false // an Srg named sources jar is made by default. uncomment this to disable.
}

val shade by configurations.creating
configurations.compile.get().extendsFrom(shade)

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    shade(kotlin("stdlib-jdk7"))
    shade("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    shade("io.sigpipe:jbsdiff:1.0")
    shade("com.anatawa12.sai:sai:0.0.2")
    shade("org.jetbrains:annotations:21.0.1")

    compileOnly(files(file("run/fixrtm-cache/script-compiled-class")))
//    compileOnly(files(projectDir.resolve("mods/rtm.deobf.jar"),
//        projectDir.resolve("mods/ngtlib.deobf.jar")))

    // https://mvnrepository.com/artifact/org.twitter4j/twitter4j-core
    apiImplementation("org.twitter4j:twitter4j-core:4.0.7")
    // https://mvnrepository.com/artifact/com.github.sarxos/webcam-capture
    apiImplementation("com.github.sarxos:webcam-capture:0.3.12")

    // https://mvnrepository.com/artifact/org.twitter4j/twitter4j-core
    compileOnly("org.twitter4j:twitter4j-core:4.0.7")
    // https://mvnrepository.com/artifact/com.github.sarxos/webcam-capture
    compileOnly("com.github.sarxos:webcam-capture:0.3.12")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

val processResources by tasks.getting(Copy::class) {
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", project.version)
    inputs.property("mcversion", project.minecraft.version)

    // replace stuff in mcmod.info, nothing else
    from(sourceSets.main.get().resources.srcDirs) {
        include("mcmod.info")

        // replace version and mcversion
        expand(mapOf(
            "version" to project.version,
            "mcversion" to project.minecraft.version
        ))
    }

    // copy everything else, thats not the mcmod.info
    from(sourceSets.main.get().resources.srcDirs) {
        exclude("mcmod.info")
    }
}

val coremods = mutableListOf(
    "com.anatawa12.fixRtm.asm.FixRtmCorePlugin",
    "com.anatawa12.fixRtm.asm.patching.PatchingFixRtmCorePlugin",
    "com.anatawa12.fixRtm.asm.preprocessing.PreprocessingFixRtmCorePlugin",
    "com.anatawa12.fixRtm.asm.hooking.HookingFixRtmCorePlugin"
)

val runServer by tasks.getting(JavaExec::class) {
    systemProperties["fml.coreMods.load"] = coremods.joinToString(",") +
            ",${resourcesDev.forgeFmlCoreModClassName}"
    systemProperties["legacy.debugClassLoading"] = "true"
    args("--noCoreSearch")
    /*
    systemProperties["legacy.debugClassLoadingSave"] = "true"
    // */
}

val runClient by tasks.getting(JavaExec::class) {
    systemProperties["fml.coreMods.load"] = coremods.joinToString(",") +
            ",${resourcesDev.forgeFmlCoreModClassName}"
    systemProperties["legacy.debugClassLoading"] = "true"
    args("--noCoreSearch")
    /*
    systemProperties["legacy.debugClassLoadingSave"] = "true"
    // */
    //*
    if (!project.hasProperty("noLogin") && project.hasProperty("minecraft.login.username") && project.hasProperty("minecraft.login.password"))
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
        args = args!! + listOf(
            "-username", project.property("minecraft.login.username").toString(),
            "-password", project.property("minecraft.login.password").toString()
        )
    // */
}

val jar by tasks.getting(Jar::class) {
    shade.forEach { dep ->
        from(project.zipTree(dep)) {
            exclude("META-INF", "META-INF/**")
            exclude("LICENSE.txt")
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

val shadowModJar by tasks.creating(ShadowJar::class) {
    dependsOn(tasks.copyJar.get())

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

tasks.assemble.get().dependsOn(copyShadowedJar)

tasks.embedJarInJar {
    dependsOn(copyShadowedJar)
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

runClient.outputs.upToDateWhen { false }

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
    mappingName = project.property("mcpVersion").toString()
    mcVersion = "1.12"
    forgeFlowerVersion = "1.5.498.12"
    autoInstallCli = true
    patch(rtm)
    patch(ngtlib)
}

resourcesDev {
    ofMod(rtm)
    ofMod(ngtlib)
}

tasks.copyModifiedClasses.get().dependsOn("reobfJar")

apply(from = "./processMods.gradle")

curseforge {
    apiKey = project.findProperty("com.anatawa12.curse.api-key").toString()
    project(closureOf<com.matthewprenger.cursegradle.CurseProject> {
        id = project.findProperty("com.anatawa12.curse.project-id").toString()
        changelogType = "markdown"
        changelog = file(project.findProperty("com.anatawa12.curse.changelog-path").toString())
        releaseType = project.findProperty("com.anatawa12.curse.release-type")?.toString() ?: "release"
        relations(closureOf<com.matthewprenger.cursegradle.CurseRelation> {
            requiredDependency("realtrainmod")
        })
        gameVersionStrings.add("1.12.2")
        gameVersionStrings.add("Forge")
        gameVersionStrings.add("Java 8")
    })
}
// */
