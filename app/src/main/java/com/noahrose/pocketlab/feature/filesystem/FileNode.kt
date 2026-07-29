package com.noahrose.pocketlab.feature.filesystem

class FileNode(

    var name: String,

    val isDirectory: Boolean = true,

    var content: String = "",

    var parent: FileNode? = null,

    val children: MutableList<FileNode> = mutableListOf()
)