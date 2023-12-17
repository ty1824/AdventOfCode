package advent

fun <S> depthFirstSearch(
    initialSearchState: List<S>,
    search: (S) -> List<S>?
) {
    val toVisit: MutableList<S> = initialSearchState.toMutableList()
    while (toVisit.isNotEmpty()) {
        toVisit += search(toVisit.removeLast())?.reversed() ?: return
    }
}

fun <S> lowestFirstSearch(
    initialSearchState: List<S>,
    comparator: Comparator<S>,
    search: (S) -> List<S>?
) {
    val toVisit: MutableList<S> = initialSearchState.toMutableList()
    while (toVisit.isNotEmpty()) {
        toVisit += search(toVisit.removeLast())?.reversed() ?: return
        toVisit.sortWith(comparator)
    }
}

inline fun <S> breadthFirstSearch(
    initialSearchStates: List<S>,
    beforeIteration: () -> Unit = {},
    afterIteration: () -> Unit = {},
    search: (S) -> List<S>?
) {
    var toVisit: List<S> = initialSearchStates
    while (toVisit.isNotEmpty()) {
        beforeIteration()
        toVisit = toVisit.flatMap { search(it) ?: return }
        afterIteration()
    }
}