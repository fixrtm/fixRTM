import com.anatawa12.javaStabGen.gradle.GenerateJavaStab

plugins {
    kotlin("jvm") version "1.4.20"
    id("com.anatawa12.jasm")
    id("net.minecraftforge.gradle.forge")
    id("com.anatawa12.mod-patching")
    id("com.matthewprenger.cursegradle") version "1.4.0"
}

version = property("modVersion")!!
group = property("modGroup")!!
base { archivesBaseName = property("modBaseName")!!.toString() }

fun SourceSet.jasm(configure: Action<SourceDirectorySet>): Unit =
    (this as org.gradle.api.internal.HasConvention).convention.plugins["jasm"]
        .let { it as com.anatawa12.jasm.plugins.gradle.JasmSourceSetExtension }
        .jasm(configure)

val SourceSet.jasm
    get() =
        (this as org.gradle.api.internal.HasConvention).convention.plugins["jasm"]
            .let { it as com.anatawa12.jasm.plugins.gradle.JasmSourceSetExtension }
            .jasm

sourceSets {
    api {
        java {
            srcDirs("src/api/rtm", "src/api/ngtlib")
        }
    }
    main {
        jasm {
            srcDirs("src/main/rtm", "src/main/ngtlib")
        }
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
    shade(kotlin("stdlib-jdk8"))
    shade(kotlin("reflect"))
    shade("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")
    shade("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.1.0")
    shade("io.sigpipe:jbsdiff:1.0") {
        exclude(group = "org.apache.commons", module = "commons-compress")
    }
    shade("com.anatawa12.sai:sai:0.0.2")

    compileOnly(files(file("run/fixrtm-cache/script-compiled-class")))
//    compileOnly(files(sourceSets.main.get().jasm.outputDir))
//    compileOnly(files(projectDir.resolve("mods/rtm.deobf.jar"),
//        projectDir.resolve("mods/ngtlib.deobf.jar")))

    // https://mvnrepository.com/artifact/org.twitter4j/twitter4j-core
    apiImplementation("org.twitter4j:twitter4j-core:4.0.7")
    // https://mvnrepository.com/artifact/com.github.sarxos/webcam-capture
    apiImplementation("com.github.sarxos:webcam-capture:0.3.12")

    // https://mvnrepository.com/artifact/org.twitter4j/twitter4j-core
    implementation("org.twitter4j:twitter4j-core:4.0.7")
    // https://mvnrepository.com/artifact/com.github.sarxos/webcam-capture
    implementation("com.github.sarxos:webcam-capture:0.3.12")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

fun Copy.configure() {
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

val processResources by tasks.getting(Copy::class) {
    configure()
}
val reprocessResources by tasks.getting(Copy::class) {
    configure()
}

val runClient by tasks.getting(JavaExec::class) {
    environment("fml.coreMods.load", "com.anatawa12.fixRtm.asm.FixRtmCorePlugin")
    systemProperties["legacy.debugClassLoading"] = "true"
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
            "FMLCorePlugin" to "com.anatawa12.fixRtm.asm.FixRtmCorePlugin",
            "FMLCorePluginContainsFMLMod" to "*",
            "FMLAT" to "fix-rtm_at.cfg"
        ))
    }
}

val compileJasm by tasks.getting(com.anatawa12.jasm.plugins.gradle.CompileJasmTask::class)
val compileJasmOutput = compileJasm.dir

val generateJavaStab by tasks.creating(GenerateJavaStab::class) {
    generatedDir = file("$buildDir/generated/stab")
    classpath = files(compileJasmOutput)
    dependsOn(compileJasm)
}

tasks.compileKotlin {
    dependsOn(tasks.generateUnmodifieds.get())
    dependsOn(generateJavaStab)
    source(generateJavaStab.generatedDir!!)
    include("**/*.java")
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
val rtm = mods.curse(id = "realtrainmod", version = "2.4.21") {
    name = "rtm"
    targetVersions("1.12.2")
}

@Suppress("SpellCheckingInspection")
val ngtlib = mods.curse(id = "ngtlib", version = "2.4.18") {
    name = "ngtlib"
    targetVersions("1.12.2")
}

patching {
    patch(rtm)
    patch(ngtlib)
    bsdiffPrefix = "com/anatawa12/fixRtm/asm/patches"
    sourceNameSuffix = "(modified by fixrtm)"
}

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
