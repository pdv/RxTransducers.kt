package io.rstlne.transducers

import io.reactivex.Observable
import io.rstlne.rxtransducers.transduce
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
    fun speedTest() {

        val rxStart = System.currentTimeMillis()
        Observable.range(0, 10000000)
            .filter { it % 2 != 0 }
            .map { sqrt(it.toFloat()) }
            .blockingLast()
        val rxEnd = System.currentTimeMillis()
        println("Rx chain: ${rxEnd - rxStart}ms")

        val xfStart = System.currentTimeMillis()
        Observable.range(0, 10000000)
            .transduce(
                filter<Int> { it % 2 != 0 } +
                map<Float, Int> { sqrt(it.toFloat()) }
            )
            .blockingLast()
        val xfEnd = System.currentTimeMillis()
        println("xf chain: ${xfEnd - xfStart}ms")

    }

}
