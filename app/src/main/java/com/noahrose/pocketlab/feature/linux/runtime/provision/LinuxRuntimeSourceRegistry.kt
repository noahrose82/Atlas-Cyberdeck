package com.noahrose.pocketlab.feature.linux.runtime.provision

object LinuxRuntimeSourceRegistry {

    val PROOT =
        LinuxRuntimeSourceDescriptor(
            id =
                "atlas-termux-proot",

            projectName =
                "Atlas PRoot",

            repository =
                "termux/proot",

            version =
                "5.1.107.92",

            sourceArchiveSha256 =
                "29385d1ddb619a9c4449ab512bfd55032034b22f724ddf98fc95ff300ea32135",

            license =
                "GPL-2.0"
        )
}