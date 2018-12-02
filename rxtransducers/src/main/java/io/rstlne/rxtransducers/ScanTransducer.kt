package io.rstlne.rxtransducers

import net.onedaybeard.transducers.ReducingFunction
import net.onedaybeard.transducers.Transducer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Creates a transducer that transforms a reducing function by
 * - performing an "inner reduction"
 * - accumulating an A from Bs via [scanFn] called on successive inputs
 *
 * TODO: pass initialValue "onSubscribe" (when applied to reducing fn)?
 * - Observable does this
 * - Makes sense in some cases, not in others
 */
fun <A, B> scan(initialValue: A, scanFn: (A, B) -> A) = object : Transducer<A, B> {
    override fun <R> apply(rf: ReducingFunction<R, A>) = object : ReducingFunction<R, B> {
        private var currentValue = initialValue
        override fun apply(result: R, input: B, reduced: AtomicBoolean): R {
            currentValue = scanFn(currentValue, input)
            return rf.apply(result, currentValue, reduced)
        }
    }
}

fun <A, B> Iterable<B>.scan(initialValue: A, rf: (A, B) -> A): Iterable<A> =
    fold(listOf()) { prev, b ->
        val last = prev.lastOrNull() ?: initialValue
        val next = rf(last, b)
        prev + next
    }
