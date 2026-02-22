package nl.vanwollingen.alerticorn.api

/**
 * Converts a string array to a map, where each pair of consecutive strings is mapped to a key-value entry.
 *
 * If the array has an odd number of elements, the last key is mapped to an empty string.
 *
 * Example: `["key1", "val1", "key2", "val2"]` becomes `{key1=val1, key2=val2}`
 *
 * @param stringArray the string array to be converted
 * @return a map of key-value pairs
 */
fun stringArrayToMap(stringArray: Array<String>): Map<String, String> {
    return stringArray.asSequence().chunked(2).associate { it[0] to it.getOrElse(1) { "" } }
}
