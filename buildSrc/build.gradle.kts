/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()

    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("org.ow2.asm:asm-commons:6.0")
    implementation("org.ow2.asm:asm-util:9.4")
    implementation("org.ow2.asm:asm:6.0")
}
