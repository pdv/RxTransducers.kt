package io.rstlne.rxtransducers

import io.reactivex.Observable
import net.onedaybeard.transducers.ReducingFunction
import net.onedaybeard.transducers.Transducer
import java.util.concurrent.atomic.AtomicBoolean

private fun <R, A> reducingFunction(f: (R, A, AtomicBoolean) -> R) =
    object : ReducingFunction<R, A> {
        override fun apply(result: R, input: A, reduced: AtomicBoolean): R =
            f(result, input, reduced)
    }

typealias SideEffect<T> = (T) -> Unit

fun <A> SideEffect<A>.asRf(): ReducingFunction<Any, A> =
    reducingFunction { _: Any, input: A, _: AtomicBoolean -> this(input) }

/**
 * Applies transducer to an Observable.
 * @param xf a transducer (or composed transducers) from A to B
 * @param [A] input type
 * @param [B} output type
 * @return result of apply xf to this
 */
fun <A, B> Observable<A>.transduce(xf: Transducer<B, A>): Observable<B> =
    Observable.create { emitter ->
        val rf = xf.apply((emitter::onNext).asRf())
        val completed = AtomicBoolean(false)
        subscribe(
            { input ->
                rf.apply(Unit, input, completed)
                if (completed.get()) {
                    emitter.onComplete()
                }
            },
            emitter::onError,
            emitter::onComplete
        ).also(emitter::setDisposable)
    }
