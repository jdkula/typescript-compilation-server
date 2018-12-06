package pw.jonak.typescriptserver

import io.netty.util.internal.ConcurrentSet
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

/** A very simple wrapper around temporary directory use. Created directories are marked for deletion on exit. */
object TemporaryFileManager {
    private val inUse = ConcurrentSet<String>()

    private val count = AtomicInteger()

    /** Creates a temporary directory, and returns it as a File. */
    fun createTemporaryDirectory(): File {
        val name = count.getAndIncrement().toString()
        val file = File(name)
        if(!file.mkdir()) {
            throw IOException("Can't make temporary directory $name")
        }
        file.deleteOnExit()
        inUse.add(name)
        return file
    }

    /** Forces the immediate removal of the temporary directory identified by [name]. */
    fun removeTemporaryDirectory(name: String) {
        if(name !in inUse) {
            throw IllegalArgumentException("$name is not a temporary directory!")
        }
        inUse.remove(name)
        if(!File(name).deleteRecursively()) {
            throw IOException("Cannot delete temporary directory $name!")
        }
    }

    /**
     * Allows for more expressive resource management.
     * The given [block] is executed after a temporary directory is created,
     * with the directory's absolute path passed in as a string. At the end of the block,
     * the temporary
     */
    fun <T> withTemporaryDirectory(block: (directory: File) -> T): T {
        val temporaryDirectory = createTemporaryDirectory()
        try {
            return block(temporaryDirectory)
        } finally {
            removeTemporaryDirectory(temporaryDirectory.name)
        }
    }
}