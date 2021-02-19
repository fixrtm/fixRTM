package com.anatawa12.fixRtm.rtm.modelpack

enum class ModelState(private val label: String, val marker: String) {
    INITIALIZED("Initialized", "I"),
    CONSTRUCTED("Constructed", "C"),

    ;

    override fun toString(): String {
        return label
    }
}
