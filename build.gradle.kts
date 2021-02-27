import com.anatawa12.javaStabGen.gradle.GenerateJavaStab

plugins {
    kotlin("jvm")
    id("com.anatawa12.jasm")
    id("net.minecraftforge.gradle.forge")
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
        /*jasm {
            srcDirs("src/main/rtm", "src/main/ngtlib")
        }*/
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
configurations.compile.extendsFrom(shade)

repositories {
    jcenter()
    mavenCentral()
}

val apiCompile by configurations.getting
dependencies {
    shade(kotlin("stdlib-jdk7"))
    shade("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")
    shade("io.sigpipe:jbsdiff:1.0") {
        exclude(group = "org.apache.commons", module = "commons-compress")
    }
    shade("com.anatawa12.sai:sai:0.0.2")

    compileOnly(files(file("run/fixrtm-cache/script-compiled-class")))
    //compileOnly(files(sourceSets.main.jasm.outputDir))//TODO
    //compileOnly files(new File(projectDir, "mods/rtm.deobf.jar"),
    //        new File(projectDir, "mods/ngtlib.deobf.jar"))

    // https://mvnrepository.com/artifact/org.twitter4j/twitter4j-core
    apiCompile("org.twitter4j:twitter4j-core:4.0.7")
    // https://mvnrepository.com/artifact/com.github.sarxos/webcam-capture
    apiCompile("com.github.sarxos:webcam-capture:0.3.12")

    // https://mvnrepository.com/artifact/org.twitter4j/twitter4j-core
    compile("org.twitter4j:twitter4j-core:4.0.7")
    // https://mvnrepository.com/artifact/com.github.sarxos/webcam-capture
    compile("com.github.sarxos:webcam-capture:0.3.12")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

val processResources by tasks.getting(Copy::class) {
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", project.version)
    //inputs.property("mcversion", project.minecraft.version)//TODO

    // replace stuff in mcmod.info, nothing else
    from(sourceSets.main.get().resources.srcDirs) {
        include("mcmod.info")

        // replace version and mcversion
        expand(mapOf(
            "version" to project.version//, TODO
            //"mcversion" to project.minecraft.version
        ))
    }

    // copy everything else, thats not the mcmod.info
    from(sourceSets.main.get().resources.srcDirs) {
        exclude("mcmod.info")
    }
}

val runClient by tasks.getting(JavaExec::class) {
    environment("fml.coreMods.load", "com.anatawa12.fixRtm.asm.FixRtmCorePlugin")
    /*
    systemProperties["legacy.debugClassLoading"] = "true"
    systemProperties["legacy.debugClassLoadingSave"] = "true"
    // */
    //*
    if (!project.hasProperty("noLogin") && project.hasProperty("minecraft.login.username") && project.hasProperty("minecraft.login.password"))
        args = args.orEmpty() + listOf(
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

apply(from = "./processMods.gradle")
apply(from = "./makePatch.gradle")
