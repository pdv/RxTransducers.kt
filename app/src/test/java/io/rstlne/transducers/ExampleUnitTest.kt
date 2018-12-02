package io.rstlne.transducers

import io.reactivex.Observable
import io.rstlne.rxtransducers.mapIndexed
import io.rstlne.rxtransducers.scan
import io.rstlne.rxtransducers.transduce
import net.onedaybeard.transducers.Transducer
import net.onedaybeard.transducers.filter
import net.onedaybeard.transducers.map
import net.onedaybeard.transducers.plus
import net.onedaybeard.transducers.listOf
import org.junit.Test
import org.junit.Assert.*
import kotlin.math.sqrt

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun testObsTransduce() {

        // sum of all the even numbers seen so far

        val list = (0 until 10)
            .filter { it % 2 == 0 }
            .scan(0) { a, b -> a + b }
            .mapIndexed { index, i: Int -> "$index: $i" }

        val rxChain = Observable.range(0, 10)
            .filter { it % 2 == 0 }
            .scan(0) { a, b -> a + b }
            .skip(1)
            .mapIndexed { index, i: Int -> "$index: $i" }
            .blockingIterable()
            .toList()

        fun transducer() =
            filter<Int> { it % 2 == 0 } +
            scan(0) { a, b -> a + b } +
            mapIndexed { index, i: Int -> "$index: $i" }

        val rxTransduced = Observable.range(0, 10)
            .transduce(transducer())
            .blockingIterable()
            .toList()

        val listTransduced = listOf(transducer(), (0 until 10))

        assertEquals("Regular RxChain", list, rxChain)
        assertEquals("Transducer RxChain", list, rxTransduced)
        assertEquals("Transducer List", list, listTransduced)
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
