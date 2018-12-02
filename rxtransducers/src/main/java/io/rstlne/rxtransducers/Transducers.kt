package io.rstlne.rxtransducers

import net.onedaybeard.transducers.ReducingFunction
import net.onedaybeard.transducers.ReducingFunctionOn
import net.onedaybeard.transducers.Transducer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Creates a stateful transducer that transforms a reducing function by
 * accumulating and processing the successive applications of a scan fn
 * to inputs.
 *
 * @param scanFn "inner" reducing function applied to each input
 * @param [A] input type of both input and output reducing function
 * @return a new transducer
 */
fun <A> scan(scanFn: (A, A) -> A) = object : Transducer<A, A> {
    override fun <R> apply(rf: ReducingFunction<R, A>) = object : ReducingFunction<R, A> {
        private var previousValue: A? = null
        override fun apply(result: R, input: A, reduced: AtomicBoolean): R {
            val newValue = previousValue?.let { scanFn(it, input) } ?: input
            previousValue = newValue
            return rf.apply(result, newValue, reduced)
        }
    }
}

/**
 * Creates a stateful transducer that transforms a reducing function by
 * accumulating and processing the successive applications of a scan fn
 * to inputs, initialized with a seed. Unlike the RxJava version,
 * does not emit seed.
 *
 * @param seed first argument to the first application
 * @param scanFn "inner" reducing function applied to each input
 * @param [A] input type of input reducing function
 * @param [B] input type of output reducing function
 * @return a new transducer
 */
fun <A, B> scan(seed: A, scanFn: (A, B) -> A) = object : Transducer<A, B> {
    override fun <R> apply(rf: ReducingFunction<R, A>) = object : ReducingFunctionOn<R, A, B>(rf) {
        private var value = seed
        override fun apply(result: R, input: B, reduced: AtomicBoolean): R {
            value = scanFn(value, input)
            return rf.apply(result, value, reduced)
        }
    }
}

/**
 * Creates a stateful transducer that transforms a reducing function by
 * applying a mapping function to each input and the number of inputs
 * that preceded it.
 *
 * @param f a function of the index and input type to the output type
 * @param [A] input type of input reducing function
 * @param [B] input type of output reducing function
 * @return a new transducer
 */
fun <A, B> mapIndexed(f: (Int, B) -> A) = object : Transducer<A, B> {
    override fun <R> apply(rf: ReducingFunction<R, A>) = object : ReducingFunctionOn<R, A, B>(rf) {
        private var index = 0
        override fun apply(result: R, input: B, reduced: AtomicBoolean): R =
            rf.apply(result, f(index++, input), reduced)
    }
}
