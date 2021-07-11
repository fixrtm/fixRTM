/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

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
