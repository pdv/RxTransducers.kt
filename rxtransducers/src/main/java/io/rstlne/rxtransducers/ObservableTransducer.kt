package io.rstlne.rxtransducers

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import net.onedaybeard.transducers.ReducingFunction
import net.onedaybeard.transducers.Transducer
import java.util.concurrent.atomic.AtomicBoolean

private fun <A, R : ObservableEmitter<A>> observableRf(): ReducingFunction<R, A> =
    object : ReducingFunction<R, A> {
        override fun apply(result: R, input: A, reduced: AtomicBoolean): R =
            result.apply {
                if (reduced.get()) onComplete()
                else onNext(input)
            }
    }

/**
 * Applies transducer to an Observable.
 * @param xf a transducer (or composed transducers) from A to B
 * @param [A] input type
 * @param [B} output type
 * @return result of apply xf to this
 */
fun <A, B> Observable<A>.transduce(xf: Transducer<B, A>): Observable<B> =
    Observable.create { emitter ->
        val rf = xf.apply(observableRf<B, ObservableEmitter<B>>())
        val completed = AtomicBoolean(false)
        subscribe(
            { rf.apply(emitter, it, completed) },
            emitter::onError,
            { completed.set(true).also { emitter.onComplete() } }
        ).also(emitter::setDisposable)
    }