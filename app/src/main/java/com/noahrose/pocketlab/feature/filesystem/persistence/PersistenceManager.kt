package com.noahrose.pocketlab.feature.filesystem.persistence

import android.content.Context
import com.noahrose.pocketlab.feature.filesystem.FileNode
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object PersistenceManager {

    private const val FILE_NAME =
        "atlas_filesystem.json"

    private var applicationContext: Context? =
        null

    fun initialize(context: Context) {

        applicationContext =
            context.applicationContext
    }

    fun save(root: FileNode): Boolean {

        val context =
            applicationContext
                ?: return false

        return try {

            val json =
                nodeToJson(root)

            val file =
                File(
                    context.filesDir,
                    FILE_NAME
                )

            file.writeText(
                json.toString(2)
            )

            true

        } catch (exception: Exception) {

            false
        }
    }

    fun load(): FileNode? {

        val context =
            applicationContext
                ?: return null

        val file =
            File(
                context.filesDir,
                FILE_NAME
            )

        if (!file.exists()) {
            return null
        }

        return try {

            val json =
                JSONObject(
                    file.readText()
                )

            jsonToNode(
                json = json,
                parent = null
            )

        } catch (exception: Exception) {

            null
        }
    }

    fun clear(): Boolean {

        val context =
            applicationContext
                ?: return false

        val file =
            File(
                context.filesDir,
                FILE_NAME
            )

        return !file.exists() ||
                file.delete()
    }

    private fun nodeToJson(
        node: FileNode
    ): JSONObject {

        val children =
            JSONArray()

        node.children.forEach { child ->

            children.put(
                nodeToJson(child)
            )
        }

        return JSONObject().apply {

            put(
                "name",
                node.name
            )

            put(
                "isDirectory",
                node.isDirectory
            )

            put(
                "content",
                node.content
            )

            put(
                "children",
                children
            )
        }
    }

    private fun jsonToNode(
        json: JSONObject,
        parent: FileNode?
    ): FileNode {

        val node =
            FileNode(
                name = json.getString(
                    "name"
                ),
                isDirectory = json.getBoolean(
                    "isDirectory"
                ),
                content = json.optString(
                    "content",
                    ""
                ),
                parent = parent
            )

        val children =
            json.optJSONArray(
                "children"
            ) ?: JSONArray()

        for (
        index in 0 until children.length()
        ) {

            val childJson =
                children.getJSONObject(
                    index
                )

            val childNode =
                jsonToNode(
                    json = childJson,
                    parent = node
                )

            node.children.add(
                childNode
            )
        }

        return node
    }
}