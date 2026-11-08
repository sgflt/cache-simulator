package cz.zcu.kiv.cachesimulator.policy

/**
 * Map key for a file name: two names have the same key exactly when they are equal ignoring case
 * (like [String.equals] with `ignoreCase = true`). Policies use it to look files up in hash maps.
 */
internal fun fileKey(fileName: String): String {
    // plain lower-case ASCII already is its own key
    if (fileName.none { it >= '\u0080' || it in 'A'..'Z' }) return fileName
    return buildString(fileName.length) {
        fileName.codePoints().forEach { appendCodePoint(Character.toLowerCase(Character.toUpperCase(it))) }
    }
}
