package io.rstlne.rxtransducers

import io.reactivex.Observable
import net.onedaybeard.transducers.ReducingFunction
import net.onedaybeard.transducers.Transducer
import java.util.concurrent.atomic.AtomicBoolean

fun <A, B> mapIndexed(f: (Int, B) -> A) = object : Transducer<A, B> {
    override fun <R> apply(rf: ReducingFunction<R, A>) = object : ReducingFunction<R, B> {
        private var index = 0
        override fun apply(result: R, input: B, reduced: AtomicBoolean): R =
            rf.apply(result, f(index++, input), reduced)
    }
}

fun <A, B> Observable<A>.mapIndexed(mapper: (Int, A) -> B) =
    scan(-1 to null as A?) { (idx, _), a -> idx.inc() to a }
        .skip(1)
        .map { mapper(it.first, it.second!!) }
