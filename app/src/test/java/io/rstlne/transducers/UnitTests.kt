package io.rstlne.transducers

import io.reactivex.Observable
import io.rstlne.rxtransducers.mapIndexed
import io.rstlne.rxtransducers.scan
import io.rstlne.rxtransducers.transduce
import net.onedaybeard.transducers.filter
import net.onedaybeard.transducers.listOf
import net.onedaybeard.transducers.map
import net.onedaybeard.transducers.plus
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.sqrt

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UnitTests {

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

        val count = 10000

        val listStart = System.currentTimeMillis()
        val list = (0..count)
            .filter { it % 2 != 0 }
            .map { sqrt(it.toFloat()) }
        val listEnd = System.currentTimeMillis()

        val rxStart = System.currentTimeMillis()
        val rx = Observable.range(0, count)
            .filter { it % 2 != 0 }
            .map { sqrt(it.toFloat()) }
            .blockingIterable()
            .toList()
        val rxEnd = System.currentTimeMillis()

        val transducer =
            filter<Int> { it % 2 != 0} +
            map<Float, Int> { sqrt(it.toFloat()) }

        val xfListStart = System.currentTimeMillis()
        val xfList = listOf(transducer, (0..count))
        val xfListEnd = System.currentTimeMillis()

        val xfRxStart = System.currentTimeMillis()
        val xfRx = Observable.range(0, count)
            .transduce(transducer)
            .blockingIterable()
            .toList()
        val xfRxEnd = System.currentTimeMillis()

        println("Plain list: ${listEnd - listStart}ms")
        println("Plain Rx chain: ${rxEnd - rxStart}ms")
        println("Transducer list: ${xfListEnd - xfListStart}ms")
        println("Transducer Rx chain: ${xfRxEnd - xfRxStart}ms")
        Assert.assertEquals(list, rx)
        Assert.assertEquals(list, xfList)
        Assert.assertEquals(list, xfRx)
    }

}
