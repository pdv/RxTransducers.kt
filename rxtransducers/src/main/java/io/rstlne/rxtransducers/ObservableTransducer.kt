package io.rstlne.rxtransducers

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import net.onedaybeard.transducers.ReducingFunction
import net.onedaybeard.transducers.Transducer
import java.util.concurrent.atomic.AtomicBoolean

fun <A, R : ObservableEmitter<A>> observableRf(): ReducingFunction<R, A> {
    return object : ReducingFunction<R, A> {
        override fun apply(result: R,
                           input: A,
                           reduced: AtomicBoolean): R =
            result.apply {
                if (reduced.get()) onComplete()
                else onNext(input)
            }
    }
}

fun <A, B> Observable<A>.transduce(xf: Transducer<B, A>) = Observable.create<B> { emitter ->
    val completed = java.util.concurrent.atomic.AtomicBoolean(false)
    val rf = xf.apply(observableRf<B, ObservableEmitter<B>>())
    subscribe(object : Observer<A> {
        override fun onNext(t: A) {
            rf.apply(emitter, t, completed)
        }
        override fun onSubscribe(d: Disposable) =
            emitter.setDisposable(d)
        override fun onComplete() {
            completed.set(true)
            emitter.onComplete()
        }
        override fun onError(e: Throwable) = emitter.onError(e)
    })
}