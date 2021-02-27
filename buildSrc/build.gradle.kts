plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()

    maven(url = "https://jitpack.io")
}

dependencies {
    compile("com.github.TheRandomLabs:CurseAPI:master-SNAPSHOT")
    compile("org.ow2.asm:asm-commons:6.0")
    compile("org.ow2.asm:asm-util:6.0")
    compile("org.ow2.asm:asm:6.0")

    compile("io.sigpipe:jbsdiff:1.0")

    // https://mvnrepository.com/artifact/io.github.java-diff-utils/java-diff-utils
    implementation("io.github.java-diff-utils:java-diff-utils:4.5")
}
