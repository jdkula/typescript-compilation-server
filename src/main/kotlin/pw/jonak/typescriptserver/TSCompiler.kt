package pw.jonak.typescriptserver

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import pw.jonak.Subprocess
import java.io.File
import java.util.concurrent.Executors

/**
 * Implements a TypeScript compilation server, with the given number of threads.
 */
class TSCompiler(nThreads: Int = 24) {
    private val dispatcher = Executors.newFixedThreadPool(nThreads).asCoroutineDispatcher()

    /** Compiles the given [program], returning output information as a TSOutput. */
    public suspend fun compile(program: TSProgram): TSOutput {
        return withContext(dispatcher) {
            TemporaryFileManager.withTemporaryDirectory {
                writeFiles(it, program)
                val (success, result) = doCompile(it)
                TSOutput(success, result, collectOutput(it))
            }
        }
    }

    /** Given a [location] to write to, writes all of the [program]'s files there. */
    private fun writeFiles(location: File, program: TSProgram) {
        for (programFile in program.files) {
            val file = File(location.absolutePath + File.separatorChar + programFile.name)
            file.writeText(programFile.content)
        }
    }

    /**
     * Given a [location] that contains files to be compiled, compiles those files.
     * The boolean of the returned pair represents whether the exit code was 0 or not;
     * the string contains the complete ANSI color-coded output of the program.
     */
    private fun doCompile(location: File): Pair<Boolean, String> {
        val tsc = Subprocess("tsc", "--pretty", workingDirectory = location.absolutePath)
        tsc.stdin.close()
        
        tsc.waitForCompletion(10)
        if (tsc.alive) {
            tsc.terminate()
        }

        val output = tsc.stdout.readText()
        val success = tsc.exitCode == 0
        return Pair(success, output)
    }

    /** Collects all the files in the given compilation [location] into a list of TSFiles. */
    private fun collectOutput(location: File): List<TSFile> {
        return location
            .listRecursively(location.absolutePath + File.separatorChar)
            .map { File(it) }
            .filter { it.isFile }
            .map { TSFile(it.name, it.readText()) }
    }
}

/** Recursively lists all files in a directory. */
fun File.listRecursively(prefix: String = ""): List<String> {
    val out = ArrayList<String>()
    for (filename in this.list()) {
        out.add(prefix + filename)
        val info = File(filename)
        if (info.isDirectory) {
            out.addAll(info.listRecursively(filename + File.separatorChar))
        }
    }
    return out
}