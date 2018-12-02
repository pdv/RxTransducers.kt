package io.rstlne.rxtransducers

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import net.onedaybeard.transducers.ReducingFunction
import net.onedaybeard.transducers.Transducer
import java.util.concurrent.atomic.AtomicBoolean

fun <A, R : ObservableEmitter<A>> observableRf(): ReducingFunction<R, A> =
    object : ReducingFunction<R, A> {
        override fun apply(result: R,
                           input: A,
                           reduced: AtomicBoolean): R =
            result.apply {
                if (reduced.get()) onComplete()
                else onNext(input)
            }
    }

fun <A, B> Observable<A>.transduce(xf: Transducer<B, A>): Observable<B> =
    Observable.create { emitter ->
        val completed = java.util.concurrent.atomic.AtomicBoolean(false)
        val rf = xf.apply(observableRf<B, ObservableEmitter<B>>())
        val disposable = subscribe(
            // onStart
            { rf.apply(emitter, it, completed) },
            // onError
            emitter::onError,
            // onComplete
            {
                completed.set(true)
                emitter.onComplete()
            }
        )
        emitter.setDisposable(disposable)
    }