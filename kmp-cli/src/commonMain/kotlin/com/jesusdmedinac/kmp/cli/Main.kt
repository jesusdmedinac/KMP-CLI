package com.jesusdmedinac.kmp.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import com.jesusdmedinac.kmp.cli.command.DoctorCommand
import com.jesusdmedinac.kmp.core.KmpCore

class KmpCommand : CliktCommand(
    name = "kmp",
) {
    init {
        versionOption(KmpCore.VERSION)
        subcommands(DoctorCommand())
    }

    override fun run() = Unit
}

fun main(args: Array<String>) = KmpCommand().main(args)
