package com.noahrose.pocketlab.feature.filesystem

class FileNode(

    var name: String,

    val isDirectory: Boolean = true,

    var content: String = "",

    val parent: FileNode? = null,

    val children: MutableList<FileNode> = mutableListOf()

)