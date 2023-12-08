package advent

import java.util.LinkedList

fun <S> depthFirstSearch(
    initialSearchState: S,
    search: (S) -> List<S>?
) {
    val toVisit: MutableList<S> = mutableListOf(initialSearchState)
    while (toVisit.isNotEmpty()) {
        toVisit += search(toVisit.removeLast())?.reversed() ?: return
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