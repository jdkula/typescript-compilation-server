package pw.jonak.typescriptserver

/** Represents the result of a single compilation. */
data class TSOutput(val success: Boolean, val result: String, val files: List<TSFile>)