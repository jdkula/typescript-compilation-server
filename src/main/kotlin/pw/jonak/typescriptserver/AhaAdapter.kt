package pw.jonak.typescriptserver

import pw.jonak.Subprocess


/** Provides a simple Kotlin API for converting an ANSI color-coded string to HTML, using aha. */
object AhaAdapter {
    /** Converts the given [ansi] color-coded string to HTML, and returns it. */
    fun convertToHtml(ansi: String): String {
        val aha = Subprocess("aha", "--black")
        aha.stdin.write(ansi)
        aha.stdin.flush()
        aha.stdin.close()

        aha.waitForCompletion(3)
        if(aha.alive) {
            aha.terminate()
        }

        return aha.stdout.readText()
    }
}