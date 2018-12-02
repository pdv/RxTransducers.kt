package io.rstlne.transducers

import io.reactivex.Observable
import io.rstlne.rxtransducers.transduce
import io.rstlne.rxtransducers.xform
import net.onedaybeard.transducers.filter
import net.onedaybeard.transducers.map
import net.onedaybeard.transducers.plus
import org.junit.Test
import kotlin.math.sqrt

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun obsTest() {
        Observable.just(1, 2, 3)
            .transduce(xform)
            .blockingIterable()
            .forEach(::println)
    }

    @Test
    fun speedTest() {

        val count = 1000

        val listStart = System.currentTimeMillis()
        (0..count)
            .filter { it % 2 != 0 }
            .map { sqrt(it.toFloat()) }
        val listEnd = System.currentTimeMillis()
        println("Plain list: ${listEnd - listStart}ms")

        val rxStart = System.currentTimeMillis()
        Observable.range(0, count)
            .filter { it % 2 != 0 }
            .map { sqrt(it.toFloat()) }
            .blockingIterable()
        val rxEnd = System.currentTimeMillis()
        println("Plain Rx chain: ${rxEnd - rxStart}ms")

        val transducer =
            filter<Int> { it % 2 != 0} +
            map<Float, Int> { sqrt(it.toFloat()) }

        val xfListStart = System.currentTimeMillis()
        listOf(transducer, (0..count))
        val xfListEnd = System.currentTimeMillis()
        println("Transducer list: ${xfListEnd - xfListStart}ms")

        val xfRxStart = System.currentTimeMillis()
        Observable.range(0, count)
            .transduce(transducer)
            .blockingLast()
        val xfRxEnd = System.currentTimeMillis()
        println("Transducer Rx chain: ${xfRxEnd - xfRxStart}ms")

    }

}
