package pw.jonak.typescriptserver

/** Represents a TypeScript program to be compiled. */
data class TSProgram(val files: List<TSFile>) {

    /** Returns false if any file contains possibly insecure file elements, such as "..". */
    fun verifySecure(): Boolean {
        for (file in files) {

            if (containsInsecurePathElements(file.name)) {
                return false
            }

            if (file.name.equals(  // Check TSConfig for bad compilation destinations
                    "tsconfig.json",
                    ignoreCase = true
                ) && containsInsecurePathElements(file.content)
            ) {
                return false
            }
        }

        return true
    }

    companion object {
        /** Returns true if the given [str] has potentially problematic path elements. */
        private fun containsInsecurePathElements(str: String): Boolean {
            return (".." in str)
        }
    }
}