package top.maplex.fomalhautshop.utils

fun String.asChar(): Char {
    return this.toCharArray()[0]
}


fun List<MutableList<String>>.flattenList(): List<String> {
    val set = HashSet<String>()
    val result = mutableListOf<String>()
    for (sublist in this) {
        for (str in sublist) {
            if (!set.contains(str)) {
                set.add(str)
                result.add(str)
            }
        }
    }
    return result
}
