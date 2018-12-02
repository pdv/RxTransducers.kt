package io.rstlne.rxtransducers

import io.reactivex.Observable

/**
 * Returns a list of the intermediate steps of the reduction of [this] by
 * [rf], starting with [init].
 */
fun <A, B> Iterable<B>.scan(init: A, rf: (A, B) -> A): Iterable<A> =
    fold(listOf()) { result, input ->
        val previous = result.lastOrNull() ?: init
        val next = rf(previous, input)
        result + next
    }

/**
 * Applies [mapper] to each emission of [this] and its index
 */
fun <A, B> Observable<A>.mapIndexed(mapper: (Int, A) -> B): Observable<B> =
    scan(-1 to null as A?) { (idx, _), a -> idx.inc() to a }
        .skip(1)
        .map { mapper(it.first, it.second!!) }