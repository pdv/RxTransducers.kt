package io.rstlne.rxtransducers

import io.reactivex.Observable
import net.onedaybeard.transducers.ReducingFunction
import net.onedaybeard.transducers.Transducer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Turns side-effecting [onNext] and [onComplete] functions into a ReducingFunction
 * that calls [onNext] on each input and [onComplete] after the last one but does not
 * modify the result.
 */
private fun <R, A> reducingFunction(
    onNext: (A) -> Unit = {},
    onComplete: () -> Unit = {}
) = object : ReducingFunction<R, A> {

    override fun apply(result: R, input: A, reduced: AtomicBoolean): R {
        onNext(input)
        return result
    }

    override fun apply(result: R): R {
        onComplete()
        return result
    }

}

/**
 * Applies transducer to an Observable.
 * @param xform a transducer (or composed transducers) from A to B
 * @param [A] input type
 * @param [B] output type
 * @return result of apply xform to this
 */
fun <A, B> Observable<A>.transduce(xform: Transducer<B, A>): Observable<B> =
    Observable.create { emitter ->
        val rf = reducingFunction<Any, B>(emitter::onNext, emitter::onComplete)
        val xf = xform.apply(rf)
        val disposable = subscribe(
            { item: A ->
                val complete = AtomicBoolean(false)
                xf.apply(Unit, item, complete)
                if (complete.get()) {
                    xf.apply(Unit)
                }
            },
            emitter::onError,
            { xf.apply(Unit) }
        )
        emitter.setDisposable(disposable)
    }
